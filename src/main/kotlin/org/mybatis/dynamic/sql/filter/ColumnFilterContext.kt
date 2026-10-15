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

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.ConstantMapping
import org.mybatis.dynamic.sql.util.NullMapping
import org.mybatis.dynamic.sql.util.StringConstantMapping
import java.util.function.Supplier

/**
 * 过滤器向 insert / update 的 set 子句贡献列映射的最小上下文。
 *
 * <p>与 [WhereFilterContext] 的设计意图一致:只暴露「追加一个列映射」所需的能力,
 * 而不是把完整的 DSL 交给过滤器。过滤器只需声明自己要为哪些列赋值, 列的合并(去重、顺序)由 [SQLFilterChain] 统一负责。
 *
 * <p>有意只暴露三种映射类型 —— null、常量、字符串常量。原因是它们恰好是
 * general insert 与 update 两类语句所支持映射的交集,能保证同一份过滤器代码
 * 在两种语句中都能生效。而审计字段这一典型场景(如 updated_by、updated_time)
 * 也只需要这几种映射即可表达。
 *
 * <p>典型用法:
 *
 * ```kotlin
 * class AuditFilter(private val updatedBy: SqlColumn<String>) : SQLFilter {
 *     override fun doWhere(context: WhereFilterContext) = Unit
 *
 *     override fun doUpdate(context: ColumnFilterContext) {
 *         context.setStringConstant(updatedBy, "system")
 *     }
 *
 *     override fun doGeneralInsert(context: ColumnFilterContext) {
 *         context.setStringConstant(updatedBy, "system")
 *     }
 * }
 * ```
 */
interface ColumnFilterContext {

    /**
     * 当前正在构建写语句的表。
     */
    val table: SqlTable

    /**
     * 把列设置为 null。
     *
     * @param column 目标列
     * @return 当前上下文,便于链式调用
     */
    fun setNull(column: SqlColumn<*>): ColumnFilterContext

    /**
     * 把列设置为原样渲染的常量。常量将完全按照给定内容写入 SQL,不做引号包装,
     * 因此适用于函数调用等表达式,例如 {@code current_timestamp}。
     *
     * @param column   目标列
     * @param constant 常量表达式
     * @return 当前上下文,便于链式调用
     */
    fun setConstant(column: SqlColumn<*>, constant: String): ColumnFilterContext

    /**
     * 把列设置为字符串常量。该值在渲染时会自动加上单引号并做转义,
     * 适用于 {@code updated_by = 'system'} 这类场景。
     *
     * @param column 目标列
     * @param value  字符串值
     * @return 当前上下文,便于链式调用
     */
    fun setStringConstant(column: SqlColumn<*>, value: String): ColumnFilterContext


    fun <T> setValue(column: SqlColumn<T>, value: T): ColumnFilterContext{
        return setValue(column,  { value })
    }

    fun <T> setValue(column: SqlColumn<T>, value: Supplier<T>): ColumnFilterContext


    /**
     * 将当前累积的列映射转换为列表,供 DSL 合并使用。
     *
     * @return 过滤器贡献的列映射列表
     */
    fun toColumnMappings(): List<AbstractColumnMapping>


}

