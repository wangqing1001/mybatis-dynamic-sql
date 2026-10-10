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
package org.mybatis.dynamic.sql.select

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator
import org.mybatis.dynamic.sql.render.GuaranteedTableAliasCalculator
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.TableAliasCalculator
import org.mybatis.dynamic.sql.select.group.GroupByModel
import org.mybatis.dynamic.sql.select.having.HavingModel
import org.mybatis.dynamic.sql.select.join.JoinModel
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.StringUtilities
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.util.toFragmentCollector
import org.mybatis.dynamic.sql.where.WhereModel


/**
 * 查询表达式模型。表示单个 select 查询的全部信息。
 */
class QueryExpressionModel @JvmOverloads constructor(
    private val table: TableExpression,
    private val selectList: List<BasicColumn> = emptyList(),
    private val whereModel: WhereModel? = null,
    private val tableAliases: Map<SqlTable, String> = emptyMap(),
    private val joinModel: JoinModel? = null,
    private val groupByModel: GroupByModel? = null,
    private val havingModel: HavingModel? = null,
    private val isDistinct: Boolean = false,
    private val connector: String? = null
) {

    init {
        Validator.assertNotEmpty(selectList, "ERROR.13") //$NON-NLS-1$
    }

    fun connector(): String? {
        return connector
    }

    fun isDistinct(): Boolean {
        return isDistinct
    }

    fun columns(): Collection<BasicColumn> {
        return selectList
    }

    fun table(): TableExpression {
        return table
    }

    fun tableAliases(): Map<SqlTable, String> {
        return tableAliases
    }

    fun whereModel(): WhereModel? {
        return whereModel
    }

    fun joinModel(): JoinModel? {
        return joinModel
    }

    fun groupByModel(): GroupByModel? {
        return groupByModel
    }

    fun havingModel(): HavingModel? {
        return havingModel
    }

    fun render(renderingContext0: RenderingContext): FragmentAndParameters{
        val childTableAliasCalculator = calculateChildTableAliasCalculator()
        val renderingContext = renderingContext0.withChildTableAliasCalculator(childTableAliasCalculator)
        val tableExpressionRenderer = TableExpressionRenderer(renderingContext)
        val list = mutableListOf(renderQueryExpressionStart(tableExpressionRenderer,renderingContext))
        val joinClause = joinModel?.render(tableExpressionRenderer, renderingContext)
        if(joinClause!=null) {
            list.add(joinClause)
        }
        val whereClause = whereModel?.render(renderingContext)
        if(whereClause!=null) {
            list.add(whereClause)
        }
        val groupByClause = groupByModel?.render(renderingContext)
        if(groupByClause!=null) {
            list.add(groupByClause)
        }
        val havingClause = havingModel?.render(renderingContext)
        if(havingClause!=null) {
            list.add(havingClause)
        }
        return list.toFragmentCollector().toFragmentAndParameters(" ")

    }



    private fun calculateChildTableAliasCalculator(): TableAliasCalculator {
        val hasSubQueries = joinModel?.containsSubQueries()?: return ExplicitTableAliasCalculator(tableAliases)
        return if (hasSubQueries) {
            ExplicitTableAliasCalculator(tableAliases)
        } else {
            GuaranteedTableAliasCalculator(tableAliases)
        }
    }

    private fun renderQueryExpressionStart(tableExpressionRenderer :TableExpressionRenderer,renderingContext: RenderingContext): FragmentAndParameters {
        val columns = renderColumns(renderingContext)
        val table = this.table.accept(tableExpressionRenderer)
        val start = connector?.let { StringUtilities.spaceAfter(it) } ?: ""
        val distinct = if(isDistinct) "distinct " else ""
        val startSQL = start + "select " + distinct + columns.fragment()+ " from " + table.fragment()
        val parameters = table.parameters() + columns.parameters()
        return FragmentAndParameters(startSQL, parameters)
    }

    private fun renderColumns(renderingContext: RenderingContext): FragmentAndParameters {
        return selectList.map {
            val renderedColumn = it.render(renderingContext)
            val alias = it.alias() ?: return renderedColumn
            renderedColumn.mapFragment {c-> "$c as $alias" }
        }.toFragmentCollector().toFragmentAndParameters(", ")
    }





}
