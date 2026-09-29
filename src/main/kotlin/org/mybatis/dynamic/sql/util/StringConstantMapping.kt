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

import org.mybatis.dynamic.sql.SqlColumn

/**
 * 此类表示列与字符串常量之间的映射。该常量在 SQL 中渲染时应使用单引号括起来。
 *
 * @author Jeff Butler
 */
class StringConstantMapping private constructor(
    column: SqlColumn<*>,
    private val constant: String
) : AbstractColumnMapping(column) {

    fun constant(): String {
        return constant
    }

    override fun <R> accept(visitor: ColumnMappingVisitor<R>): R {
        return visitor.visit(this)
    }

    companion object {
        @JvmStatic
        fun of(column: SqlColumn<*>, constant: String): StringConstantMapping {
            return StringConstantMapping(column, constant)
        }
    }
}
