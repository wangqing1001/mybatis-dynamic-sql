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
package org.mybatis.dynamic.sql.select

import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import java.util.Objects

/**
 * 基于列的排序规范:指定表别名和列。
 */
class ColumnSortSpecification private constructor(
    private val tableAlias: String,
    private val column: SqlColumn<*>,
    private val descendingPhrase: String
) : SortSpecification {

    constructor(tableAlias: String, column: SqlColumn<*>) :
        this(tableAlias, column, "") //$NON-NLS-1$

    init {
        Objects.requireNonNull(tableAlias)
        Objects.requireNonNull(column)
    }

    override fun descending(): SortSpecification {
        return ColumnSortSpecification(tableAlias, column, " DESC") //$NON-NLS-1$
    }

    override fun renderForOrderBy(renderingContext: RenderingContext): FragmentAndParameters {
        return FragmentAndParameters(tableAlias + "." + column.name() + descendingPhrase) //$NON-NLS-1$
    }
}
