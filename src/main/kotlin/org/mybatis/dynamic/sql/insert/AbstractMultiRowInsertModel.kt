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

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.Validator
import java.util.Collections
import java.util.Objects
import java.util.stream.Stream

/**
 * 多行 insert 模型的抽象基类,持有表、记录与列映射。
 */
abstract class AbstractMultiRowInsertModel<T> protected constructor(
    private val table: SqlTable,
    private val records: List<T>,
    private val columnMappings: List<AbstractColumnMapping> = emptyList()
) {

    fun columnMappings(): List<AbstractColumnMapping> {
        return columnMappings
    }

    fun records(): List<T> {
        return records
    }

    fun table(): SqlTable {
        return table
    }

    fun recordCount(): Int {
        return records.size
    }

    abstract class AbstractBuilder<T, S : AbstractBuilder<T, S>> {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        lateinit var table: SqlTable
        val records: MutableList<T> = mutableListOf()
        val columnMappings: MutableList<AbstractColumnMapping> = mutableListOf()

        fun withTable(table: SqlTable): S {
            this.table = table
            return getThis()
        }

        fun withRecords(records: Collection<T>): S {
            this.records.addAll(records)
            return getThis()
        }

        fun withColumnMappings(columnMappings: List<AbstractColumnMapping>): S {
            this.columnMappings.addAll(columnMappings)
            return getThis()
        }

        protected abstract fun getThis(): S
    }
}
