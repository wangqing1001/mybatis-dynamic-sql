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
import org.mybatis.dynamic.sql.CriteriaGroup
import org.mybatis.dynamic.sql.NullCriterion
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.util.kotlin.GroupingCriteriaCollector
import org.mybatis.dynamic.sql.util.kotlin.GroupingCriteriaReceiver
import org.mybatis.dynamic.sql.where.WhereApplier

/**
 * 过滤器向 where 子句贡献条件的最小上下文。
 *
 * <p>该接口有意只暴露「追加一个条件」所需的能力,而不是把完整的 where DSL 交给过滤器。
 * 过滤器实现者无需感知过滤器链的调用顺序,只需声明自己要附加哪些条件即可——条件之间的
 * 串联由 [SQLFilterChain] 统一负责。
 *
 * <p>典型用法:
 *
 * ```kotlin
 * class LogicDeleteFilter(private val deleted: SqlColumn<Boolean>) : SQLFilter {
 *     override fun doWhere(context: WhereFilterContext) {
 *         context.and { deleted isEqualTo true }
 *     }
 * }
 * ```
 */
interface WhereFilterContext {

    /**
     * 当前正在构建 where 子句的表表达式(可能是 [org.mybatis.dynamic.sql.SqlTable] 或子查询)。
     * 过滤器可据此决定是否附加条件。
     */
    val table: TableExpression

    /**
     * 以 "and" 连接符追加一个条件。
     *
     * <p>第一次调用时该条件将直接成为初始条件,因此无需担心产生 "where and xxx" 这类非法片段。
     *
     * @param criterion 要追加的条件
     * @return 当前上下文,便于链式调用
     */
    fun and(criterion: SqlCriterion): WhereFilterContext

    /**
     * 以 "or" 连接符追加一个条件。
     *
     * @param criterion 要追加的条件
     * @return 当前上下文,便于链式调用
     */
    fun or(criterion: SqlCriterion): WhereFilterContext

    /**
     * 追加一个已经构建好的 where 应用器。其初始条件与子条件将作为一个整体被追加。
     *
     * @param applier 已构建的 where 应用器
     * @return 当前上下文,便于链式调用
     */
    fun applyWhere(applier: WhereApplier): WhereFilterContext

    /**
     * 以 Kotlin DSL 风格追加一个 "and" 条件:
     *
     * ```kotlin
     * context.and { deleted isEqualTo true }
     * ```
     *
     * @param receiver 条件构建块
     * @return 当前上下文,便于链式调用
     */
    fun and(receiver: GroupingCriteriaReceiver): WhereFilterContext =
        and(GroupingCriteriaCollector().apply(receiver).toCriterion())

    /**
     * 以 Kotlin DSL 风格追加一个 "or" 条件。
     *
     * @param receiver 条件构建块
     * @return 当前上下文,便于链式调用
     */
    fun or(receiver: GroupingCriteriaReceiver): WhereFilterContext =
        or(GroupingCriteriaCollector().apply(receiver).toCriterion())

    /**
     * 将当前累积的条件转换为 where 应用器,供 DSL 合并使用。
     *
     * @return 表示当前累积条件的 where 应用器
     */
    fun toWhereApplier(): WhereApplier
}

/**
 * 把收集器中的条件包装为条件组。仅有一个条件时不做多余包装,保证渲染结果简洁。
 */
private fun GroupingCriteriaCollector.toCriterion(): SqlCriterion =
    if (subCriteria.isEmpty()) {
        initialCriterion
    } else {
        CriteriaGroup(initialCriterion, subCriteria)
    }

/**
 * [WhereFilterContext] 的默认实现。
 *
 * <p>该实现是可变累加器,但生命周期仅限一次过滤器链执行;每次执行都会创建新实例,
 * 因此不会像持有游标的实现那样出现「第二次调用静默失效」的问题。
 */
internal class DefaultWhereFilterContext(
    override val table: TableExpression
) : WhereFilterContext {

    private var initialCriterion: SqlCriterion = NullCriterion()
    private val subCriteria: MutableList<AndOrCriteriaGroup> = mutableListOf()

    override fun and(criterion: SqlCriterion): WhereFilterContext = addCriterion("and", criterion)

    override fun or(criterion: SqlCriterion): WhereFilterContext = addCriterion("or", criterion)

    override fun applyWhere(applier: WhereApplier): WhereFilterContext =
        addCriterion("and", CriteriaGroup(applier.initialCriterion(), applier.subCriteria()))

    override fun toWhereApplier(): WhereApplier = WhereApplier(initialCriterion, subCriteria)

    private fun addCriterion(connector: String, criterion: SqlCriterion): WhereFilterContext {
        if (isFirstCriterion()) {
            initialCriterion = criterion
        } else {
            subCriteria.add(AndOrCriteriaGroup(connector, criterion))
        }
        return this
    }

    private fun isFirstCriterion(): Boolean =
        initialCriterion is NullCriterion && subCriteria.isEmpty()
}
