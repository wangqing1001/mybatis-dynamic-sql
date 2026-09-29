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
class RenderingContext private constructor(builder: Builder) {
    private val renderingStrategy: RenderingStrategy
    private val sequence: AtomicInteger
    private val tableAliasCalculator: TableAliasCalculator
    private val statementConfiguration: StatementConfiguration

    init {
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy)
        tableAliasCalculator = Objects.requireNonNull(builder.tableAliasCalculator)
        statementConfiguration = Objects.requireNonNull(builder.statementConfiguration)
        sequence = builder.sequence ?: AtomicInteger(1)
    }

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
            .map { alias -> aliasedColumnName(column, alias) }
            .orElseGet { column.name() }
    }

    fun <T> aliasedColumnName(column: SqlColumn<T>, explicitAlias: String): String {
        return explicitAlias + "." + column.name() //$NON-NLS-1$
    }

    fun aliasedTableName(table: SqlTable): String {
        return tableAliasCalculator.aliasForTable(table)
            .map { a -> table.tableName() + StringUtilities.spaceBefore(a) }
            .orElseGet { table.tableName() }
    }

    fun isNonRenderingClauseAllowed(): Boolean {
        return statementConfiguration.nonRenderingWhereClauseAllowed()
    }

    /**
     * 基于此创建一个新的渲染上下文,表别名计算器被修改为包含指定的子表别名计算器。
     * 当查询表达式渲染器在渲染过程中别名计算器可能改变时,使用此方法。
     *
     * @param childTableAliasCalculator 子表别名计算器
     * @return 新的渲染上下文,其表别名计算器由原计算器作为父级和新的子计算器组合而成
     */
    fun withChildTableAliasCalculator(childTableAliasCalculator: TableAliasCalculator): RenderingContext {
        val tac = TableAliasCalculatorWithParent.Builder()
            .withParent(tableAliasCalculator)
            .withChild(childTableAliasCalculator)
            .build()

        return Builder()
            .withRenderingStrategy(this.renderingStrategy)
            .withSequence(this.sequence)
            .withTableAliasCalculator(tac)
            .withStatementConfiguration(statementConfiguration)
            .build()
    }

    class Builder {
        lateinit var renderingStrategy: RenderingStrategy
        var sequence: AtomicInteger? = null
        var tableAliasCalculator: TableAliasCalculator = TableAliasCalculator.empty()
        lateinit var statementConfiguration: StatementConfiguration

        fun withRenderingStrategy(renderingStrategy: RenderingStrategy): Builder {
            this.renderingStrategy = renderingStrategy
            return this
        }

        fun withSequence(sequence: AtomicInteger): Builder {
            this.sequence = sequence
            return this
        }

        fun withTableAliasCalculator(tableAliasCalculator: TableAliasCalculator): Builder {
            this.tableAliasCalculator = tableAliasCalculator
            return this
        }

        fun withStatementConfiguration(statementConfiguration: StatementConfiguration): Builder {
            this.statementConfiguration = statementConfiguration
            return this
        }

        fun build(): RenderingContext {
            return RenderingContext(this)
        }
    }

    companion object {
        private const val PARAMETER_NAME = RenderingStrategy.DEFAULT_PARAMETER_PREFIX

        @JvmStatic
        fun withRenderingStrategy(renderingStrategy: RenderingStrategy): Builder {
            return Builder().withRenderingStrategy(renderingStrategy)
        }
    }
}
