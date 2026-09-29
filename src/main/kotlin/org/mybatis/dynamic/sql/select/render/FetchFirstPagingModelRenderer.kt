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

import org.mybatis.dynamic.sql.exception.InvalidSqlException
import org.mybatis.dynamic.sql.render.RenderedParameterInfo
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.select.PagingModel
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.InternalError
import org.mybatis.dynamic.sql.util.Messages

/**
 * fetch first 分页渲染器。
 */
class FetchFirstPagingModelRenderer(
    private val renderingContext: RenderingContext,
    private val pagingModel: PagingModel
) {

    fun render(): FragmentAndParameters {
        return pagingModel.offset()
            .map { offset: Long -> renderWithOffset(offset) }
            .orElseGet { renderFetchFirstRowsOnly() }
    }

    private fun renderWithOffset(offset: Long): FragmentAndParameters {
        return pagingModel.fetchFirstRows()
            .map { ffr: Long -> renderOffsetAndFetchFirstRows(offset, ffr) }
            .orElseGet { renderOffsetOnly(offset) }
    }

    private fun renderFetchFirstRowsOnly(): FragmentAndParameters {
        return pagingModel.fetchFirstRows().map { fetchFirstRows: Long -> renderFetchFirstRowsOnly(fetchFirstRows) }
            .orElseThrow {
                InvalidSqlException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_13))
            }
    }

    private fun renderFetchFirstRowsOnly(fetchFirstRows: Long): FragmentAndParameters {
        val fetchFirstParameterInfo = renderingContext.calculateFetchFirstRowsParameterInfo()
        return FragmentAndParameters
            .withFragment(
                "fetch first " + fetchFirstParameterInfo.renderedPlaceHolder //$NON-NLS-1$
                    + " rows only" //$NON-NLS-1$
            )
            .withParameter(fetchFirstParameterInfo.parameterMapKey, fetchFirstRows)
            .build()
    }

    private fun renderOffsetOnly(offset: Long): FragmentAndParameters {
        val offsetParameterInfo = renderingContext.calculateOffsetParameterInfo()
        return FragmentAndParameters.withFragment(
            "offset " + offsetParameterInfo.renderedPlaceHolder //$NON-NLS-1$
                + " rows" //$NON-NLS-1$
        )
            .withParameter(offsetParameterInfo.parameterMapKey, offset)
            .build()
    }

    private fun renderOffsetAndFetchFirstRows(offset: Long, fetchFirstRows: Long): FragmentAndParameters {
        val offsetParameterInfo = renderingContext.calculateOffsetParameterInfo()
        val fetchFirstParameterInfo = renderingContext.calculateFetchFirstRowsParameterInfo()
        return FragmentAndParameters.withFragment(
            "offset " + offsetParameterInfo.renderedPlaceHolder //$NON-NLS-1$
                + " rows fetch first " + fetchFirstParameterInfo.renderedPlaceHolder //$NON-NLS-1$
                + " rows only" //$NON-NLS-1$
        )
            .withParameter(offsetParameterInfo.parameterMapKey, offset)
            .withParameter(fetchFirstParameterInfo.parameterMapKey, fetchFirstRows)
            .build()
    }
}
