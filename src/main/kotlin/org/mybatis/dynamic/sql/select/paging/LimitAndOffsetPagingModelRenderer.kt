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
package org.mybatis.dynamic.sql.select.paging

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters

class LimitAndOffsetPagingModelRenderer(
    private val renderingContext: RenderingContext,
    private val limit: Long,
    private val pagingModel: PagingModel
) {

    fun render(): FragmentAndParameters {
        val offset = pagingModel.offset()?:return renderLimitOnly()
        return renderLimitAndOffset(offset)
    }

    private fun renderLimitOnly(): FragmentAndParameters {
        val limitParameterInfo = renderingContext.calculateLimitParameterInfo()
        val fragment = "limit ${limitParameterInfo.renderedPlaceHolder}"
        val parameters = mapOf(limitParameterInfo.parameterMapKey to limit)
        return FragmentAndParameters(fragment,parameters)
    }

    private fun renderLimitAndOffset(offset: Long): FragmentAndParameters {
        val limitParameterInfo = renderingContext.calculateLimitParameterInfo()
        val offsetParameterInfo = renderingContext.calculateOffsetParameterInfo()
        val fragment = "limit ${limitParameterInfo.renderedPlaceHolder} offset ${offsetParameterInfo.renderedPlaceHolder}"
        val parameters = mapOf(limitParameterInfo.parameterMapKey to limit,offsetParameterInfo.parameterMapKey to offset)
        return FragmentAndParameters(fragment, parameters)
    }

}
