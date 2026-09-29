/*
 *    Copyright 2016-2025 the original author or authors.
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
package org.mybatis.dynamic.sql.exception

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.util.Messages
import java.util.Objects

/**
 * 当构建的查询对同一个 SqlTable 实例指定了多个别名时抛出此异常。
 * 该错误会产生一个无法工作的 select 语句。
 *
 * <p>此错误通常发生在构建自连接查询时。解决方法是创建 SqlTable 对象的第二个实例用于自连接。
 */
class DuplicateTableAliasException : DynamicSqlException {
    constructor(table: SqlTable, newAlias: String, existingAlias: String) : super(
        generateMessage(
            Objects.requireNonNull(table),
            Objects.requireNonNull(newAlias),
            Objects.requireNonNull(existingAlias)
        )
    )

    companion object {
        private fun generateMessage(table: SqlTable, newAlias: String, existingAlias: String): String {
            return Messages.getString("ERROR.1", table.tableName(), newAlias, existingAlias) //$NON-NLS-1$
        }

        @JvmField
        @kotlin.jvm.Transient
        val serialVersionUID: Long = -2631664872557787391L
    }
}
