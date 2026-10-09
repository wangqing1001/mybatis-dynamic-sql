package org.mybatis.dynamic.sql.select

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator
import org.mybatis.dynamic.sql.render.GuaranteedTableAliasCalculator
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.TableAliasCalculator
import org.mybatis.dynamic.sql.select.group.GroupByRenderer
import org.mybatis.dynamic.sql.select.having.HavingRenderer
import org.mybatis.dynamic.sql.select.join.JoinRenderer
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.StringUtilities
import org.mybatis.dynamic.sql.util.toFragmentCollector

/**
 * 查询表达式渲染器。
 */
class QueryExpressionRenderer(
    private val queryExpression: QueryExpressionModel,
    renderingContext: RenderingContext
) {

    private val tableExpressionRenderer: TableExpressionRenderer
    private val renderingContext: RenderingContext

    init {
        val childTableAliasCalculator = calculateChildTableAliasCalculator(queryExpression)
        this.renderingContext = renderingContext.withChildTableAliasCalculator(childTableAliasCalculator)
        tableExpressionRenderer = TableExpressionRenderer(this.renderingContext)
    }


    private fun calculateChildTableAliasCalculator(queryExpression: QueryExpressionModel): TableAliasCalculator {
        return queryExpression.joinModel()?.containsSubQueries()?.let { calculateTableAliasCalculatorWithJoins(it) }
            ?:explicitTableAliasCalculator()
    }

    private fun calculateTableAliasCalculatorWithJoins(hasSubQueries: Boolean): TableAliasCalculator {
        return if (hasSubQueries) {
            explicitTableAliasCalculator()
        } else {
            guaranteedTableAliasCalculator()
        }
    }

    private fun explicitTableAliasCalculator(): TableAliasCalculator {
        return ExplicitTableAliasCalculator(queryExpression.tableAliases())
    }

    private fun guaranteedTableAliasCalculator(): TableAliasCalculator {
        return GuaranteedTableAliasCalculator(queryExpression.tableAliases())
    }

    fun render(): FragmentAndParameters {
        val list = mutableListOf(renderQueryExpressionStart())
        val joinClause = calculateJoinClause()
        if(joinClause!=null) {
            list.add(joinClause)
        }
        val whereClause = calculateWhereClause()
        if(whereClause!=null) {
            list.add(whereClause)
        }
        val groupByClause = calculateGroupByClause()
        if(groupByClause!=null) {
            list.add(groupByClause)
        }
        val havingClause = calculateHavingClause()
        if(havingClause!=null) {
            list.add(havingClause)
        }
        return list.toFragmentCollector().toFragmentAndParameters(" ")
    }

    private fun renderQueryExpressionStart(): FragmentAndParameters {
        val columns = renderColumns()
        val table = renderTableExpression()
        val start = queryExpression.connector()?.let { StringUtilities.spaceAfter(it) } ?: ""
        val distinct = if(queryExpression.isDistinct()) "distinct " else ""
        val startSQL = start + "select " + distinct + columns.fragment()+ " from " + table.fragment()
        val parameters = table.parameters() + columns.parameters()
        return FragmentAndParameters(startSQL, parameters)
    }

    private fun renderColumns(): FragmentAndParameters {
        return queryExpression.columns().map { renderColumn(it) }
            .toFragmentCollector()
            .toFragmentAndParameters(", ")
    }

    private fun renderColumn(column: BasicColumn): FragmentAndParameters {
        val renderedColumn = column.render(renderingContext)
        val alias = column.alias() ?: return renderedColumn
        return renderedColumn.mapFragment {  "$it as $alias" }
    }

    private fun renderTableExpression(): FragmentAndParameters {
        return queryExpression.table().accept(tableExpressionRenderer)
    }

    private fun calculateJoinClause(): FragmentAndParameters? {
        val joinModel = queryExpression.joinModel() ?: return null
        return JoinRenderer(joinModel, tableExpressionRenderer, renderingContext).render()
    }

    private fun calculateWhereClause(): FragmentAndParameters? {
        return queryExpression.whereModel()?.render(renderingContext)
    }

    private fun calculateGroupByClause(): FragmentAndParameters? {
        val groupByModel = queryExpression.groupByModel() ?:return null
        return GroupByRenderer(groupByModel, renderingContext).render()
    }

    private fun calculateHavingClause(): FragmentAndParameters? {
        val havingModel = queryExpression.havingModel() ?: return null
        return HavingRenderer(havingModel, renderingContext).render()
    }

}