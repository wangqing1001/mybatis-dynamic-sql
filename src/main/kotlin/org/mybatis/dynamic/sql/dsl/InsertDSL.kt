package org.mybatis.dynamic.sql.dsl

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.insert.InsertModel
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.ConstantMapping
import org.mybatis.dynamic.sql.util.MappedColumnMapping
import org.mybatis.dynamic.sql.util.MappedColumnWhenPresentMapping
import org.mybatis.dynamic.sql.util.NullMapping
import org.mybatis.dynamic.sql.util.PropertyMapping
import org.mybatis.dynamic.sql.util.PropertyWhenPresentMapping
import org.mybatis.dynamic.sql.util.RowMapping
import org.mybatis.dynamic.sql.util.StringConstantMapping
import java.util.function.Supplier

/**
 * 单行 insert DSL。
 */
class InsertDSL<T>(
    private val table: SqlTable,
    private val row: T
) : Buildable<InsertModel<T>> {

    private val columnMappings: MutableList<AbstractColumnMapping> = mutableListOf()

    fun <F> map(column: SqlColumn<F>): ColumnMappingFinisher<F> {
        return ColumnMappingFinisher(column)
    }

    internal fun withColumnMapping(columnMapping: AbstractColumnMapping): InsertDSL<T> {
        this.columnMappings.add(columnMapping)
        return this
    }

    fun <F> withMappedColumn(column: SqlColumn<F>): InsertDSL<T> {
        columnMappings.add(MappedColumnMapping(column))
        return this
    }

    fun <F> withMappedColumnWhenPresent(column: SqlColumn<F>, valueSupplier: Supplier<*>): InsertDSL<T> {
        columnMappings.add(MappedColumnWhenPresentMapping(column, valueSupplier))
        return this
    }

    override fun build(): InsertModel<T> {
        return InsertModel(table, row, columnMappings)
    }

    inner class ColumnMappingFinisher<F>(private val column: SqlColumn<F>) {

        fun toProperty(property: String): InsertDSL<T> {
            columnMappings.add(PropertyMapping(column, property))
            return this@InsertDSL
        }

        fun toPropertyWhenPresent(property: String, valueSupplier: Supplier<*>): InsertDSL<T> {
            columnMappings.add(PropertyWhenPresentMapping(column, property, valueSupplier))
            return this@InsertDSL
        }

        fun toNull(): InsertDSL<T> {
            columnMappings.add(NullMapping(column))
            return this@InsertDSL
        }

        fun toConstant(constant: String): InsertDSL<T> {
            columnMappings.add(ConstantMapping(column, constant))
            return this@InsertDSL
        }

        fun toStringConstant(constant: String): InsertDSL<T> {
            columnMappings.add(StringConstantMapping(column, constant))
            return this@InsertDSL
        }

        fun toRow(): InsertDSL<T> {
            columnMappings.add(RowMapping(column))
            return this@InsertDSL
        }
    }



}