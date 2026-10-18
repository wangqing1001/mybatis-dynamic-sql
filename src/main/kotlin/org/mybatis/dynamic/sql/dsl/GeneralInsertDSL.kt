package org.mybatis.dynamic.sql.dsl

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.filter.buildInsertColumnFilters
import org.mybatis.dynamic.sql.insert.GeneralInsertModel
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.ConfigurableStatement
import org.mybatis.dynamic.sql.util.ConstantMapping
import org.mybatis.dynamic.sql.util.NullMapping
import org.mybatis.dynamic.sql.util.StringConstantMapping
import org.mybatis.dynamic.sql.util.ValueMapping
import org.mybatis.dynamic.sql.util.ValueOrNullMapping
import org.mybatis.dynamic.sql.util.ValueWhenPresentMapping
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * 通用 insert DSL,支持 set 子句的各种映射方式。
 */
class GeneralInsertDSL(
    private val table: SqlTable
) : Buildable<GeneralInsertModel>, ConfigurableStatement<GeneralInsertDSL> {

    private val columnMappings: MutableList<AbstractColumnMapping> = mutableListOf()
    private val statementConfiguration: StatementConfiguration = StatementConfiguration()

    fun <T> set(column: SqlColumn<T>): SetClauseFinisher<T> {
        return SetClauseFinisher(column)
    }

    override fun configureStatement(consumer: Consumer<StatementConfiguration>): GeneralInsertDSL {
        consumer.accept(statementConfiguration)
        return this
    }

    internal fun withColumnMapping(columnMapping: AbstractColumnMapping): GeneralInsertDSL {
        this.columnMappings.add(columnMapping)
        return this
    }

    override fun build(): GeneralInsertModel {
        val enabledFilter = statementConfiguration.enabledFilter()
        val insertMappings = buildInsertColumnFilters(table,columnMappings,enabledFilter)
        return GeneralInsertModel(table, statementConfiguration, insertMappings)
    }


    inner class SetClauseFinisher<T>(private val column: SqlColumn<T>) {

        fun toNull(): GeneralInsertDSL {
            columnMappings.add(NullMapping(column))
            return this@GeneralInsertDSL
        }

        fun toConstant(constant: String): GeneralInsertDSL {
            columnMappings.add(ConstantMapping(column, constant))
            return this@GeneralInsertDSL
        }

        fun toStringConstant(constant: String): GeneralInsertDSL {
            columnMappings.add(StringConstantMapping(column, constant))
            return this@GeneralInsertDSL
        }

        fun toValue(value: T): GeneralInsertDSL {
            return toValue({ value })
        }

        fun toValue(valueSupplier: Supplier<T>): GeneralInsertDSL {
            columnMappings.add(ValueMapping(column, valueSupplier))
            return this@GeneralInsertDSL
        }

        fun toValueOrNull(value: T?): GeneralInsertDSL {
            return toValueOrNull({ value })
        }

        fun toValueOrNull(valueSupplier: Supplier<T?>): GeneralInsertDSL {
            columnMappings.add(ValueOrNullMapping(column, valueSupplier))
            return this@GeneralInsertDSL
        }

        fun toValueWhenPresent(value: T?): GeneralInsertDSL {
            return toValueWhenPresent({ value })
        }

        fun toValueWhenPresent(valueSupplier: Supplier<T?>): GeneralInsertDSL {
            columnMappings.add(ValueWhenPresentMapping(column, valueSupplier))
            return this@GeneralInsertDSL
        }
    }


}