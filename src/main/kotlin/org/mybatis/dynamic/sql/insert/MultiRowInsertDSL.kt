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
import java.util.Arrays
import java.util.Objects

/**
 * 多行 insert DSL。
 */
class MultiRowInsertDSL<T> private constructor(builder: BatchInsertDSL.AbstractBuilder<T, *>) :
    Buildable<MultiRowInsertModel<T>> {
    private val records: Collection<T> = builder.records
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
        return MultiRowInsertModel.withRecords(records)
            .withTable(table)
            .withColumnMappings(columnMappings)
            .build()
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
