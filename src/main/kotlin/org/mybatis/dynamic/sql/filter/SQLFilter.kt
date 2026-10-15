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

/**
 * SQL 过滤器。用于在构建语句时附加额外的条件或列映射,典型场景是逻辑删除、
 * 多租户隔离、数据权限、审计字段填充等需要「自动追加内容」的需求。
 *
 * <p>过滤器只负责声明自己要附加哪些内容,不负责内容之间的串联——串联由
 * [SQLFilterChain] 与 [applyColumnFilters] 统一处理。因此实现时不必关心链路上游或下游,
 * 也不存在「忘记把接力棒传给下一个过滤器」的风险。
 *
 * <p>本接口覆盖四个场景,实现者<b>只需覆写自己关心的那些方法</b>,其余保持默认空实现:
 * <ul>
 *     <li>[doWhere] —— 为 select / count / delete / update 的 where 子句附加条件;</li>
 *     <li>[doUpdate] —— 为 update 的 set 子句附加列映射;</li>
 *     <li>[doGeneralInsert] —— 为 general insert 的列清单附加列映射。</li>
 * </ul>
 *
 * <p>注意:[doWhere] 是唯一的抽象方法,因此本接口仍可作为函数式接口使用,
 * lambda 形式对应 [doWhere]:
 *
 * ```kotlin
 * val filter = SQLFilter { context -> context.and { deleted isEqualTo true } }
 * ```
 *
 * <p>如需同时处理 where 与列,则应实现类:
 *
 * ```kotlin
 * class AuditFilter(private val deleted: SqlColumn<Boolean>,
 *                   private val updatedBy: SqlColumn<String>) : SQLFilter {
 *     override fun doWhere(context: WhereFilterContext) {
 *         context.and { deleted isEqualTo true }
 *     }
 *
 *     override fun doUpdate(context: ColumnFilterContext) {
 *         context.setStringConstant(updatedBy, "system")
 *     }
 * }
 * ```
 */
interface SQLFilter {

    /**
     * 向 where 子句贡献过滤条件。
     *
     * @param context 条件收集上下文,通过其 and/or/apply 方法追加条件
     */
    fun doWhere(context: WhereFilterContext){

    }

    /**
     * 向 update 语句的 set 子句贡献列映射,例如自动填充 updated_by。
     *
     * <p>仅当过滤器在用户显式设置的列之外补缺时才会写入——用户设置过的列不会被覆盖。
     *
     * @param context 列映射收集上下文
     */
    fun doUpdate(context: ColumnFilterContext) {
    }

    /**
     * 向 general insert 语句的列清单贡献列映射,例如自动填充 created_by。
     *
     * <p>仅当过滤器在用户显式设置的列之外补缺时才会写入——用户设置过的列不会被覆盖。
     *
     * @param context 列映射收集上下文
     */
    fun doGeneralInsert(context: ColumnFilterContext) {
    }
}
