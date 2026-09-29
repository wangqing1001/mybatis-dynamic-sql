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
package org.mybatis.dynamic.sql.util

import java.util.*
import java.util.function.UnaryOperator

class FragmentAndParameters private constructor(builder: Builder) {

    private val fragment: String
    private val parameters: MutableMap<String, Any?>

    init {
        fragment = builder.fragment
        parameters = Collections.unmodifiableMap(builder.parameters)
    }

    fun fragment(): String {
        return fragment
    }

    fun parameters(): MutableMap<String, Any?> {
        return parameters
    }

    /**
     * Return a new instance with the same parameters and a transformed fragment.
     *
     * @param mapper a function that can change the value of the fragment
     * @return a new instance with the same parameters and a transformed fragment
     */
    fun mapFragment(mapper: UnaryOperator<String>): FragmentAndParameters {
        return withFragment(mapper.apply(fragment))
            .withParameters(parameters)
            .build()
    }

    class Builder {

        lateinit var fragment: String
        val parameters: MutableMap<String, Any?> = mutableMapOf()

        fun withFragment(fragment: String): Builder {
            this.fragment = fragment
            return this
        }

        fun withParameter(key: String, value: Any?): Builder {
            parameters[key] = value
            return this
        }

        fun withParameters(parameters: Map<String, Any?>): Builder {
            this.parameters.putAll(parameters)
            return this
        }

        fun build(): FragmentAndParameters {
            return FragmentAndParameters(this)
        }

        fun buildOptional(): Optional<FragmentAndParameters> {
            return Optional.of(build())
        }
    }

    companion object {

        @JvmStatic
        fun withFragment(fragment: String): Builder {
            return Builder().withFragment(fragment)
        }

        @JvmStatic
        fun fromFragment(fragment: String): FragmentAndParameters {
            return Builder().withFragment(fragment).build()
        }

    }
}
