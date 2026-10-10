package org.mybatis.dynamic.sql.select.paging

import org.mybatis.dynamic.sql.exception.InvalidSqlException
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.InternalError
import org.mybatis.dynamic.sql.util.Messages

class FetchFirstAndOffsetModel(
    private val offset: Long? = null,
    private val fetchFirstRows: Long? = null
) {

    init {
        if(offset ==null && fetchFirstRows == null) {
            throw InvalidSqlException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_13))
        }
    }

    fun offset(): Long? {
        return offset
    }

    fun fetchFirstRows(): Long? {
        return fetchFirstRows
    }

    fun render(renderingContext: RenderingContext): FragmentAndParameters {
        val offset = offset ?: return renderFetchFirstRowsOnly(renderingContext)
        return renderWithOffset(offset,renderingContext)
    }

    private fun renderWithOffset(offset: Long,renderingContext: RenderingContext): FragmentAndParameters {
        val fetchFirstRows = fetchFirstRows ?: return renderOffsetOnly(offset,renderingContext)
        return renderOffsetAndFetchFirstRows(offset, fetchFirstRows,renderingContext)
    }

    private fun renderFetchFirstRowsOnly(renderingContext: RenderingContext): FragmentAndParameters {
        val fetchFirstRows = fetchFirstRows() ?: throw InvalidSqlException(Messages.getInternalErrorString(
            InternalError.INTERNAL_ERROR_13))
        return renderFetchFirstRowsOnly(fetchFirstRows,renderingContext)
    }

    private fun renderFetchFirstRowsOnly(fetchFirstRows: Long,renderingContext: RenderingContext): FragmentAndParameters {
        val fetchFirstParameterInfo = renderingContext.calculateFetchFirstRowsParameterInfo()
        val fragment = "fetch first ${fetchFirstParameterInfo.renderedPlaceHolder} rows only"
        val parameters = mapOf(fetchFirstParameterInfo.parameterMapKey to fetchFirstRows)
        return FragmentAndParameters(fragment,parameters)
    }

    private fun renderOffsetOnly(offset: Long,renderingContext: RenderingContext): FragmentAndParameters {
        val offsetParameterInfo = renderingContext.calculateOffsetParameterInfo()
        val fragment = "offset ${offsetParameterInfo.renderedPlaceHolder} rows"
        val parameters = mapOf(offsetParameterInfo.parameterMapKey to offset)
        return FragmentAndParameters(fragment,parameters)
    }

    private fun renderOffsetAndFetchFirstRows(offset: Long, fetchFirstRows: Long,renderingContext: RenderingContext): FragmentAndParameters {
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