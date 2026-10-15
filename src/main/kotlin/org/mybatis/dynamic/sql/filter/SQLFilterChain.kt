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

import org.mybatis.dynamic.sql.NullCriterion
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.where.WhereApplier
import org.mybatis.dynamic.sql.where.WhereModel

/**
 * SQL 过滤器链。负责按顺序应用一组 [SQLFilter],把它们贡献的条件合并进原始 where 条件。
 *
 * <p>本接口的实现是**无状态**的:每次调用 [filter] 都会重新遍历全部过滤器,
 * 因此同一个链实例可以被安全地重复调用(多次 build、并发调用),不会出现条件丢失。
 *
 * <p>过滤器贡献的条件始终追加在原始条件之后,并以 "and" 连接;若过滤器贡献了多个条件,
 * 它们之间按各自声明的连接符(or/and)组合。
 */
interface SQLFilterChain {

    /**
     * 应用全部过滤器,返回合并后的 where 应用器。
     *
     * @param where 原始 where 条件
     * @param table 当前表表达式,供过滤器决策使用
     * @return 合并了所有过滤条件的 where 应用器;无过滤器时原样返回 [where]
     */
    fun filterWhere(where: WhereApplier, table: TableExpression): WhereApplier{
        return where
    }

    fun filterGeneralInsert(columnMappings: List<AbstractColumnMapping>,table: SqlTable):List<AbstractColumnMapping>{
        return columnMappings
    }

    fun filterUpdate(columnMappings: List<AbstractColumnMapping>,table: SqlTable):List<AbstractColumnMapping>{
        return columnMappings
    }

    companion object {

        private val NON_FILTER_CHAIN: SQLFilterChain = object : SQLFilterChain {}

        /**
         * 根据给定的过滤器数组创建过滤器链。
         *
         * 当 [filters] 为空时返回空过滤器链([NON_FILTER_CHAIN]);否则返回真实链实例。
         *
         * @param filters 参与过滤的过滤器集合(可省略,表示无需过滤)
         * @return 组装好的过滤器链
         */
        @JvmStatic
        fun createFilterChain(vararg filters: SQLFilter): SQLFilterChain {
            if (filters.isEmpty()) {
                return NON_FILTER_CHAIN
            }
            return DefaultSQLFilterChain(filters.toList())
        }

    }
}

/**
 * 构建带过滤器支持的 where 模型。这是所有语句 DSL 接入 SQL 过滤器的统一入口,
 * 保证 select / count / delete / update 对过滤器的处理语义完全一致。
 *
 * <p>处理流程:
 * <ol>
 *     <li>把原始 where 条件(可能不存在)规整为 [WhereApplier];</li>
 *     <li>当且仅当过滤器开关为 true 时,执行表上声明的过滤器链,把其贡献的条件
 *         并入原始条件;开关为 false 时完全跳过过滤器;</li>
 *     <li>按下方规则决定是否生成 [WhereModel]。</li>
 * </ol>
 *
 * <p>关于"空"的处理,需要区分两种情形,否则会破坏既有的校验行为:
 * <ul>
 *     <li>用户<b>从未调用</b> {@code where()},且过滤器也未贡献条件 —— 返回 null,
 *         完全省略 where 子句;</li>
 *     <li>用户<b>显式调用过</b> {@code where()} 但未附加任何条件 —— 仍需构造 WhereModel,
 *         以保留原有的 {@code NonRenderingWhereClauseException} 校验语义。</li>
 * </ul>
 *
 * @param table             当前表表达式,过滤器据此决策
 * @param whereApplier      已累积的 where 条件,无显式 where 时传 null
 * @param enabledFilter    是否启用过滤器
 * @return 构建好的 where 模型;无任何条件且无显式 where 时返回 null
 */
internal fun buildFilteredWhereModel(table: TableExpression, whereApplier: WhereApplier?,enabledFilter: Boolean): WhereModel? {
    if(!enabledFilter || table.filter().isEmpty()) {
        if (whereApplier == null) {
            return null
        }
        val initialCriterion = whereApplier.initialCriterion()
        val subCriterion = whereApplier.subCriteria()
        return WhereModel(initialCriterion, subCriterion)
    }
    val where =  whereApplier?:WhereApplier(NullCriterion(), emptyList())
    val filtered = SQLFilterChain.createFilterChain(*table.filter()).filterWhere(where,table)
    if(filtered.isEffectivelyEmpty() && whereApplier == null ){
        return null
    }
    return WhereModel(filtered.initialCriterion(), filtered.subCriteria())
}

internal fun buildInsertColumnFilters(table: SqlTable, columnMappings: List<AbstractColumnMapping>,enabledFilter: Boolean): List<AbstractColumnMapping> {
    val filters = table.filter()
    if (!enabledFilter || filters.isEmpty()) {
        return columnMappings
    }
    return SQLFilterChain.createFilterChain(*table.filter()).filterGeneralInsert(columnMappings,table)
}

internal fun buildUpdateColumnFilters(table: SqlTable, columnMappings: List<AbstractColumnMapping>,enabledFilter: Boolean): List<AbstractColumnMapping> {
    val filters = table.filter()
    if (!enabledFilter || filters.isEmpty()) {
        return columnMappings
    }
    return SQLFilterChain.createFilterChain(*table.filter()).filterUpdate(columnMappings,table)
}
