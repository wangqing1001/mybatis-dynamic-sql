package org.mybatis.dynamic.sql.insert.select

import org.mybatis.dynamic.sql.insert.DefaultGeneralInsertStatementProvider
import org.mybatis.dynamic.sql.insert.InsertRenderingUtilities
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.SubQueryRenderer
import org.mybatis.dynamic.sql.util.StringUtilities

/**
 * insert-select 渲染器。
 */
class InsertSelectRenderer(
    private val model: InsertSelectModel,
    renderingStrategy: RenderingStrategy
) {
    private val renderingContext: RenderingContext = RenderingContext(renderingStrategy, model.statementConfiguration())

    fun render(): InsertSelectStatementProvider {
        val statementStart = InsertRenderingUtilities.calculateInsertStatementStart(model.table())
        val columnsPhrase = calculateColumnsPhrase()
        val prefix = statementStart + StringUtilities.spaceAfter(columnsPhrase)
        val fragmentAndParameters = SubQueryRenderer(model.selectModel(), renderingContext, prefix).render()
        return DefaultGeneralInsertStatementProvider(
            fragmentAndParameters.fragment(),
            fragmentAndParameters.parameters()
        )
    }

    private fun calculateColumnsPhrase(): String {
        val columnList = model.columnList() ?: return ""
        return columnList.columns().joinToString(", ", " (", ")") { column -> column.name() }
    }

}