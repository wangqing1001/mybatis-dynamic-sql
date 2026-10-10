package org.mybatis.dynamic.sql.insert.select

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.insert.DefaultGeneralInsertStatementProvider
import org.mybatis.dynamic.sql.insert.InsertRenderingUtilities
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.StringUtilities

/**
 * insert-select 模型。
 */
class InsertSelectModel @JvmOverloads constructor(
    private val table: SqlTable,
    private val selectModel: SelectModel,
    private val statementConfiguration: StatementConfiguration,
    private val columnList: InsertColumnListModel? = null
) {

    fun table(): SqlTable {
        return table
    }

    fun selectModel(): SelectModel {
        return selectModel
    }

    fun columnList(): InsertColumnListModel? {
        return columnList
    }

    fun statementConfiguration(): StatementConfiguration {
        return statementConfiguration
    }

    fun render(renderingStrategy: RenderingStrategy): InsertSelectStatementProvider {
        val renderingContext = RenderingContext(renderingStrategy, statementConfiguration)
        val statementStart = InsertRenderingUtilities.calculateInsertStatementStart(table)
        val prefix = statementStart + StringUtilities.spaceAfter(columnList?.render()?:"")
        val fragmentAndParameters = selectModel.render(renderingContext,prefix)
        val insertStatement = fragmentAndParameters.fragment()
        val parameters = fragmentAndParameters.parameters()
        return DefaultGeneralInsertStatementProvider(insertStatement,parameters)
    }

}