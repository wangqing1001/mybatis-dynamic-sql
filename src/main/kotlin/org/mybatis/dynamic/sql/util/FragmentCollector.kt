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

import java.util.Optional
import java.util.stream.Collector

/**
 * 片段收集器。用于收集渲染过程中的 SQL 片段与参数。
 */
class FragmentCollector(
    private val fragments: List<String> = listOf(),
    private val parameters: Map<String, Any?> = mapOf()
) {

    fun firstFragment(): String? {
        return fragments.firstOrNull()
    }

    fun collectFragments(fragmentCollector: Collector<CharSequence, *, String>): String {
        return fragments.stream().collect(fragmentCollector)
    }

    @JvmOverloads
    fun collectFragments(separator: CharSequence, prefix: CharSequence = "", postfix: CharSequence = ""): String {
        return this.fragments.joinToString(separator, prefix, postfix )
    }


    fun toFragmentAndParameters(fragmentCollector: Collector<CharSequence, *, String>): FragmentAndParameters {
        return FragmentAndParameters(collectFragments(fragmentCollector), parameters())
    }

    fun toFragmentAndParameters(separator: CharSequence, prefix: CharSequence = "", postfix: CharSequence = ""): FragmentAndParameters {
        val fragments = collectFragments(separator, prefix, postfix )
        return FragmentAndParameters(fragments, parameters())
    }

    fun parameters(): Map<String, Any?> {
        return parameters
    }

    fun hasMultipleFragments(): Boolean {
        return fragments.size > 1
    }

    fun isEmpty(): Boolean {
        return fragments.isEmpty()
    }

}
