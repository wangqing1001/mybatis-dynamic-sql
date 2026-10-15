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
package org.mybatis.dynamic.sql.filter

import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.where.WhereApplier

/**
 * [SQLFilterChain] 的默认实现。
 *
 * <p>实现为无状态折叠:每次调用都新建一次收集上下文,按**声明顺序**让所有过滤器贡献条件,
 * 再把贡献的条件整体作为一个 "and" 分组并入原始 where 条件。不持有任何可变游标,
 * 因此同一实例可安全地重复调用、并发调用,不会出现条件静默丢失。
 */
internal class DefaultSQLFilterChain(
    private val filters: List<SQLFilter>
) : SQLFilterChain {

    override fun filterWhere(where: WhereApplier, table: TableExpression): WhereApplier {
        val context = DefaultWhereFilterContext(table)
        filters.forEach { it.doWhere(context) }
        val contributed = context.toWhereApplier()
        if (contributed.isEffectivelyEmpty()) {
            return where
        }
        val andGroup = AndOrCriteriaGroup("and", contributed.initialCriterion(), contributed.subCriteria())
        return where.andThen { operations -> operations.addSubCriterion(andGroup) }
    }

    override fun filterGeneralInsert(columnMappings: List<AbstractColumnMapping>,table: SqlTable): List<AbstractColumnMapping> {
        return filterInsertOrUpdate(columnMappings,table){ filter, context -> filter.doGeneralInsert(context) }
    }

    override fun filterUpdate(columnMappings: List<AbstractColumnMapping>,table: SqlTable): List<AbstractColumnMapping> {
        return filterInsertOrUpdate(columnMappings,table){ filter, context -> filter.doUpdate(context) }
    }

    private fun filterInsertOrUpdate(columnMappings: List<AbstractColumnMapping>,table: SqlTable,consumer:(SQLFilter,ColumnFilterContext)->Unit):List<AbstractColumnMapping> {
        val context = DefaultColumnFilterContext(table)
        filters.forEach { consumer(it,context) }
        if (context.toColumnMappings().isEmpty()) {
            return columnMappings
        }
        val seenColumns = columnMappings.mapTo(mutableSetOf()) { it.columnName() }
        val contributed = mutableListOf<AbstractColumnMapping>()
        context.toColumnMappings().forEach {
            if (seenColumns.add(it.columnName())) {
                contributed.add(it)
            }
        }
        if (contributed.isEmpty()) {
            return columnMappings
        }
        return columnMappings + contributed
    }

}
