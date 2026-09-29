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
package org.mybatis.dynamic.sql.delete

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.common.CommonBuilder
import org.mybatis.dynamic.sql.common.OrderByModel
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.delete.render.DeleteRenderer
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.where.WhereModel
import java.util.Objects
import java.util.Optional
import java.util.function.Function

/**
 * delete 模型。
 */
class DeleteModel private constructor(builder: Builder) {
    private val table: SqlTable
    private val tableAlias: String?
    private val whereModel: WhereModel?
    private val limit: Long?
    private val orderByModel: OrderByModel?
    private val statementConfiguration: StatementConfiguration

    init {
        table = builder.table()
        statementConfiguration = builder.statementConfiguration()
        whereModel = builder.whereModel()
        tableAlias = builder.tableAlias()
        limit = builder.limit()
        orderByModel = builder.orderByModel()
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

    fun limit(): Optional<Long> {
        return Optional.ofNullable(limit)
    }

    fun orderByModel(): Optional<OrderByModel> {
        return Optional.ofNullable(orderByModel)
    }

    fun statementConfiguration(): StatementConfiguration {
        return statementConfiguration
    }

    fun render(renderingStrategy: RenderingStrategy): DeleteStatementProvider {
        return DeleteRenderer.withDeleteModel(this)
            .withRenderingStrategy(renderingStrategy)
            .build()
            .render()
    }

    fun <R> map(adapterFunction: Function<DeleteModel, R>): R {
        return adapterFunction.apply(this)
    }

    companion object {
        @JvmStatic
        fun withTable(table: SqlTable): Builder {
            return Builder().withTable(table)
        }
    }

    class Builder : CommonBuilder<Builder>() {

        override fun self(): Builder {
            return this
        }

        fun build(): DeleteModel {
            return DeleteModel(this)
        }

    }
}
