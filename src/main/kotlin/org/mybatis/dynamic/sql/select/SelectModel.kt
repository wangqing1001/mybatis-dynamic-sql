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
package org.mybatis.dynamic.sql.select

import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.order.OrderByModel
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.paging.PagingModel
import org.mybatis.dynamic.sql.select.render.SelectRenderer
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.util.Validator
import java.util.function.Function

/**
 * select 语句模型。
 */
class SelectModel @JvmOverloads constructor(
    private val queryExpressions: List<QueryExpressionModel>,
    statementConfiguration: StatementConfiguration,
    private val forClause: String? = null,
    private val waitClause: String? = null,
    orderByModel: OrderByModel? = null,
    pagingModel: PagingModel? = null,
) : AbstractSelectModel(orderByModel,pagingModel,statementConfiguration) {

    init {
        Validator.assertNotEmpty(queryExpressions, "ERROR.14") //$NON-NLS-1$
    }

    fun queryExpressions(): List<QueryExpressionModel> {
        return queryExpressions
    }

    fun forClause(): String? {
        return forClause
    }

    fun waitClause(): String? {
        return waitClause
    }

    fun render(renderingStrategy: RenderingStrategy): SelectStatementProvider {
        return SelectRenderer(this,renderingStrategy).render()
    }

    fun <R> map(mapper: Function<SelectModel, R>): R {
        return mapper.apply(this)
    }

}
