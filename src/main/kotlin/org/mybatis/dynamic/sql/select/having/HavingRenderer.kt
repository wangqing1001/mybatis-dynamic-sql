package org.mybatis.dynamic.sql.select.having

import org.mybatis.dynamic.sql.AbstractBooleanExpressionModel
import org.mybatis.dynamic.sql.AbstractBooleanExpressionRenderer
import org.mybatis.dynamic.sql.render.RenderingContext

/**
 * having 子句渲染器。
 */
class HavingRenderer(
    model: AbstractBooleanExpressionModel,
    renderingContext: RenderingContext
) : AbstractBooleanExpressionRenderer("having", model, renderingContext)