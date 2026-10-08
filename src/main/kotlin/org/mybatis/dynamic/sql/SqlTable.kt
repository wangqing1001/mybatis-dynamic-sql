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
package org.mybatis.dynamic.sql

import java.sql.JDBCType

open class SqlTable protected constructor(protected var tableName: String) : TableExpression {

    fun tableName(): String {
        return tableName
    }

    fun allColumns(): BasicColumn {
        return SqlColumn.of<Any>("*", this) //$NON-NLS-1$
    }

    fun <T: Any> column(name: String): SqlColumn<T> {
        return SqlColumn.of(name, this)
    }

    fun <T: Any> column(name: String, jdbcType: JDBCType): SqlColumn<T> {
        return SqlColumn.of(name, this, jdbcType)
    }

    fun <T: Any> column(name: String, jdbcType: JDBCType, typeHandler: String): SqlColumn<T> {
        return SqlColumn.Builder<T>().withTable(this).withName(name).withJdbcType(jdbcType).withTypeHandler(typeHandler).build()
    }

    override fun <R> accept(visitor: TableExpressionVisitor<R>): R {
        return visitor.visit(this)
    }

    open fun tableAlias(): String? {
        return null
    }

    companion object {

        @JvmStatic
        fun of(name: String): SqlTable {
            return SqlTable(name)
        }

    }
}
