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
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.insert.render.InsertSelectRenderer
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.SelectModel
import java.util.Objects
import java.util.Optional

/**
 * insert-select 模型。
 */
class InsertSelectModel @JvmOverloads constructor(
    private val table: SqlTable,
    private val selectModel: SelectModel,
    private val statementConfiguration: StatementConfiguration,
    private val columnList: InsertColumnListModel? = null
) {

    fun table(): SqlTable {
        return table
    }

    fun selectModel(): SelectModel {
        return selectModel
    }

    fun columnList(): InsertColumnListModel? {
        return columnList
    }

    fun statementConfiguration(): StatementConfiguration {
        return statementConfiguration
    }

    fun render(renderingStrategy: RenderingStrategy): InsertSelectStatementProvider {
        return InsertSelectRenderer(this,renderingStrategy).render()
    }

}
