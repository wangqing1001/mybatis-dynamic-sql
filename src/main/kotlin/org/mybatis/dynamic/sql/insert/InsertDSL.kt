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
import org.mybatis.dynamic.sql.util.MappedColumnWhenPresentMapping
import org.mybatis.dynamic.sql.util.NullMapping
import org.mybatis.dynamic.sql.util.PropertyMapping
import org.mybatis.dynamic.sql.util.PropertyWhenPresentMapping
import org.mybatis.dynamic.sql.util.RowMapping
import org.mybatis.dynamic.sql.util.StringConstantMapping
import java.util.Objects
import java.util.function.Supplier

/**
 * 单行 insert DSL。
 */
class InsertDSL<T> private constructor(
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
        return InsertModel.withRow(row)
            .withTable(table)
            .withColumnMappings(columnMappings)
            .build()
    }

    companion object {
        @JvmStatic
        fun <T> insert(row: T): IntoGatherer<T> {
            return IntoGatherer(row)
        }


    }

    class IntoGatherer<T>(private val row: T) {
        fun into(table: SqlTable): InsertDSL<T> {
            return Builder(row).withTable(table).build()
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

    class Builder<T>(
        val row: T
    ) {
        lateinit var table: SqlTable
        val columnMappings: MutableList<AbstractColumnMapping> = mutableListOf()

        fun withTable(table: SqlTable): Builder<T> {
            this.table = table
            return this
        }

        fun withColumnMappings(columnMappings: Collection<AbstractColumnMapping>): Builder<T> {
            this.columnMappings.addAll(columnMappings)
            return this
        }

        fun build(): InsertDSL<T> {
            return InsertDSL(row, table, columnMappings)
        }
    }

}
