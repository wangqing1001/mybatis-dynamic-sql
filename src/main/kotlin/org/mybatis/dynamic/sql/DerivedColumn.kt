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

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import java.sql.JDBCType

/**
 * A derived column is a column that is not directly related to a table. This is primarily
 * used for supporting sub-queries. The main difference in this class and [SqlColumn] is
 * that this class does not have a related [SqlTable] and therefore ignores any table
 * qualifier set in a query. If a table qualifier is required it can be set directly in the
 * builder for this class.
 *
 * @param <T>
 * The Java type that corresponds to this column - not used except for compiler type checking for conditions
</T> */
class DerivedColumn<T> protected constructor(builder: Builder<T>) : BindableColumn<T> {

    private val name: String
    private val tableQualifier: String?
    private val columnAlias: String?
    private val jdbcType: JDBCType?
    private val typeHandler: String?

    init {
        this.name = builder.name
        this.tableQualifier = builder.tableQualifier
        this.columnAlias = builder.columnAlias
        this.jdbcType = builder.jdbcType
        this.typeHandler = builder.typeHandler
    }

    override fun alias(): String? {
        return columnAlias
    }

    override fun jdbcType(): JDBCType? {
        return jdbcType
    }

    override fun typeHandler(): String? {
        return typeHandler
    }

    override fun render(renderingContext: RenderingContext): FragmentAndParameters {
        val fragment = if (tableQualifier == null) name else "$tableQualifier.$name" //$NON-NLS-1$
        return FragmentAndParameters.fromFragment(fragment)
    }

    override fun `as`(alias: String): DerivedColumn<T> {
        return Builder<T>()
            .withName(name)
            .withColumnAlias(alias)
            .withJdbcType(jdbcType)
            .withTypeHandler(typeHandler)
            .withTableQualifier(tableQualifier)
            .build()
    }

    class Builder<T> {
        lateinit var name: String
        var tableQualifier: String? = null
        var columnAlias: String? = null
        var jdbcType: JDBCType? = null
        var typeHandler: String? = null

        fun withName(name: String): Builder<T> {
            this.name = name
            return this
        }

        fun withTableQualifier(tableQualifier: String?): Builder<T> {
            this.tableQualifier = tableQualifier
            return this
        }

        fun withColumnAlias(columnAlias: String?): Builder<T> {
            this.columnAlias = columnAlias
            return this
        }

        fun withJdbcType(jdbcType: JDBCType?): Builder<T> {
            this.jdbcType = jdbcType
            return this
        }

        fun withTypeHandler(typeHandler: String?): Builder<T> {
            this.typeHandler = typeHandler
            return this
        }

        fun build(): DerivedColumn<T> {
            return DerivedColumn<T>(this)
        }
    }

    companion object {
        @JvmStatic
        fun <T> of(name: String): DerivedColumn<T?> {
            return Builder<T?>()
                .withName(name)
                .build()
        }

        @JvmStatic
        fun <T> of(name: String, tableQualifier: String?): DerivedColumn<T?> {
            return Builder<T?>()
                .withName(name)
                .withTableQualifier(tableQualifier)
                .build()
        }
    }
}
