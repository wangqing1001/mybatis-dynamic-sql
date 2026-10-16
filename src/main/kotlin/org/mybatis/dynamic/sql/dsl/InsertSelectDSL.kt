package org.mybatis.dynamic.sql.dsl

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.insert.select.InsertColumnListModel
import org.mybatis.dynamic.sql.insert.select.InsertSelectModel
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.ConfigurableStatement
import java.util.function.Consumer

/**
 * insert-select DSL。
 */
class InsertSelectDSL private constructor(
    private val table: SqlTable,
    private val selectModel: SelectModel,
    private val columnList: InsertColumnListModel? = null
) : Buildable<InsertSelectModel>, ConfigurableStatement<InsertSelectDSL> {

    private val statementConfiguration: StatementConfiguration = StatementConfiguration()

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
            return InsertSelectDSL(table,selectModelBuilder.build(),columnList)
        }
    }
}