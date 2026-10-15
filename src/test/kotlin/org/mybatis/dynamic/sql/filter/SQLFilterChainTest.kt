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
import org.mybatis.dynamic.sql.NullCriterion
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.dsl.SelectDSL
import org.mybatis.dynamic.sql.render.RenderingStrategies
import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo
import org.mybatis.dynamic.sql.util.kotlin.model.select
import org.mybatis.dynamic.sql.where.WhereApplier

class SQLFilterChainTest {

    /**
     * 带逻辑删除过滤器的表。过滤器在对象内部引用自身列，
     * 通过匿名对象避免类层面的初始化循环。
     */
    private val person = object : SqlTable("person") {
        val id = column<Int>("id")
        val firstName = column<String>("first_name")
        val deleted = column<Boolean>("deleted")

        private val filters: Array<SQLFilter> = arrayOf(
            object : SQLFilter {
                override fun doWhere(context: WhereFilterContext) {
                    context.and { deleted isEqualTo false }
                }
            }
        )

        override fun filter(): Array<SQLFilter> = filters
    }

    @Test
    fun filterConditionIsAppendedToExistingWhereClause() {
        val statement = select(person.id, person.firstName) {
            from(person)
            where { person.id isEqualTo 3 }
        }.render(RenderingStrategies.MYBATIS3).selectStatement
        assertThat(statement).isEqualTo(
            "select id, first_name from person where id = #{parameters.p1} and deleted = #{parameters.p2}"
        )
    }

    @Test
    fun filterConditionBecomesInitialCriterionWhenNoWhereClause() {
        val statement = select(person.id, person.firstName) {
            from(person)
        }.render(RenderingStrategies.MYBATIS3).selectStatement

        assertThat(statement).isEqualTo(
            "select id, first_name from person where deleted = #{parameters.p1}"
        )
    }

    /**
     * 回归测试：修复前 buildWhereModel 会把过滤结果写回 subCriteria 字段，
     * 导致同一 DSL 实例多次 build 时条件不断累积。
     */
    @Test
    fun repeatedBuildOfSameDslIsIdempotent() {
        val builder = SelectDSL.select(person.id, person.firstName)
            .from(person)
            .where(person.id, isEqualTo(3))

        val first = builder.build().render(RenderingStrategies.MYBATIS3).selectStatement
        val second = builder.build().render(RenderingStrategies.MYBATIS3).selectStatement
        val third = builder.build().render(RenderingStrategies.MYBATIS3).selectStatement

        assertThat(first).isEqualTo(
            "select id, first_name from person where id = #{parameters.p1} and deleted = #{parameters.p2}"
        )
        assertThat(second).isEqualTo(first)
        assertThat(third).isEqualTo(first)
    }

    /**
     * 回归测试：修复前链实例持有递增游标，第二次调用会静默跳过所有过滤器。
     */
    @Test
    fun repeatedUseOfSameChainInstanceIsStable() {
        val chain = SQLFilterChain.createFilterChain(person.filter()[0])

        val first = chain.filterWhere(WhereApplier(NullCriterion(), emptyList()), person)
        val second = chain.filterWhere(WhereApplier(NullCriterion(), emptyList()), person)

        assertThat(first.subCriteria()).hasSize(1)
        assertThat(second.subCriteria()).hasSize(1)
        assertThat(second.initialCriterion()).isNotNull()
    }

    @Test
    fun emptyFilterChainReturnsOriginalWhereApplier() {
        val chain = SQLFilterChain.createFilterChain()
        val where = WhereApplier(NullCriterion(), emptyList())
        assertThat(chain.filterWhere(where, person)).isSameAs(where)
    }

    @Test
    fun multipleFiltersAreAppliedInDeclarationOrder() {
        val table = object : SqlTable("person") {
            val id = column<Int>("id")
            val firstName = column<String>("first_name")
            private val filters: Array<SQLFilter> = arrayOf(
                object : SQLFilter {
                    override fun doWhere(context: WhereFilterContext) {
                        context.and { id isEqualTo 1 }
                    }
                },
                object : SQLFilter {
                    override fun doWhere(context: WhereFilterContext) {
                        context.and { firstName isEqualTo "Fred" }
                    }
                }
            )
            override fun filter(): Array<SQLFilter> = filters
        }

        val statement = select(table.id, table.firstName) {
            from(table)
        }.render(RenderingStrategies.MYBATIS3).selectStatement

        assertThat(statement).isEqualTo(
            "select id, first_name from person where id = #{parameters.p1} and first_name = #{parameters.p2}"
        )
    }
}
