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

import java.util.Objects

/**
 * 默认通用 insert 语句提供者。
 */
class DefaultGeneralInsertStatementProvider private constructor(builder: Builder) :
    GeneralInsertStatementProvider, InsertSelectStatementProvider {
    override val insertStatement: String
    override val parameters: Map<String, Any?>

    init {
        insertStatement = Objects.requireNonNull(builder.insertStatement)
        parameters = builder.parameters
    }



    companion object {
        @JvmStatic
        fun withInsertStatement(insertStatement: String): Builder {
            return Builder().withInsertStatement(insertStatement)
        }
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        lateinit var insertStatement: String
        val parameters: MutableMap<String, Any?> = HashMap()

        fun withInsertStatement(insertStatement: String): Builder {
            this.insertStatement = insertStatement
            return this
        }

        fun withParameters(parameters: Map<String, Any?>): Builder {
            this.parameters.putAll(parameters)
            return this
        }

        fun build(): DefaultGeneralInsertStatementProvider {
            return DefaultGeneralInsertStatementProvider(this)
        }
    }
}
