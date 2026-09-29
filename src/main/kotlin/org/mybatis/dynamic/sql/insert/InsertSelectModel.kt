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
class InsertSelectModel private constructor(builder: Builder) {
    private val table: SqlTable
    private val columnList: InsertColumnListModel?
    private val selectModel: SelectModel
    private val statementConfiguration: StatementConfiguration

    init {
        table = Objects.requireNonNull(builder.table)
        columnList = builder.columnList
        selectModel = Objects.requireNonNull(builder.selectModel)
        statementConfiguration = Objects.requireNonNull(builder.statementConfiguration)
    }

    fun table(): SqlTable {
        return table
    }

    fun selectModel(): SelectModel {
        return selectModel
    }

    fun columnList(): Optional<InsertColumnListModel> {
        return Optional.ofNullable(columnList)
    }

    fun statementConfiguration(): StatementConfiguration {
        return statementConfiguration
    }

    fun render(renderingStrategy: RenderingStrategy): InsertSelectStatementProvider {
        return InsertSelectRenderer.withInsertSelectModel(this)
            .withRenderingStrategy(renderingStrategy)
            .build()
            .render()
    }

    companion object {
        @JvmStatic
        fun withTable(table: SqlTable): Builder {
            return Builder().withTable(table)
        }
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        lateinit var table: SqlTable
        var columnList: InsertColumnListModel? = null
        lateinit var selectModel: SelectModel
        lateinit var statementConfiguration: StatementConfiguration

        fun withTable(table: SqlTable): Builder {
            this.table = table
            return this
        }

        fun withColumnList(columnList: InsertColumnListModel?): Builder {
            this.columnList = columnList
            return this
        }

        fun withSelectModel(selectModel: SelectModel): Builder {
            this.selectModel = selectModel
            return this
        }

        fun withStatementConfiguration(statementConfiguration: StatementConfiguration): Builder {
            this.statementConfiguration = statementConfiguration
            return this
        }

        fun build(): InsertSelectModel {
            return InsertSelectModel(this)
        }
    }
}
