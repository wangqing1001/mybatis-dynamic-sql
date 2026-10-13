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
package examples.emptywhere

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.filter.SQLFilter
import org.mybatis.dynamic.sql.filter.WhereFilterContext
import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo

/**
 * 逻辑删除过滤器。
 *
 * 该过滤器用于实现“软删除”场景：不对数据进行物理删除，而是在每次操作时
 * 自动追加逻辑删除标记列的条件，从而保证业务数据只能操作到“未删除”的记录。
 *
 * 工作方式：向 where 子句贡献 `deleted = false` 条件。过滤器只声明自己附加什么条件，
 * 无需关心过滤器链的执行顺序——串联由框架统一处理。
 *
 * 注意：该过滤器不会把 `delete` 语句改写为 `update` 语句，它只负责在语句上
 * 追加“未删除”过滤条件。
 *
 * @param deleted 逻辑删除标记列（类型通常为 Boolean，`false` 表示未删除）
 */
class LogicDeleteDSLFilter(val deleted: SqlColumn<Boolean>) : SQLFilter {

    override fun doWhere(context: WhereFilterContext) {
        context.and { deleted isEqualTo false }
    }
}
