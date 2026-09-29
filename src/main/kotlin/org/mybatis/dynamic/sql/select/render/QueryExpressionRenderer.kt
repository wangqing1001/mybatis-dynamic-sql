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
import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator
import org.mybatis.dynamic.sql.render.GuaranteedTableAliasCalculator
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.TableAliasCalculator
import org.mybatis.dynamic.sql.select.GroupByModel
import org.mybatis.dynamic.sql.select.HavingModel
import org.mybatis.dynamic.sql.select.QueryExpressionModel
import org.mybatis.dynamic.sql.select.join.JoinModel
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.FragmentCollector
import org.mybatis.dynamic.sql.util.StringUtilities
import org.mybatis.dynamic.sql.where.WhereModel
import java.util.Objects
import java.util.Optional
import java.util.stream.Collectors

/**
 * 查询表达式渲染器。
 */
class QueryExpressionRenderer private constructor(builder: Builder) {
    private val queryExpression: QueryExpressionModel
    private val tableExpressionRenderer: TableExpressionRenderer
    private val renderingContext: RenderingContext

    init {
        queryExpression = Objects.requireNonNull(builder.queryExpression!!)
        val childTableAliasCalculator = calculateChildTableAliasCalculator(queryExpression)

        renderingContext = Objects.requireNonNull(builder.renderingContext!!)
            .withChildTableAliasCalculator(childTableAliasCalculator)

        tableExpressionRenderer = TableExpressionRenderer.Builder()
            .withRenderingContext(renderingContext)
            .build()
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
        return queryExpression.joinModel()
            .map { joinModel: JoinModel -> joinModel.containsSubQueries() }
            .map { hasSubQueries: Boolean -> calculateTableAliasCalculatorWithJoins(hasSubQueries) }
            .orElseGet { explicitTableAliasCalculator() }
    }

    private fun calculateTableAliasCalculatorWithJoins(hasSubQueries: Boolean): TableAliasCalculator {
        return if (hasSubQueries) {
            // 如果有子查询,我们不能自动使用表名,因此必须指定所有别名
            explicitTableAliasCalculator()
        } else {
            // 没有子查询时,我们可以自动使用表名作为别名
            guaranteedTableAliasCalculator()
        }
    }

    private fun explicitTableAliasCalculator(): TableAliasCalculator {
        return ExplicitTableAliasCalculator.of(queryExpression.tableAliases())
    }

    private fun guaranteedTableAliasCalculator(): TableAliasCalculator {
        return GuaranteedTableAliasCalculator.of(queryExpression.tableAliases())
    }

    fun render(): FragmentAndParameters {
        val fragmentCollector = FragmentCollector()

        fragmentCollector.add(calculateQueryExpressionStart())
        calculateJoinClause().ifPresent { fragmentCollector.add(it) }
        calculateWhereClause().ifPresent { fragmentCollector.add(it) }
        calculateGroupByClause().ifPresent { fragmentCollector.add(it) }
        calculateHavingClause().ifPresent { fragmentCollector.add(it) }

        return fragmentCollector.toFragmentAndParameters(Collectors.joining(" ")) //$NON-NLS-1$
    }

    private fun calculateQueryExpressionStart(): FragmentAndParameters {
        val columnList = calculateColumnList()
        var start = queryExpression.connector().map { connector: String -> StringUtilities.spaceAfter(connector) }
            .orElse("") + "select " + (if (queryExpression.isDistinct()) "distinct " else "") + columnList.fragment()+ " from "

        val renderedTable = renderTableExpression(queryExpression.table())
        start += renderedTable.fragment()

        return FragmentAndParameters.withFragment(start)
            .withParameters(renderedTable.parameters())
            .withParameters(columnList.parameters())
            .build()
    }

    private fun calculateColumnList(): FragmentAndParameters {
        return queryExpression.columns()
            .map { selectListItem: BasicColumn -> renderColumnAndAlias(selectListItem) }
            .collect(FragmentCollector.collect())
            .toFragmentAndParameters(Collectors.joining(", ")) //$NON-NLS-1$
    }

    private fun renderColumnAndAlias(selectListItem: BasicColumn): FragmentAndParameters {
        val renderedColumn = selectListItem.render(renderingContext)
        val alias = selectListItem.alias()
        if (alias == null) {
            return renderedColumn
        }
        return renderedColumn.mapFragment { f: String -> "$f as $alias" }
    }

    private fun renderTableExpression(table: TableExpression): FragmentAndParameters {
        return table.accept(tableExpressionRenderer)
    }

    private fun calculateJoinClause(): Optional<FragmentAndParameters> {
        return queryExpression.joinModel().map { joinModel: JoinModel -> renderJoin(joinModel) }
    }

    private fun renderJoin(joinModel: JoinModel): FragmentAndParameters {
        return JoinRenderer.withJoinModel(joinModel)
            .withTableExpressionRenderer(tableExpressionRenderer)
            .withRenderingContext(renderingContext)
            .build()
            .render()
    }

    private fun calculateWhereClause(): Optional<FragmentAndParameters> {
        return queryExpression.whereModel().flatMap { whereModel: WhereModel -> renderWhereClause(whereModel) }
    }

    private fun renderWhereClause(whereModel: WhereModel): Optional<FragmentAndParameters> {
        return whereModel.render(renderingContext)
    }

    private fun calculateGroupByClause(): Optional<FragmentAndParameters> {
        return queryExpression.groupByModel().map { groupByModel: GroupByModel -> renderGroupBy(groupByModel) }
    }

    private fun renderGroupBy(groupByModel: GroupByModel): FragmentAndParameters {
        return groupByModel.columns()
            .map { column: BasicColumn -> renderColumn(column) }
            .collect(FragmentCollector.collect())
            .toFragmentAndParameters(
                Collectors.joining(", ", "group by ", "")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private fun renderColumn(column: BasicColumn): FragmentAndParameters {
        return column.render(renderingContext)
    }

    private fun calculateHavingClause(): Optional<FragmentAndParameters> {
        return queryExpression.havingModel().flatMap { havingModel: HavingModel -> renderHavingClause(havingModel) }
    }

    private fun renderHavingClause(havingModel: HavingModel): Optional<FragmentAndParameters> {
        return HavingRenderer.withHavingModel(havingModel)
            .withRenderingContext(renderingContext)
            .build()
            .render()
    }

    companion object {
        @JvmStatic
        fun withQueryExpression(model: QueryExpressionModel): Builder {
            return Builder().withQueryExpression(model)
        }
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        var queryExpression: QueryExpressionModel? = null
        var renderingContext: RenderingContext? = null

        fun withRenderingContext(renderingContext: RenderingContext): Builder {
            this.renderingContext = renderingContext
            return this
        }

        fun withQueryExpression(queryExpression: QueryExpressionModel): Builder {
            this.queryExpression = queryExpression
            return this
        }

        fun build(): QueryExpressionRenderer {
            return QueryExpressionRenderer(this)
        }
    }
}
