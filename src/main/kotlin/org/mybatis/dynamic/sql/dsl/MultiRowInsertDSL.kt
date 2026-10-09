package org.mybatis.dynamic.sql.dsl

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.insert.batch.MultiRowInsertModel
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.ConstantMapping
import org.mybatis.dynamic.sql.util.MappedColumnMapping
import org.mybatis.dynamic.sql.util.NullMapping
import org.mybatis.dynamic.sql.util.PropertyMapping
import org.mybatis.dynamic.sql.util.RowMapping
import org.mybatis.dynamic.sql.util.StringConstantMapping
import java.util.Objects

/**
 * 多行 insert DSL。
 */
class MultiRowInsertDSL<T> private constructor(builder: BatchInsertDSL.AbstractBuilder<T, *>) :
    Buildable<MultiRowInsertModel<T>> {
    private val records: List<T> = builder.records
    private val table: SqlTable = Objects.requireNonNull(builder.table)
    private val columnMappings: MutableList<AbstractColumnMapping> = builder.columnMappings

    fun <F> map(column: SqlColumn<F>): ColumnMappingFinisher<F> {
        return ColumnMappingFinisher(column)
    }

    fun <F> withMappedColumn(column: SqlColumn<F>): MultiRowInsertDSL<T> {
        columnMappings.add(MappedColumnMapping.of(column))
        return this
    }

    override fun build(): MultiRowInsertModel<T> {
        return MultiRowInsertModel(table, records, columnMappings)
    }

    companion object {
        @JvmStatic
        @SafeVarargs
        fun <T> insert(vararg records: T): IntoGatherer<T> {
            return insert(listOf(*records))
        }

        @JvmStatic
        fun <T> insert(records: Collection<T>): IntoGatherer<T> {
            return IntoGatherer(records)
        }
    }

    class IntoGatherer<T>(private val records: Collection<T>) {
        fun into(table: SqlTable): MultiRowInsertDSL<T> {
            return Builder<T>().withRecords(records).withTable(table).build()
        }
    }

    inner class ColumnMappingFinisher<F>(private val column: SqlColumn<F>) {

        fun toProperty(property: String): MultiRowInsertDSL<T> {
            columnMappings.add(PropertyMapping.of(column, property))
            return this@MultiRowInsertDSL
        }

        fun toNull(): MultiRowInsertDSL<T> {
            columnMappings.add(NullMapping.of(column))
            return this@MultiRowInsertDSL
        }

        fun toConstant(constant: String): MultiRowInsertDSL<T> {
            columnMappings.add(ConstantMapping.of(column, constant))
            return this@MultiRowInsertDSL
        }

        fun toStringConstant(constant: String): MultiRowInsertDSL<T> {
            columnMappings.add(StringConstantMapping.of(column, constant))
            return this@MultiRowInsertDSL
        }

        fun toRow(): MultiRowInsertDSL<T> {
            columnMappings.add(RowMapping.of(column))
            return this@MultiRowInsertDSL
        }
    }

    class Builder<T> : BatchInsertDSL.AbstractBuilder<T, Builder<T>>() {
        override fun getThis(): Builder<T> {
            return this
        }

        fun build(): MultiRowInsertDSL<T> {
            return MultiRowInsertDSL(this)
        }
    }
}