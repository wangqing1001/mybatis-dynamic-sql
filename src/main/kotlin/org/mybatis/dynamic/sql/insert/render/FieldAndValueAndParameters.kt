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

import java.util.HashMap
import java.util.Objects
import java.util.Optional

/**
 * 字段名、值短语与参数的组合。
 */
class FieldAndValueAndParameters private constructor(builder: Builder) {
    private val fieldName: String
    private val valuePhrase: String
    private val parameters: Map<String, Any?>

    init {
        fieldName = Objects.requireNonNull(builder.fieldName)
        valuePhrase = Objects.requireNonNull(builder.valuePhrase)
        parameters = builder.parameters
    }

    fun fieldName(): String {
        return fieldName
    }

    fun valuePhrase(): String {
        return valuePhrase
    }

    fun parameters(): Map<String, Any?> {
        return parameters
    }

    companion object {
        @JvmStatic
        fun withFieldName(fieldName: String): Builder {
            return Builder().withFieldName(fieldName)
        }
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        lateinit var fieldName: String
        lateinit var valuePhrase: String
        val parameters: MutableMap<String, Any?> = HashMap()

        fun withFieldName(fieldName: String): Builder {
            this.fieldName = fieldName
            return this
        }

        fun withValuePhrase(valuePhrase: String): Builder {
            this.valuePhrase = valuePhrase
            return this
        }

        fun withParameter(key: String, value: Any?): Builder {
            // 值可能为 null,因为参数类型转换器可能返回 null
            parameters.put(key, value)
            return this
        }

        fun build(): FieldAndValueAndParameters {
            return FieldAndValueAndParameters(this)
        }

        fun buildOptional(): Optional<FieldAndValueAndParameters> {
            return Optional.of(build())
        }
    }
}
