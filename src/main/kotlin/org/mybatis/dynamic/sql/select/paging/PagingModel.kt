package org.mybatis.dynamic.sql.select.paging

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters

class PagingModel @JvmOverloads constructor(
    private val limit: Long? = null,
    private val offset: Long? = null,
    private val fetchFirstRows: Long? = null
) {

    fun limit(): Long? {
        return limit
    }

    fun offset(): Long? {
        return offset
    }

    fun fetchFirstRows(): Long? {
        return fetchFirstRows
    }

    fun render(renderingContext: RenderingContext): FragmentAndParameters {
        val limit = limit ?: return FetchFirstAndOffsetModel(fetchFirstRows, fetchFirstRows).render(renderingContext)
        return LimitAndOffsetModel(limit,offset).render(renderingContext)
    }

}