/*
 *    Copyright 2016-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.filter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.dsl.GeneralInsertDSL
import org.mybatis.dynamic.sql.dsl.UpdateDSL
import org.mybatis.dynamic.sql.render.RenderingStrategies
import org.mybatis.dynamic.sql.isEqualTo

/**
 * 列过滤器（update / general insert 的 set 子句填充）的回归测试。
 */
class ColumnFilterTest {

    /**
     * 带审计字段列过滤器的表。过滤器在对象内部引用自身列，
     * 通过匿名对象避免类层面的初始化循环。
     */
    private val person = object : SqlTable("person") {
        val id = column<Int>("id")
        val name = column<String>("name")
        val updatedBy = column<String>("updated_by")
        val createdBy = column<String>("created_by")

        private val filters: Array<SQLFilter> = arrayOf(object : SQLFilter {
            override fun doWhere(context: WhereFilterContext) = Unit

            override fun doUpdate(context: ColumnFilterContext) {
                context.setStringConstant(updatedBy, "updater")
            }

            override fun doGeneralInsert(context: ColumnFilterContext) {
                context.setStringConstant(createdBy, "creator")
            }
        })

        override fun filter(): Array<SQLFilter> = filters
    }

    @Test
    fun updateContributesColumnNotSetByUser() {
        val statement = UpdateDSL.update(person)
            .set(person.name).equalTo("Fred")
            .where(person.id, isEqualTo(1))
            .build()
            .render(RenderingStrategies.MYBATIS3)
            .updateStatement

        assertThat(statement).isEqualTo(
            "update person set name = #{parameters.p1}, updated_by = 'updater' where id = #{parameters.p2}"
        )
    }

    /**
     * 用户显式设置的列优先，过滤器不得覆盖，避免出现 set a = ?, a = 'x' 这类非法片段。
     */
    @Test
    fun userSetColumnWinsOverFilter() {
        val statement = UpdateDSL.update(person)
            .set(person.name).equalTo("Fred")
            .set(person.updatedBy).equalTo("bob")
            .where(person.id, isEqualTo(1))
            .build()
            .render(RenderingStrategies.MYBATIS3)
            .updateStatement

        assertThat(statement).isEqualTo(
            "update person set name = #{parameters.p1}, updated_by = #{parameters.p2} where id = #{parameters.p3}"
        )
        assertThat(statement).doesNotContain("'updater'")
    }

    @Test
    fun generalInsertContributesColumn() {
        val statement = GeneralInsertDSL.insertInto(person)
            .set(person.name).toValue("Fred")
            .build()
            .render(RenderingStrategies.MYBATIS3)
            .insertStatement

        assertThat(statement).isEqualTo(
            "insert into person (name, created_by) values (#{parameters.p1}, 'creator')"
        )
    }

    @Test
    fun generalInsertDoesNotContributeUpdateOnlyColumn() {
        val statement = GeneralInsertDSL.insertInto(person)
            .set(person.name).toValue("Fred")
            .build()
            .render(RenderingStrategies.MYBATIS3)
            .insertStatement

        assertThat(statement).doesNotContain("updated_by")
    }

    /**
     * 开关关闭时必须完全跳过过滤器，update 与 general insert 都一样。
     */
    @Test
    fun disabledFilterSkipsColumnContribution() {
        val updateStatement = UpdateDSL.update(person)
            .set(person.name).equalTo("Fred")
            .where(person.id, isEqualTo(1))
            .configureStatement { it.enabledFilter(false) }
            .build()
            .render(RenderingStrategies.MYBATIS3)
            .updateStatement

        assertThat(updateStatement).isEqualTo(
            "update person set name = #{parameters.p1} where id = #{parameters.p2}"
        )

        val insertStatement = GeneralInsertDSL.insertInto(person)
            .set(person.name).toValue("Fred")
            .configureStatement { it.enabledFilter(false) }
            .build()
            .render(RenderingStrategies.MYBATIS3)
            .insertStatement

        assertThat(insertStatement).isEqualTo(
            "insert into person (name) values (#{parameters.p1})"
        )
    }

    /**
     * 回归测试：列映射不得写回 DSL 的 columnMappings 字段，
     * 否则同一 DSL 多次 build 会累积重复列。
     */
    @Test
    fun repeatedBuildOfSameDslIsIdempotent() {
        val builder = UpdateDSL.update(person)
            .set(person.name).equalTo("Fred")
            .where(person.id, isEqualTo(1))

        val first = builder.build().render(RenderingStrategies.MYBATIS3).updateStatement
        val second = builder.build().render(RenderingStrategies.MYBATIS3).updateStatement
        val third = builder.build().render(RenderingStrategies.MYBATIS3).updateStatement

        assertThat(first).isEqualTo(
            "update person set name = #{parameters.p1}, updated_by = 'updater' where id = #{parameters.p2}"
        )
        assertThat(second).isEqualTo(first)
        assertThat(third).isEqualTo(first)
    }

    /**
     * 表上没有任何过滤器时，列映射应原样返回。
     */
    @Test
    fun tableWithoutFiltersIsUnaffected() {
        val plain = object : SqlTable("person") {
            val id = column<Int>("id")
            val name = column<String>("name")
        }

        val statement = UpdateDSL.update(plain)
            .set(plain.name).equalTo("Fred")
            .where(plain.id, isEqualTo(1))
            .build()
            .render(RenderingStrategies.MYBATIS3)
            .updateStatement

        assertThat(statement).isEqualTo(
            "update person set name = #{parameters.p1} where id = #{parameters.p2}"
        )
    }

    /**
     * 多个过滤器按声明顺序贡献列；过滤器之间同列去重，先声明者优先。
     */
    @Test
    fun multipleFiltersAreAppliedInDeclarationOrder() {
        val table = object : SqlTable("person") {
            val name = column<String>("name")
            val updatedBy = column<String>("updated_by")
            val updatedAt = column<String>("updated_at")

            private val filters: Array<SQLFilter> = arrayOf(
                object : SQLFilter {

                    override fun doUpdate(context: ColumnFilterContext) {
                        context.setStringConstant(updatedBy, "first")
                    }
                },
                object : SQLFilter {
                    override fun doUpdate(context: ColumnFilterContext) {
                        context.setStringConstant(updatedBy, "second")
                        context.setStringConstant(updatedAt, "now")
                    }
                }
            )

            override fun filter(): Array<SQLFilter> = filters
        }

        val statement = UpdateDSL.update(table)
            .set(table.name).equalTo("Fred")
            .build()
            .render(RenderingStrategies.MYBATIS3)
            .updateStatement

        assertThat(statement).isEqualTo(
            "update person set name = #{parameters.p1}, updated_by = 'first', updated_at = 'now'"
        )
    }
}
