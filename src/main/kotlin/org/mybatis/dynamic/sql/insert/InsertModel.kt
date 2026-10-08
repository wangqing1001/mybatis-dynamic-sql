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
package org.mybatis.dynamic.sql.insert

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.insert.render.InsertRenderer
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.Validator
import java.util.ArrayList
import java.util.Objects
import java.util.stream.Stream

/**
 * 单行 insert 模型。
 */
class InsertModel<T> @JvmOverloads constructor(
    private val table: SqlTable,
    private val row: T,
    private val columnMappings: List<AbstractColumnMapping> = emptyList()
) {

    init {
        Validator.assertNotEmpty(columnMappings, "ERROR.7") //$NON-NLS-1$
    }

    fun columnMappings(): List<AbstractColumnMapping> {
        return columnMappings
    }

    fun row(): T {
        return row
    }

    fun table(): SqlTable {
        return table
    }

    fun render(renderingStrategy: RenderingStrategy): InsertStatementProvider<T> {
        return InsertRenderer(this,renderingStrategy).render()
    }

}
