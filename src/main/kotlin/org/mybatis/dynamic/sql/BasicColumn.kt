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
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import java.sql.JDBCType

/**
 * Describes attributes of columns that are necessary for rendering if the column is not expected to
 * be bound as a JDBC parameter.  Columns in select lists, join expressions, and group by expressions
 * are typically not bound.
 *
 * @author Jeff Butler
 */
interface BasicColumn {
    /**
     * Returns the columns alias if one has been specified.
     *
     * @return the column alias
     */
    fun alias(): String?

    /**
     * Returns a new instance of a BasicColumn with the alias set.
     *
     * @param alias
     * the column alias to set
     *
     * @return new instance with alias set
     */
    fun `as`(alias: String): BasicColumn

    /**
     * Returns a rendering of the column.
     * The rendered fragment should include the table alias based on the TableAliasCalculator
     * in the RenderingContext. The fragment could contain prepared statement parameter
     * markers and associated parameter values if desired.
     *
     * @param renderingContext the rendering context (strategy, sequence, etc.)
     * @return a rendered SQL fragment and, optionally, parameters associated with the fragment
     * @since 1.5.1
     */
    fun render(renderingContext: RenderingContext): FragmentAndParameters

    fun jdbcType(): JDBCType? {
        return null
    }

    fun typeHandler(): String? {
        return null
    }

    fun renderingStrategy(): RenderingStrategy? {
        return null
    }

    companion object {
        /**
         * Utility method to make it easier to build column lists for methods that require an
         * array rather than the varargs method.
         *
         * @param columns list of BasicColumn
         * @return an array of BasicColumn
         */
        @JvmStatic
        fun columnList(vararg columns: BasicColumn): Array<out BasicColumn> {
            return columns
        }
    }
}
