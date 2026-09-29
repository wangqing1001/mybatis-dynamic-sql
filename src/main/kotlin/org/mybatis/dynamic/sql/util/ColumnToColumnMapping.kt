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
package org.mybatis.dynamic.sql.util

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlColumn

/**
 * 将列映射到另一列的映射(仅用于 update 语句)。
 */
class ColumnToColumnMapping private constructor(
    column: SqlColumn<*>,
    private val rightColumn: BasicColumn
) : AbstractColumnMapping(column) {

    fun rightColumn(): BasicColumn {
        return rightColumn
    }

    override fun <R> accept(visitor: ColumnMappingVisitor<R>): R {
        return visitor.visit(this)
    }

    companion object {
        @JvmStatic
        fun of(column: SqlColumn<*>, rightColumn: BasicColumn): ColumnToColumnMapping {
            return ColumnToColumnMapping(column, rightColumn)
        }
    }
}
