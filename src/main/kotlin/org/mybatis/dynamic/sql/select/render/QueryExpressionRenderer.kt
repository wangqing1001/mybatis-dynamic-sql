/*
 *    Copyright 2016-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.select.render

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator
import org.mybatis.dynamic.sql.render.GuaranteedTableAliasCalculator
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.TableAliasCalculator
import org.mybatis.dynamic.sql.select.GroupByModel
import org.mybatis.dynamic.sql.select.having.HavingModel
import org.mybatis.dynamic.sql.select.QueryExpressionModel
import org.mybatis.dynamic.sql.select.having.HavingRenderer
import org.mybatis.dynamic.sql.select.join.JoinModel
import org.mybatis.dynamic.sql.select.join.JoinRenderer
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.StringUtilities
import org.mybatis.dynamic.sql.util.toFragmentCollector
import java.util.Objects

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
        tableExpressionRenderer = TableExpressionRenderer(renderingContext)
    }

    /**
     * 此函数计算当前上下文中使用的表别名计算器。有几种可能性:这可能是顶层 select 语句的渲染器,
     * 也可能是 join 中表表达式的渲染器,或者是 where 条件中列到子查询的渲染器,
     * 或者是 where 子句 "exists" 条件中 select 语句的渲染器。
     *
     * <p>对于 where 子句中的条件,我们将有一个父表别名计算器。这将使外层 select 语句中的别名
     * 对该渲染器可见,以便在 where 子查询条件中可以使用别名表中的列,而无需重新指定别名。
     *
     * <p>另一个复杂之处在于,如果有 join 和子查询,我们计算别名的方式不同。情况如下:
     *
     * <ol>
     *     <li>如果没有 join,那么我们只使用用户显式设置的别名</li>
     *     <lI>如果有 join 和子查询,我们也只使用显式别名</lI>
     *     <li>如果有 join 但没有子查询,那么如果未指定显式别名,我们将自动使用表名作为别名</li>
     * </ol>
     *
     * @param queryExpression 要渲染的模型
     * @return 适合此上下文的表别名计算器
     */
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
        return FragmentAndParameters(startSQL,parameters)
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
        return groupByModel.columns()
            .map { it.render(renderingContext) }
            .toFragmentCollector()
            .toFragmentAndParameters(", ", "group by ", "")
    }

    private fun calculateHavingClause(): FragmentAndParameters? {
        val havingModel = queryExpression.havingModel() ?: return null
        return HavingRenderer(havingModel, renderingContext).render()
    }

}
