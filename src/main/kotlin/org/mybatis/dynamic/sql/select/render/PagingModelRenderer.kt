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

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.select.PagingModel
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import java.util.Objects

/**
 * 分页模型渲染器,根据是否有 limit 选择 limit/offset 或 fetch first 渲染方式。
 */
class PagingModelRenderer private constructor(builder: Builder) {
    private val pagingModel: PagingModel
    private val renderingContext: RenderingContext

    init {
        renderingContext = Objects.requireNonNull(builder.renderingContext!!)
        pagingModel = Objects.requireNonNull(builder.pagingModel!!)
    }

    fun render(): FragmentAndParameters {
        return pagingModel.limit().map { limit: Long -> limitAndOffsetRender(limit) }
            .orElseGet { fetchFirstRender() }
    }

    private fun limitAndOffsetRender(limit: Long): FragmentAndParameters {
        return LimitAndOffsetPagingModelRenderer(renderingContext, limit, pagingModel).render()
    }

    private fun fetchFirstRender(): FragmentAndParameters {
        return FetchFirstPagingModelRenderer(renderingContext, pagingModel).render()
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        var pagingModel: PagingModel? = null
        var renderingContext: RenderingContext? = null

        fun withRenderingContext(renderingContext: RenderingContext): Builder {
            this.renderingContext = renderingContext
            return this
        }

        fun withPagingModel(pagingModel: PagingModel): Builder {
            this.pagingModel = pagingModel
            return this
        }

        fun build(): PagingModelRenderer {
            return PagingModelRenderer(this)
        }
    }
}
