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

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.ConfigurableStatement
import java.util.Arrays
import java.util.Objects
import java.util.function.Consumer

/**
 * insert-select DSL。
 */
class InsertSelectDSL private constructor(
    table: SqlTable,
    columnList: InsertColumnListModel?,
    selectModel: SelectModel
) : Buildable<InsertSelectModel>, ConfigurableStatement<InsertSelectDSL> {

    private val table: SqlTable
    private val columnList: InsertColumnListModel?
    private val selectModel: SelectModel
    private val statementConfiguration: StatementConfiguration = StatementConfiguration()

    init {
        this.table = Objects.requireNonNull(table)
        this.selectModel = Objects.requireNonNull(selectModel)
        this.columnList = columnList
    }

    private constructor(table: SqlTable, selectModel: SelectModel) : this(table, null, selectModel)

    override fun build(): InsertSelectModel {
        return InsertSelectModel.withTable(table)
            .withColumnList(columnList)
            .withSelectModel(selectModel)
            .withStatementConfiguration(statementConfiguration)
            .build()
    }

    override fun configureStatement(consumer: Consumer<StatementConfiguration>): InsertSelectDSL {
        consumer.accept(statementConfiguration)
        return this
    }

    companion object {
        @JvmStatic
        fun insertInto(table: SqlTable): InsertColumnGatherer {
            return InsertColumnGatherer(table)
        }
    }

    class InsertColumnGatherer(private val table: SqlTable) {

        fun withColumnList(vararg columns: SqlColumn<*>): SelectGatherer {
            return withColumnList(Arrays.asList(*columns))
        }

        fun withColumnList(columns: List<SqlColumn<*>>): SelectGatherer {
            return SelectGatherer(table, columns)
        }

        fun withSelectStatement(selectModelBuilder: Buildable<SelectModel>): InsertSelectDSL {
            return InsertSelectDSL(table, selectModelBuilder.build())
        }
    }

    class SelectGatherer (
        private val table: SqlTable,
        columns: List<SqlColumn<*>>
    ) {
        private val columnList: InsertColumnListModel = InsertColumnListModel.of(columns)

        fun withSelectStatement(selectModelBuilder: Buildable<SelectModel>): InsertSelectDSL {
            return InsertSelectDSL(table, columnList, selectModelBuilder.build())
        }
    }
}
