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
import org.mybatis.dynamic.sql.insert.render.GeneralInsertRenderer
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.Validator
import java.util.ArrayList
import java.util.Objects
import java.util.stream.Stream

/**
 * 通用 insert 模型。
 */
class GeneralInsertModel private constructor(builder: Builder) {
    private val table: SqlTable
    private val insertMappings: List<AbstractColumnMapping>
    private val statementConfiguration: StatementConfiguration

    init {
        table = Objects.requireNonNull(builder.table)
        Validator.assertNotEmpty(builder.insertMappings, "ERROR.6") //$NON-NLS-1$
        insertMappings = builder.insertMappings
        statementConfiguration = Objects.requireNonNull(builder.statementConfiguration)
    }

    fun columnMappings(): Stream<AbstractColumnMapping> {
        return insertMappings.stream()
    }

    fun table(): SqlTable {
        return table
    }

    fun statementConfiguration(): StatementConfiguration {
        return statementConfiguration
    }

    fun render(renderingStrategy: RenderingStrategy): GeneralInsertStatementProvider {
        return GeneralInsertRenderer.withInsertModel(this)
            .withRenderingStrategy(renderingStrategy)
            .build()
            .render()
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        lateinit var table: SqlTable
        val insertMappings: MutableList<AbstractColumnMapping> = ArrayList()
        lateinit var statementConfiguration: StatementConfiguration

        fun withTable(table: SqlTable): Builder {
            this.table = table
            return this
        }

        fun withInsertMappings(insertMappings: List<AbstractColumnMapping>): Builder {
            this.insertMappings.addAll(insertMappings)
            return this
        }

        fun withStatementConfiguration(statementConfiguration: StatementConfiguration): Builder {
            this.statementConfiguration = statementConfiguration
            return this
        }

        fun build(): GeneralInsertModel {
            return GeneralInsertModel(this)
        }
    }
}
