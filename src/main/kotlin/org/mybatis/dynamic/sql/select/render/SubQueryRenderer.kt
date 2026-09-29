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
import org.mybatis.dynamic.sql.select.PagingModel
import org.mybatis.dynamic.sql.select.QueryExpressionModel
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.FragmentCollector
import java.util.Objects
import java.util.stream.Collectors

/**
 * 子查询渲染器,负责将 SelectModel 渲染为子查询片段。
 */
class SubQueryRenderer private constructor(builder: Builder) {
    private val selectModel: SelectModel
    private val renderingContext: RenderingContext
    private val prefix: String
    private val suffix: String

    init {
        selectModel = Objects.requireNonNull(builder.selectModel!!)
        renderingContext = Objects.requireNonNull(builder.renderingContext!!)
        prefix = if (builder.prefix == null) "" else builder.prefix!! //$NON-NLS-1$
        suffix = if (builder.suffix == null) "" else builder.suffix!! //$NON-NLS-1$
    }

    fun render(): FragmentAndParameters {
        val fragmentCollector = selectModel
            .queryExpressions()
            .map { queryExpressionModel: QueryExpressionModel -> renderQueryExpression(queryExpressionModel) }
            .collect(FragmentCollector.collect())

        selectModel.orderByModel()
            .map { orderByModel: OrderByModel -> renderOrderBy(orderByModel) }
            .ifPresent { fragmentCollector.add(it) }

        selectModel.pagingModel()
            .map { pagingModel: PagingModel -> renderPagingModel(pagingModel) }
            .ifPresent { fragmentCollector.add(it) }

        selectModel.forClause()
            .map { forClause: String -> FragmentAndParameters.fromFragment(forClause) }
            .ifPresent { fragmentCollector.add(it) }

        selectModel.waitClause()
            .map { waitClause: String -> FragmentAndParameters.fromFragment(waitClause) }
            .ifPresent { fragmentCollector.add(it) }

        return fragmentCollector.toFragmentAndParameters(Collectors.joining(" ", prefix, suffix)) //$NON-NLS-1$
    }

    private fun renderQueryExpression(queryExpressionModel: QueryExpressionModel): FragmentAndParameters {
        return QueryExpressionRenderer.withQueryExpression(queryExpressionModel)
            .withRenderingContext(renderingContext)
            .build()
            .render()
    }

    private fun renderOrderBy(orderByModel: OrderByModel): FragmentAndParameters {
        return OrderByRenderer(renderingContext).render(orderByModel)
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
        fun withSelectModel(selectModel: SelectModel): Builder {
            return Builder().withSelectModel(selectModel)
        }
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        var selectModel: SelectModel? = null
        var renderingContext: RenderingContext? = null
        var prefix: String? = null
        var suffix: String? = null

        fun withRenderingContext(renderingContext: RenderingContext): Builder {
            this.renderingContext = renderingContext
            return this
        }

        fun withSelectModel(selectModel: SelectModel): Builder {
            this.selectModel = selectModel
            return this
        }

        fun withPrefix(prefix: String): Builder {
            this.prefix = prefix
            return this
        }

        fun withSuffix(suffix: String): Builder {
            this.suffix = suffix
            return this
        }

        fun build(): SubQueryRenderer {
            return SubQueryRenderer(this)
        }
    }
}
