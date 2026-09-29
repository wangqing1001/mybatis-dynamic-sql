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

import org.mybatis.dynamic.sql.render.RenderedParameterInfo
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.select.PagingModel
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import java.util.Objects

/**
 * limit 与 offset 分页渲染器。
 */
class LimitAndOffsetPagingModelRenderer(
    private val renderingContext: RenderingContext,
    private val limit: Long,
    private val pagingModel: PagingModel
) {

    fun render(): FragmentAndParameters {
        return pagingModel.offset().map { offset: Long -> renderLimitAndOffset(offset) }
            .orElseGet { renderLimitOnly() }
    }

    private fun renderLimitOnly(): FragmentAndParameters {
        val limitParameterInfo = renderingContext.calculateLimitParameterInfo()
        return FragmentAndParameters.withFragment("limit " + limitParameterInfo.renderedPlaceHolder) //$NON-NLS-1$
            .withParameter(limitParameterInfo.parameterMapKey, limit)
            .build()
    }

    private fun renderLimitAndOffset(offset: Long): FragmentAndParameters {
        val limitParameterInfo = renderingContext.calculateLimitParameterInfo()
        val offsetParameterInfo = renderingContext.calculateOffsetParameterInfo()
        return FragmentAndParameters.withFragment(
            "limit " + limitParameterInfo.renderedPlaceHolder //$NON-NLS-1$
                + " offset " + offsetParameterInfo.renderedPlaceHolder //$NON-NLS-1$
        )
            .withParameter(limitParameterInfo.parameterMapKey, limit)
            .withParameter(offsetParameterInfo.parameterMapKey, offset)
            .build()
    }
}
