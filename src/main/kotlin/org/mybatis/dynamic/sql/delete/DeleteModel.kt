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
import org.mybatis.dynamic.sql.order.OrderByModel
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.where.WhereModel
import java.util.function.Function

/**
 * delete 模型。
 */
class DeleteModel @JvmOverloads constructor(
    private val table: SqlTable,
    private val statementConfiguration: StatementConfiguration,
    private val tableAlias: String? = null,
    private val whereModel: WhereModel? = null,
    private val limit: Long? = null,
    private val orderByModel: OrderByModel? = null,
) {

    fun table(): SqlTable {
        return table
    }

    fun tableAlias(): String? {
        return tableAlias
    }

    fun whereModel(): WhereModel? {
        return whereModel
    }

    fun limit(): Long? {
        return limit
    }

    fun orderByModel(): OrderByModel? {
        return orderByModel
    }

    fun statementConfiguration(): StatementConfiguration {
        return statementConfiguration
    }

    fun render(renderingStrategy: RenderingStrategy): DeleteStatementProvider {
        return DeleteRenderer(this, renderingStrategy).render()
    }

    fun <R> map(adapterFunction: Function<DeleteModel, R>): R {
        return adapterFunction.apply(this)
    }

}
