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
 * SQL 过滤器。用于在构建 where 子句时附加额外的过滤条件,典型场景是逻辑删除、
 * 多租户隔离、数据权限等需要"自动追加 where 条件"的需求。
 *
 * <p>过滤器只负责声明自己要附加哪些条件,不负责条件之间的串联——串联由
 * [SQLFilterChain] 统一处理。因此实现时不必关心链路上游或下游,
 * 也不存在"忘记把接力棒传给下一个过滤器"的风险。
 *
 * <p>实现示例:
 *
 * ```kotlin
 * class LogicDeleteFilter(private val deleted: SqlColumn<Boolean>) : SQLFilter {
 *     override fun doWhere(context: WhereFilterContext) {
 *         context.and { deleted isEqualTo true }
 *     }
 * }
 * ```
 *
 * <p>本接口是函数式接口,因此也可以直接用 lambda 实现:
 *
 * ```kotlin
 * val filter = SQLFilter { context -> context.and { deleted isEqualTo true } }
 * ```
 */
fun interface SQLFilter {

    /**
     * 向 where 子句贡献过滤条件。
     *
     * @param context 条件收集上下文,通过其 and/or/apply 方法追加条件
     */
    fun doWhere(context: WhereFilterContext)
}
