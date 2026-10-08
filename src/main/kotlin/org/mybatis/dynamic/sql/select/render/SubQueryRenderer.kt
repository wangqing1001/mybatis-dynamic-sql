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
package org.mybatis.dynamic.sql.select.render

import org.mybatis.dynamic.sql.order.OrderByModel
import org.mybatis.dynamic.sql.order.OrderByRenderer
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.select.paging.PagingModel
import org.mybatis.dynamic.sql.select.QueryExpressionModel
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.paging.PagingModelRenderer
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.toFragmentCollector
import java.util.Objects


class SubQueryRenderer @JvmOverloads constructor(
    private val selectModel: SelectModel,
    private val renderingContext: RenderingContext,
    private val prefix: String = "",
    private val suffix: String = ""
) {

    fun render(): FragmentAndParameters {
        val list = selectModel.queryExpressions().map { renderQueryExpression(it) }.toMutableList()
        val orderModel = selectModel.orderByModel()
        if(orderModel != null) {
            list.add(renderOrderBy(orderModel))
        }
        val pagingModel = selectModel.pagingModel()
        if(pagingModel != null) {
            list.add(renderPagingModel(pagingModel))
        }
        val forClause = selectModel.forClause()
        if(forClause != null) {
            list.add(FragmentAndParameters(forClause))
        }
        val waitClause = selectModel.waitClause()
        if(waitClause != null) {
            list.add(FragmentAndParameters(waitClause))
        }
        return list.toFragmentCollector().toFragmentAndParameters(" ",prefix,suffix)
    }

    private fun renderQueryExpression(queryExpressionModel: QueryExpressionModel): FragmentAndParameters {
        return QueryExpressionRenderer(queryExpressionModel,renderingContext).render()
    }

    private fun renderOrderBy(orderByModel: OrderByModel): FragmentAndParameters {
        return OrderByRenderer(renderingContext).render(orderByModel)
    }

    private fun renderPagingModel(pagingModel: PagingModel): FragmentAndParameters {
        return PagingModelRenderer(pagingModel,renderingContext).render()
    }

}
