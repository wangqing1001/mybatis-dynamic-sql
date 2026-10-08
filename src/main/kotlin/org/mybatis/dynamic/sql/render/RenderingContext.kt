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
package org.mybatis.dynamic.sql.render

import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.util.StringUtilities
import java.util.Objects
import java.util.concurrent.atomic.AtomicInteger

/**
 * 此类封装了与渲染相关的所有支持项,并包含渲染过程中使用的许多实用方法。
 *
 * @since 1.5.1
 * @author Jeff Butler
 */
class RenderingContext @JvmOverloads constructor(
    private val renderingStrategy: RenderingStrategy,
    private val statementConfiguration: StatementConfiguration,
    private val tableAliasCalculator: TableAliasCalculator = TableAliasCalculator.empty(),
    private val sequence: AtomicInteger = AtomicInteger(1),
) {
    private fun nextMapKey(): String {
        return renderingStrategy.formatParameterMapKey(sequence)
    }

    private fun <T> renderedPlaceHolder(mapKey: String, column: BindableColumn<T>): String {
        var renderingStrategy = column.renderingStrategy()
        if (renderingStrategy == null) {
            renderingStrategy = this.renderingStrategy
        }
        return renderingStrategy.getFormattedJdbcPlaceholder(column, PARAMETER_NAME, mapKey)
    }

    fun calculateFetchFirstRowsParameterInfo(): RenderedParameterInfo {
        val mapKey = renderingStrategy.formatParameterMapKeyForFetchFirstRows(sequence)
        return RenderedParameterInfo(
            mapKey,
            renderingStrategy.getFormattedJdbcPlaceholderForPagingParameters(PARAMETER_NAME, mapKey)
        )
    }

    fun calculateLimitParameterInfo(): RenderedParameterInfo {
        val mapKey = renderingStrategy.formatParameterMapKeyForLimit(sequence)
        return RenderedParameterInfo(
            mapKey,
            renderingStrategy.getFormattedJdbcPlaceholderForPagingParameters(PARAMETER_NAME, mapKey)
        )
    }

    fun calculateOffsetParameterInfo(): RenderedParameterInfo {
        val mapKey = renderingStrategy.formatParameterMapKeyForOffset(sequence)
        return RenderedParameterInfo(
            mapKey,
            renderingStrategy.getFormattedJdbcPlaceholderForPagingParameters(PARAMETER_NAME, mapKey)
        )
    }

    fun <T> calculateParameterInfo(column: BindableColumn<T>): RenderedParameterInfo {
        val mapKey = nextMapKey()
        return RenderedParameterInfo(mapKey, renderedPlaceHolder(mapKey, column))
    }

    fun <T> aliasedColumnName(column: SqlColumn<T>): String {
        return tableAliasCalculator.aliasForColumn(column.table())
            ?.let { aliasedColumnName(column, it) } ?: column.name()
    }

    fun <T> aliasedColumnName(column: SqlColumn<T>, explicitAlias: String): String {
        return explicitAlias + "." + column.name()
    }

    fun aliasedTableName(table: SqlTable): String {
        return tableAliasCalculator.aliasForTable(table)
            ?.let { table.tableName() + StringUtilities.spaceBefore(it) }?:table.tableName()
    }

    fun isNonRenderingClauseAllowed(): Boolean {
        return statementConfiguration.nonRenderingWhereClauseAllowed()
    }

    fun withChildTableAliasCalculator(childTableAliasCalculator: TableAliasCalculator): RenderingContext {
        val tac = TableAliasCalculatorWithParent(tableAliasCalculator,childTableAliasCalculator)
        return RenderingContext(this.renderingStrategy,this.statementConfiguration,tac,this.sequence)
    }

    companion object {
        private const val PARAMETER_NAME = RenderingStrategy.DEFAULT_PARAMETER_PREFIX


    }
}
