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
 * 默认 insert 语句提供者。
 */
class DefaultInsertStatementProvider<T> private constructor(builder: Builder<T>) : InsertStatementProvider<T> {
    override val insertStatement: String = builder.insertStatement
    override val row: T = builder.row!!





    companion object {
        @JvmStatic
        fun <T> withRow(row: T): Builder<T> {
            return Builder<T>().withRow(row)
        }
    }

    class Builder<T> {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        lateinit var insertStatement: String
        var row: T?=null

        fun withInsertStatement(insertStatement: String): Builder<T> {
            this.insertStatement = insertStatement
            return this
        }

        fun withRow(row: T): Builder<T> {
            this.row = row
            return this
        }

        fun build(): DefaultInsertStatementProvider<T> {
            return DefaultInsertStatementProvider(this)
        }
    }
}
