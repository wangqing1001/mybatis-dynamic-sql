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
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.paging.PagingModel
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.util.toFragmentCollector
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

    fun <R> map(mapper: Function<SelectModel, R>): R {
        return mapper.apply(this)
    }

    fun render(renderingStrategy: RenderingStrategy): SelectStatementProvider {
        val renderingContext = RenderingContext(renderingStrategy, statementConfiguration)
        val fragmentAndParameters = render(renderingContext)
        val selectStatement = fragmentAndParameters.fragment()
        val parameters = fragmentAndParameters.parameters()
        return DefaultSelectStatementProvider(selectStatement, parameters)
    }

    fun render(renderingContext: RenderingContext,prefix: String = "",suffix: String = ""): FragmentAndParameters {
        val list = queryExpressions.map { it.render(renderingContext) }.toMutableList()
        val orderClause = orderByModel?.render(renderingContext)
        if(orderClause != null) {
            list.add(orderClause)
        }
        val pagingClause = pagingModel?.render(renderingContext)
        if(pagingClause != null) {
            list.add(pagingClause)
        }
        val forClause = forClause
        if(forClause != null) {
            list.add(FragmentAndParameters(forClause))
        }
        val waitClause = waitClause
        if(waitClause != null) {
            list.add(FragmentAndParameters(waitClause))
        }
        return list.toFragmentCollector().toFragmentAndParameters(" ",prefix,suffix)
    }

}
