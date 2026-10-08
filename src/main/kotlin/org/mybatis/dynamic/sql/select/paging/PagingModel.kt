package org.mybatis.dynamic.sql.select.paging

class PagingModel @JvmOverloads constructor(
    private val limit: Long? = null,
    private val offset: Long? = null,
    private val fetchFirstRows: Long? = null,
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

}