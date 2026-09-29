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
package org.mybatis.dynamic.sql.insert.render


/**
 * 批量 insert 语句封装。
 */
class BatchInsert<T> private constructor(builder: Builder<T>) {
    private val insertStatement: String
    val records: List<T>

    init {
        insertStatement = builder.insertStatement
        records = builder.records
    }

    val insertStatementSQL: String
        get() = insertStatement



    /**
     * 返回 InsertStatement 对象列表。这对 MyBatis 批量支持很有用。
     *
     * @return InsertStatement 列表
     */
    fun insertStatements(): List<InsertStatementProvider<T>> {
        return records.stream()
            .map { row: T -> toInsertStatement(row) }
            .toList()
    }

    private fun toInsertStatement(row: T): InsertStatementProvider<T> {
        return DefaultInsertStatementProvider.withRow(row)
            .withInsertStatement(insertStatement)
            .build()
    }



    companion object {
        @JvmStatic
        fun <T> withRecords(records: List<T>): Builder<T> {
            return Builder<T>().withRecords(records)
        }
    }

    class Builder<T> {
        lateinit var insertStatement: String
        val records: MutableList<T> = mutableListOf()

        fun withInsertStatement(insertStatement: String): Builder<T> {
            this.insertStatement = insertStatement
            return this
        }

        fun withRecords(records: List<T>): Builder<T> {
            this.records.addAll(records)
            return this
        }

        fun build(): BatchInsert<T> {
            return BatchInsert(this)
        }
    }
}
