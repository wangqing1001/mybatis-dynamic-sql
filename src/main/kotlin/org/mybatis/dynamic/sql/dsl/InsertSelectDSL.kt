package org.mybatis.dynamic.sql.dsl

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.insert.InsertColumnListModel
import org.mybatis.dynamic.sql.insert.InsertSelectModel
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
        return InsertSelectModel(table,selectModel,statementConfiguration,columnList)
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
            return withColumnList(listOf(*columns))
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
        private val columnList: InsertColumnListModel = InsertColumnListModel(columns)

        fun withSelectStatement(selectModelBuilder: Buildable<SelectModel>): InsertSelectDSL {
            return InsertSelectDSL(table, columnList, selectModelBuilder.build())
        }
    }
}