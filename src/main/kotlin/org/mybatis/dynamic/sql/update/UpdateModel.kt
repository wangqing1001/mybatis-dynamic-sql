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
import org.mybatis.dynamic.sql.order.OrderByModel
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.where.WhereModel
import java.util.function.Function

/**
 * update 模型。
 */
class UpdateModel @JvmOverloads constructor(
    private val table: SqlTable,
    private val columnMappings: List<AbstractColumnMapping>,
    private val statementConfiguration: StatementConfiguration,
    private val tableAlias: String? = null,
    private val whereModel: WhereModel? = null,
    private val orderByModel: OrderByModel? = null,
    private val limit: Long? = null

) {

    init {
        Validator.assertNotEmpty(columnMappings, "ERROR.17") //$NON-NLS-1$
    }

    fun table(): SqlTable {
        return table
    }

    fun tableAlias(): String? {
        return tableAlias
    }

    fun whereModel(): WhereModel? {
        return whereModel
    }

    fun columnMappings(): List<AbstractColumnMapping> {
        return columnMappings
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

    fun render(renderingStrategy: RenderingStrategy): UpdateStatementProvider {
        return UpdateRenderer(this,renderingStrategy).render()
    }

    fun <R> map(mapper: Function<UpdateModel, R>): R {
        return mapper.apply(this)
    }

}
