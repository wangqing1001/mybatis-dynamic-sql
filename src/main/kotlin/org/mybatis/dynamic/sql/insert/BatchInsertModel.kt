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
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.insert.render.BatchInsert
import org.mybatis.dynamic.sql.insert.render.BatchInsertRenderer
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.util.AbstractColumnMapping

/**
 * 批量 insert 模型。
 */
class BatchInsertModel<T> @JvmOverloads constructor(
    table: SqlTable,
    records: List<T> = emptyList(),
    columnMappings: List<AbstractColumnMapping> = emptyList()
) : AbstractMultiRowInsertModel<T>(table,records,columnMappings) {

    init {
        Validator.assertNotEmpty(records(), "ERROR.19") //$NON-NLS-1$
        Validator.assertNotEmpty(columnMappings(), "ERROR.5") //$NON-NLS-1$
    }

    fun render(renderingStrategy: RenderingStrategy): BatchInsert<T> {
        return BatchInsertRenderer(this,renderingStrategy).render()
    }

}
