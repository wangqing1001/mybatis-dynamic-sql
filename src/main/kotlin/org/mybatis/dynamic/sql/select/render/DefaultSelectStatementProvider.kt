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
package org.mybatis.dynamic.sql.select.render

import java.util.Collections
import java.util.HashMap
import java.util.Objects

/**
 * SelectStatementProvider 的默认实现。
 */
class DefaultSelectStatementProvider private constructor(builder: Builder) : SelectStatementProvider {

    override val selectStatement: String
    override val parameters: Map<String, Any?>

    init {
        selectStatement = Objects.requireNonNull(builder.selectStatement!!)
        parameters = Collections.unmodifiableMap(Objects.requireNonNull(builder.parameters))
    }



    companion object {
        @JvmStatic
        fun withSelectStatement(selectStatement: String): Builder {
            return Builder().withSelectStatement(selectStatement)
        }
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        var selectStatement: String? = null
        val parameters: MutableMap<String, Any?> = HashMap()

        fun withSelectStatement(selectStatement: String): Builder {
            this.selectStatement = selectStatement
            return this
        }

        fun withParameters(parameters: Map<String, Any?>): Builder {
            this.parameters.putAll(parameters)
            return this
        }

        fun build(): DefaultSelectStatementProvider {
            return DefaultSelectStatementProvider(this)
        }
    }
}
