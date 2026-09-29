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

import org.mybatis.dynamic.sql.common.OrderByModel
import org.mybatis.dynamic.sql.common.OrderByRenderer
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.MultiSelectModel
import org.mybatis.dynamic.sql.select.PagingModel
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.UnionQuery
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.FragmentCollector
import java.util.Objects
import java.util.Optional
import java.util.stream.Collectors

/**
 * 多 select 渲染器,渲染 union 查询。
 */
class MultiSelectRenderer private constructor(builder: Builder) {
    private val multiSelectModel: MultiSelectModel
    private val renderingContext: RenderingContext

    init {
        multiSelectModel = Objects.requireNonNull(builder.multiSelectModel!!)
        renderingContext = RenderingContext
            .withRenderingStrategy(Objects.requireNonNull(builder.renderingStrategy!!))
            .withStatementConfiguration(multiSelectModel.statementConfiguration())
            .build()
    }

    fun render(): SelectStatementProvider {
        val initialSelect = renderSelect(multiSelectModel.initialSelect())

        val fragmentCollector = multiSelectModel
            .unionQueries()
            .map { unionQuery: UnionQuery -> renderSelect(unionQuery) }
            .collect(FragmentCollector.collect(initialSelect))

        renderOrderBy().ifPresent { fragmentCollector.add(it) }
        renderPagingModel().ifPresent { fragmentCollector.add(it) }

        return toSelectStatementProvider(fragmentCollector)
    }

    private fun toSelectStatementProvider(fragmentCollector: FragmentCollector): SelectStatementProvider {
        return DefaultSelectStatementProvider
            .withSelectStatement(fragmentCollector.collectFragments(Collectors.joining(" "))) //$NON-NLS-1$
            .withParameters(fragmentCollector.parameters())
            .build()
    }

    private fun renderSelect(selectModel: SelectModel): FragmentAndParameters {
        return SubQueryRenderer.withSelectModel(selectModel)
            .withRenderingContext(renderingContext)
            .withPrefix("(") //$NON-NLS-1$
            .withSuffix(")") //$NON-NLS-1$
            .build()
            .render()
    }

    private fun renderSelect(unionQuery: UnionQuery): FragmentAndParameters {
        return SubQueryRenderer.withSelectModel(unionQuery.selectModel)
            .withRenderingContext(renderingContext)
            .withPrefix(unionQuery.connector+ " (") //$NON-NLS-1$
            .withSuffix(")") //$NON-NLS-1$
            .build()
            .render()
    }

    private fun renderOrderBy(): Optional<FragmentAndParameters> {
        return multiSelectModel.orderByModel().map { orderByModel: OrderByModel -> renderOrderBy(orderByModel) }
    }

    private fun renderOrderBy(orderByModel: OrderByModel): FragmentAndParameters {
        return OrderByRenderer(renderingContext).render(orderByModel)
    }

    private fun renderPagingModel(): Optional<FragmentAndParameters> {
        return multiSelectModel.pagingModel().map { pagingModel: PagingModel -> renderPagingModel(pagingModel) }
    }

    private fun renderPagingModel(pagingModel: PagingModel): FragmentAndParameters {
        return PagingModelRenderer.Builder()
            .withPagingModel(pagingModel)
            .withRenderingContext(renderingContext)
            .build()
            .render()
    }

    companion object {
        @JvmStatic
        fun withMultiSelectModel(multiSelectModel: MultiSelectModel): Builder {
            return Builder().withMultiSelectModel(multiSelectModel)
        }
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        var renderingStrategy: RenderingStrategy? = null
        var multiSelectModel: MultiSelectModel? = null

        fun withRenderingStrategy(renderingStrategy: RenderingStrategy): Builder {
            this.renderingStrategy = renderingStrategy
            return this
        }

        fun withMultiSelectModel(multiSelectModel: MultiSelectModel): Builder {
            this.multiSelectModel = multiSelectModel
            return this
        }

        fun build(): MultiSelectRenderer {
            return MultiSelectRenderer(this)
        }
    }
}
