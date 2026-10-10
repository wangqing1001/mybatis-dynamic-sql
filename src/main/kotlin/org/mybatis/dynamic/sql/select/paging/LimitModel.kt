package org.mybatis.dynamic.sql.select.paging

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters

open class LimitModel(private val limit: Long) {

    fun limit(): Long {
        return limit
    }

    open fun render(renderingContext: RenderingContext): FragmentAndParameters{
        val limitParameterInfo = renderingContext.calculateLimitParameterInfo()
        val fragment = "limit ${limitParameterInfo.renderedPlaceHolder}"
        val parameters = mapOf(limitParameterInfo.parameterMapKey to limit)
        return FragmentAndParameters(fragment,parameters)
    }

}