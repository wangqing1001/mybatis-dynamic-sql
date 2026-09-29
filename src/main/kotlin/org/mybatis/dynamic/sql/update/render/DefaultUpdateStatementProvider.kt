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
package org.mybatis.dynamic.sql.update.render

import java.util.HashMap
import java.util.Objects

/**
 * 默认 update 语句提供者。
 */
class DefaultUpdateStatementProvider private constructor(builder: Builder) : UpdateStatementProvider {

    override val updateStatement: String
    override val parameters: Map<String, Any?>

    init {
        updateStatement = Objects.requireNonNull(builder.updateStatement!!)
        parameters = builder.parameters
    }



    companion object {
        @JvmStatic
        fun withUpdateStatement(updateStatement: String): Builder {
            return Builder().withUpdateStatement(updateStatement)
        }
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        var updateStatement: String? = null
        val parameters: MutableMap<String, Any?> = HashMap()

        fun withUpdateStatement(updateStatement: String): Builder {
            this.updateStatement = updateStatement
            return this
        }

        fun withParameters(parameters: Map<String, Any?>): Builder {
            this.parameters.putAll(parameters)
            return this
        }

        fun build(): DefaultUpdateStatementProvider {
            return DefaultUpdateStatementProvider(this)
        }
    }
}
