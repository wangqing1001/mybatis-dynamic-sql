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
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.ConstantMapping
import org.mybatis.dynamic.sql.util.MappedColumnMapping
import org.mybatis.dynamic.sql.util.NullMapping
import org.mybatis.dynamic.sql.util.PropertyMapping
import org.mybatis.dynamic.sql.util.RowMapping
import org.mybatis.dynamic.sql.util.StringConstantMapping
import java.util.ArrayList
import java.util.Arrays
import java.util.Objects

/**
 * 批量 insert DSL。
 */
class BatchInsertDSL<T> private constructor(builder: AbstractBuilder<T, *>) : Buildable<BatchInsertModel<T>> {
    private val records: Collection<T>
    private val table: SqlTable
    private val columnMappings: MutableList<AbstractColumnMapping>

    init {
        this.records = builder.records
        this.table = Objects.requireNonNull(builder.table)
        this.columnMappings = builder.columnMappings
    }

    fun <F> map(column: SqlColumn<F>): ColumnMappingFinisher<F> {
        return ColumnMappingFinisher(column)
    }

    fun <F> withMappedColumn(column: SqlColumn<F>): BatchInsertDSL<T> {
        columnMappings.add(MappedColumnMapping.of(column))
        return this
    }

    override fun build(): BatchInsertModel<T> {
        return BatchInsertModel.withRecords(records)
            .withTable(table)
            .withColumnMappings(columnMappings)
            .build()
    }

    companion object {
        @JvmStatic
        @SafeVarargs
        fun <T> insert(vararg records: T): IntoGatherer<T> {
            return insert(Arrays.asList(*records))
        }

        @JvmStatic
        fun <T> insert(records: Collection<T>): IntoGatherer<T> {
            return IntoGatherer(records)
        }
    }

    class IntoGatherer<T>(private val records: Collection<T>) {
        fun into(table: SqlTable): BatchInsertDSL<T> {
            return Builder<T>().withRecords(records).withTable(table).build()
        }
    }

    inner class ColumnMappingFinisher<F>(private val column: SqlColumn<F>) {

        fun toProperty(property: String): BatchInsertDSL<T> {
            columnMappings.add(PropertyMapping.of(column, property))
            return this@BatchInsertDSL
        }

        fun toNull(): BatchInsertDSL<T> {
            columnMappings.add(NullMapping.of(column))
            return this@BatchInsertDSL
        }

        fun toConstant(constant: String): BatchInsertDSL<T> {
            columnMappings.add(ConstantMapping.of(column, constant))
            return this@BatchInsertDSL
        }

        fun toStringConstant(constant: String): BatchInsertDSL<T> {
            columnMappings.add(StringConstantMapping.of(column, constant))
            return this@BatchInsertDSL
        }

        fun toRow(): BatchInsertDSL<T> {
            columnMappings.add(RowMapping.of(column))
            return this@BatchInsertDSL
        }
    }

    abstract class AbstractBuilder<T, B : AbstractBuilder<T, B>> {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        val records: MutableCollection<T> = ArrayList()
        lateinit var table: SqlTable
        val columnMappings: MutableList<AbstractColumnMapping> = ArrayList()

        fun withRecords(records: Collection<T>): B {
            this.records.addAll(records)
            return getThis()
        }

        fun withTable(table: SqlTable): B {
            this.table = table
            return getThis()
        }

        fun withColumnMappings(columnMappings: Collection<AbstractColumnMapping>): B {
            this.columnMappings.addAll(columnMappings)
            return getThis()
        }

        protected abstract fun getThis(): B
    }

    class Builder<T> : AbstractBuilder<T, Builder<T>>() {
        override fun getThis(): Builder<T> {
            return this
        }

        fun build(): BatchInsertDSL<T> {
            return BatchInsertDSL(this)
        }
    }
}
