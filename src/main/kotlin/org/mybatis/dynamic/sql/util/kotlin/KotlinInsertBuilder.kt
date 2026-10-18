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
package org.mybatis.dynamic.sql.util.kotlin

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.dsl.InsertDSL
import org.mybatis.dynamic.sql.insert.InsertModel
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.MappedColumnMapping
import org.mybatis.dynamic.sql.util.MappedColumnWhenPresentMapping

typealias KotlinInsertCompleter<T> = KotlinInsertBuilder<T>.() -> Unit

@MyBatisDslMarker
class KotlinInsertBuilder<T> (private val dsl: InsertDSL<T>): Buildable<InsertModel<T>> {


    fun <C> map(column: SqlColumn<C>) = SingleRowInsertColumnMapCompleter(column) {
        dsl.withColumnMapping(it)
    }

    fun <C> withMappedColumn(column: SqlColumn<C>) {
        dsl.withMappedColumn(column)
    }

    fun <C> withMappedColumnWhenPresent(column: SqlColumn<C>, valueSupplier: () -> Any?) {
        dsl.withMappedColumnWhenPresent(column,valueSupplier)
    }

    override fun build(): InsertModel<T> {
        return dsl.build()
    }
}
