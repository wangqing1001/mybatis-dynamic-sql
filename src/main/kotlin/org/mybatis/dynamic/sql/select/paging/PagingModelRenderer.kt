package org.mybatis.dynamic.sql.select.paging

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import java.util.Objects

/**
 * 分页模型渲染器,根据是否有 limit 选择 limit/offset 或 fetch first 渲染方式。
 */
class PagingModelRenderer(
    private val pagingModel: PagingModel,
    private val renderingContext: RenderingContext
) {

    fun render(): FragmentAndParameters {
        val limit = pagingModel.limit() ?: return fetchFirstRender()
        return limitAndOffsetRender(limit)
    }

    private fun limitAndOffsetRender(limit: Long): FragmentAndParameters {
        return LimitAndOffsetPagingModelRenderer(renderingContext, limit, pagingModel).render()
    }

    private fun fetchFirstRender(): FragmentAndParameters {
        return FetchFirstPagingModelRenderer(renderingContext, pagingModel).render()
    }

}