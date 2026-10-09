package org.mybatis.dynamic.sql.select

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy

class SelectRenderer(
    private val selectModel: SelectModel,
    private val renderingStrategy: RenderingStrategy
) {

    fun render(): SelectStatementProvider {
        val statementConfiguration = selectModel.statementConfiguration()
        val renderingContext = RenderingContext(renderingStrategy, statementConfiguration)
        val fragmentAndParameters = SubQueryRenderer(selectModel, renderingContext).render()
        return DefaultSelectStatementProvider(fragmentAndParameters.fragment(), fragmentAndParameters.parameters())
    }

}