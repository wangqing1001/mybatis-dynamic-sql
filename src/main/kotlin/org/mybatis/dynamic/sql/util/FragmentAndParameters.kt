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

import java.util.function.UnaryOperator

class FragmentAndParameters @JvmOverloads constructor (
    private val fragment: String,
    private val parameters: Map<String, Any?> = emptyMap()
) {

    fun fragment(): String {
        return fragment
    }

    fun parameters(): Map<String, Any?> {
        return parameters
    }

    fun mapFragment(mapper: UnaryOperator<String>): FragmentAndParameters {
        return FragmentAndParameters(mapper.apply(fragment),parameters)
    }

    operator fun plus(other: String): FragmentAndParameters {
        val fragment = this.fragment() + other
        return FragmentAndParameters(fragment, parameters)
    }

    operator fun plus(other: FragmentAndParameters): FragmentAndParameters {
        val fragment = this.fragment() + other.fragment()
        val map = mutableMapOf<String, Any?>()
        map.putAll(this.parameters)
        map.putAll(other.parameters)
        return FragmentAndParameters(fragment, parameters)
    }

}
