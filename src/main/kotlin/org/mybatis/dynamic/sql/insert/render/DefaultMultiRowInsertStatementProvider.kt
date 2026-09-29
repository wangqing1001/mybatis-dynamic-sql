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
 * 默认多行 insert 语句提供者。
 */
class DefaultMultiRowInsertStatementProvider<T> private constructor(builder: Builder<T>) :
    MultiRowInsertStatementProvider<T> {
    override val records: List<T> = builder.records
    override val insertStatement: String = builder.insertStatement


    class Builder<T> {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        val records: MutableList<T> = mutableListOf()
        lateinit var insertStatement: String

        fun withRecords(records: List<T>): Builder<T> {
            this.records.addAll(records)
            return this
        }

        fun withInsertStatement(insertStatement: String): Builder<T> {
            this.insertStatement = insertStatement
            return this
        }

        fun build(): DefaultMultiRowInsertStatementProvider<T> {
            return DefaultMultiRowInsertStatementProvider(this)
        }
    }
}
