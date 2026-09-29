/*
 *    Copyright 2016-2025 the original author or authors.
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
package org.mybatis.dynamic.sql.update

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.common.CommonBuilder
import org.mybatis.dynamic.sql.common.OrderByModel
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.update.render.UpdateRenderer
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.where.WhereModel
import java.util.Objects
import java.util.Optional
import java.util.function.Function
import java.util.stream.Stream

/**
 * update 模型。
 */
class UpdateModel private constructor(builder: Builder) {
    private val table: SqlTable
    private val tableAlias: String?
    private val whereModel: WhereModel?
    private val columnMappings: List<AbstractColumnMapping>
    private val limit: Long?
    private val orderByModel: OrderByModel?
    private val statementConfiguration: StatementConfiguration

    init {
        table = Objects.requireNonNull(builder.table())
        whereModel = builder.whereModel()
        columnMappings = Objects.requireNonNull(builder.columnMappings)
        tableAlias = builder.tableAlias()
        limit = builder.limit()
        orderByModel = builder.orderByModel()
        Validator.assertNotEmpty(columnMappings, "ERROR.17") //$NON-NLS-1$
        statementConfiguration = Objects.requireNonNull(builder.statementConfiguration())
    }

    fun table(): SqlTable {
        return table
    }

    fun tableAlias(): Optional<String> {
        return Optional.ofNullable(tableAlias)
    }

    fun whereModel(): Optional<WhereModel> {
        return Optional.ofNullable(whereModel)
    }

    fun columnMappings(): Stream<AbstractColumnMapping> {
        return columnMappings.stream()
    }

    fun limit(): Optional<Long> {
        return Optional.ofNullable(limit)
    }

    fun orderByModel(): Optional<OrderByModel> {
        return Optional.ofNullable(orderByModel)
    }

    fun statementConfiguration(): StatementConfiguration {
        return statementConfiguration
    }

    fun render(renderingStrategy: RenderingStrategy): UpdateStatementProvider {
        return UpdateRenderer.withUpdateModel(this)
            .withRenderingStrategy(renderingStrategy)
            .build()
            .render()
    }

    fun <R> map(mapper: Function<UpdateModel, R>): R {
        return mapper.apply(this)
    }

    companion object {
        @JvmStatic
        fun withTable(table: SqlTable): Builder {
            return Builder().withTable(table)
        }
    }

    class Builder : CommonBuilder<Builder>() {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        val columnMappings: MutableList<AbstractColumnMapping> = ArrayList()

        fun withColumnMappings(columnMappings: List<AbstractColumnMapping>): Builder {
            this.columnMappings.addAll(columnMappings)
            return this
        }

        override fun self(): Builder {
            return this
        }


        fun build(): UpdateModel {
            return UpdateModel(this)
        }
    }
}
