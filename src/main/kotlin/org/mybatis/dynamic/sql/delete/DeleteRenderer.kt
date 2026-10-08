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

import org.mybatis.dynamic.sql.order.OrderByRenderer
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.render.TableAliasCalculator
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.toFragmentCollector

/**
 * delete 渲染器。
 */
class DeleteRenderer(
    val deleteModel: DeleteModel,
    renderingStrategy: RenderingStrategy
) {
    val renderingContext: RenderingContext

    init {
        val tableAliasCalculator = deleteModel.tableAlias()?.let {
            ExplicitTableAliasCalculator(mapOf(deleteModel.table() to it))
        }?:TableAliasCalculator.empty()
        val statementConfiguration = deleteModel.statementConfiguration()
        renderingContext = RenderingContext(renderingStrategy,statementConfiguration,tableAliasCalculator)
    }

    fun render(): DeleteStatementProvider {
        val aliasedTableName = renderingContext.aliasedTableName(deleteModel.table())
        val start =  FragmentAndParameters("delete from $aliasedTableName")
        val list = mutableListOf(start)
        val whereClause = deleteModel.whereModel()?.render(renderingContext)
        if (whereClause != null) {
            list.add(whereClause)
        }
        val orderByClause = deleteModel.orderByModel()?.let { OrderByRenderer(renderingContext).render(it) }
        if (orderByClause != null) {
            list.add(orderByClause)
        }
        val limitClause = deleteModel.limit()?.let {
            val parameterInfo = renderingContext.calculateLimitParameterInfo()
            val fragment = "limit " + parameterInfo.renderedPlaceHolder
            val parameters = mapOf(parameterInfo.parameterMapKey to it)
            FragmentAndParameters(fragment,parameters )
        }
        if (limitClause != null) {
            list.add(limitClause)
        }
        val fragmentCollector = list.toFragmentCollector()
        val deleteStatement = fragmentCollector.collectFragments(" ")
        val parameters = fragmentCollector.parameters()
        return DefaultDeleteStatementProvider(deleteStatement,parameters)
    }

}
