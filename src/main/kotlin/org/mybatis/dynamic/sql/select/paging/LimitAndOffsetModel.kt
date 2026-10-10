package org.mybatis.dynamic.sql.select.paging

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters

class LimitAndOffsetModel(
    val limit: Long,
    private val offset: Long? = null,
) : LimitModel(limit){

    override fun render(renderingContext: RenderingContext): FragmentAndParameters {
        val limitClause = super.render(renderingContext)
        val offset = offset?:return limitClause
        val offsetParameterInfo = renderingContext.calculateOffsetParameterInfo()
        val fragment = "offset ${offsetParameterInfo.renderedPlaceHolder}"
        val parameters = mapOf(offsetParameterInfo.parameterMapKey to offset)
        val offsetClause = FragmentAndParameters(fragment, parameters)
        return limitClause + "" + offsetClause
    }





}
