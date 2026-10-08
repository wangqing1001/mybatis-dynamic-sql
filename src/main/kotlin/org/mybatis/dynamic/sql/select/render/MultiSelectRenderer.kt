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

import org.mybatis.dynamic.sql.order.OrderByRenderer
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.MultiSelectModel
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.UnionQuery
import org.mybatis.dynamic.sql.select.paging.PagingModelRenderer
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.FragmentCollector
import org.mybatis.dynamic.sql.util.toFragmentCollector

class MultiSelectRenderer(
    private val multiSelectModel: MultiSelectModel,
    renderingStrategy: RenderingStrategy
) {

    private val renderingContext: RenderingContext = RenderingContext(renderingStrategy,multiSelectModel.statementConfiguration())

    fun render(): SelectStatementProvider {
        val initialSelect = renderSelect(multiSelectModel.initialSelect())
        val list = multiSelectModel.unionQueries().map { renderSelect(it) }.toMutableList()
        val orderBy = renderOrderBy()
        if(orderBy != null) {
            list.add(orderBy)
        }
        val paging = renderPagingModel()
        if(paging != null) {
            list.add(paging)
        }
        return toSelectStatementProvider(list.toFragmentCollector(initialSelect))
    }

    private fun toSelectStatementProvider(fragmentCollector: FragmentCollector): SelectStatementProvider {
        return DefaultSelectStatementProvider
            .withSelectStatement(fragmentCollector.collectFragments(" "))
            .withParameters(fragmentCollector.parameters())
            .build()
    }

    private fun renderSelect(selectModel: SelectModel): FragmentAndParameters {
        return SubQueryRenderer(selectModel,renderingContext,"(",")").render()
    }

    private fun renderSelect(unionQuery: UnionQuery): FragmentAndParameters {
        return SubQueryRenderer(unionQuery.selectModel,renderingContext,"${unionQuery.connector} (",")").render()
    }

    private fun renderOrderBy(): FragmentAndParameters? {
        val orderByModel = multiSelectModel.orderByModel()?:return null
        return OrderByRenderer(renderingContext).render(orderByModel)
    }

    private fun renderPagingModel(): FragmentAndParameters? {
        val pagingModel = multiSelectModel.pagingModel()?:return null
        return PagingModelRenderer(pagingModel,renderingContext).render()
    }

}
