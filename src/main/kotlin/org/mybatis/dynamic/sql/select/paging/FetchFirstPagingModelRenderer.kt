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

import org.mybatis.dynamic.sql.exception.InvalidSqlException
import org.mybatis.dynamic.sql.render.RenderingContext
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
        val offset = pagingModel.offset()?: return renderFetchFirstRowsOnly()
        return renderWithOffset(offset)
    }

    private fun renderWithOffset(offset: Long): FragmentAndParameters {
        val fetchFirstRows = pagingModel.fetchFirstRows() ?: return renderOffsetOnly(offset)
        return renderOffsetAndFetchFirstRows(offset, fetchFirstRows)
    }

    private fun renderFetchFirstRowsOnly(): FragmentAndParameters {
        val fetchFirstRows = pagingModel.fetchFirstRows()?: throw InvalidSqlException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_13))
        return renderFetchFirstRowsOnly(fetchFirstRows)
    }

    private fun renderFetchFirstRowsOnly(fetchFirstRows: Long): FragmentAndParameters {
        val fetchFirstParameterInfo = renderingContext.calculateFetchFirstRowsParameterInfo()
        val fragment = "fetch first ${fetchFirstParameterInfo.renderedPlaceHolder} rows only"
        val parameters = mapOf(fetchFirstParameterInfo.parameterMapKey to fetchFirstRows)
        return FragmentAndParameters(fragment,parameters)
    }

    private fun renderOffsetOnly(offset: Long): FragmentAndParameters {
        val offsetParameterInfo = renderingContext.calculateOffsetParameterInfo()
        val fragment = "offset ${offsetParameterInfo.renderedPlaceHolder} rows"
        val parameters = mapOf(offsetParameterInfo.parameterMapKey to offset)
        return FragmentAndParameters(fragment,parameters)
    }

    private fun renderOffsetAndFetchFirstRows(offset: Long, fetchFirstRows: Long): FragmentAndParameters {
        val offsetParameterInfo = renderingContext.calculateOffsetParameterInfo()
        val fetchFirstParameterInfo = renderingContext.calculateFetchFirstRowsParameterInfo()
        val sql = "offset ${offsetParameterInfo.renderedPlaceHolder} rows fetch first ${fetchFirstParameterInfo.renderedPlaceHolder} rows only"
        val parameters = mapOf(
            offsetParameterInfo.parameterMapKey to offset,
            fetchFirstParameterInfo.parameterMapKey to fetchFirstRows
        )
        return FragmentAndParameters(sql, parameters)
    }
}
