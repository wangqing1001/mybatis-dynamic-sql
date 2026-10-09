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
    private val row: T,
    private val table: SqlTable,
    private val columnMappings: MutableList<AbstractColumnMapping>,
) : Buildable<InsertModel<T>> {

    fun <F> map(column: SqlColumn<F>): ColumnMappingFinisher<F> {
        return ColumnMappingFinisher(column)
    }

    fun <F> withMappedColumn(column: SqlColumn<F>): InsertDSL<T> {
        columnMappings.add(MappedColumnMapping.of(column))
        return this
    }

    fun <F> withMappedColumnWhenPresent(column: SqlColumn<F>, valueSupplier: Supplier<*>): InsertDSL<T> {
        columnMappings.add(MappedColumnWhenPresentMapping.of(column, valueSupplier))
        return this
    }

    override fun build(): InsertModel<T> {
        return InsertModel(table, row, columnMappings)
    }

    companion object {
        @JvmStatic
        fun <T> insert(row: T): IntoGatherer<T> {
            return IntoGatherer(row)
        }

    }

    class IntoGatherer<T>(private val row: T) {
        fun into(table: SqlTable): InsertDSL<T> {
            return Builder(row,table).build()
        }
    }

    inner class ColumnMappingFinisher<F>(private val column: SqlColumn<F>) {

        fun toProperty(property: String): InsertDSL<T> {
            columnMappings.add(PropertyMapping.of(column, property))
            return this@InsertDSL
        }

        fun toPropertyWhenPresent(property: String, valueSupplier: Supplier<*>): InsertDSL<T> {
            columnMappings.add(PropertyWhenPresentMapping.of(column, property, valueSupplier))
            return this@InsertDSL
        }

        fun toNull(): InsertDSL<T> {
            columnMappings.add(NullMapping.of(column))
            return this@InsertDSL
        }

        fun toConstant(constant: String): InsertDSL<T> {
            columnMappings.add(ConstantMapping.of(column, constant))
            return this@InsertDSL
        }

        fun toStringConstant(constant: String): InsertDSL<T> {
            columnMappings.add(StringConstantMapping.of(column, constant))
            return this@InsertDSL
        }

        fun toRow(): InsertDSL<T> {
            columnMappings.add(RowMapping.of(column))
            return this@InsertDSL
        }
    }

    class Builder<T>(val row: T,val table: SqlTable) {

        val columnMappings: MutableList<AbstractColumnMapping> = mutableListOf()

        fun withColumnMappings(columnMappings: Collection<AbstractColumnMapping>): Builder<T> {
            this.columnMappings.addAll(columnMappings)
            return this
        }

        fun build(): InsertDSL<T> {
            return InsertDSL(row, table, columnMappings)
        }

    }

}