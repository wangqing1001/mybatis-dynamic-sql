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
class FragmentCollector {
    private val fragments: MutableList<String> = mutableListOf()
    private val parameters: MutableMap<String, Any?> = mutableMapOf()

    constructor()

    private constructor(initialFragment: FragmentAndParameters) {
        add(initialFragment)
    }

    fun add(fragmentAndParameters: FragmentAndParameters) {
        fragments.add(fragmentAndParameters.fragment())
        parameters.putAll(fragmentAndParameters.parameters())
    }

    fun merge(other: FragmentCollector): FragmentCollector {
        fragments.addAll(other.fragments)
        parameters.putAll(other.parameters)
        return this
    }

    fun firstFragment(): Optional<String> {
        return fragments.stream().findFirst()
    }

    fun collectFragments(fragmentCollector: Collector<CharSequence, *, String>): String {
        return fragments.stream().collect(fragmentCollector)
    }

    fun toFragmentAndParameters(fragmentCollector: Collector<CharSequence, *, String>): FragmentAndParameters {
        return FragmentAndParameters.withFragment(collectFragments(fragmentCollector))
            .withParameters(parameters())
            .build()
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

    companion object {
        @JvmStatic
        fun collect(): Collector<FragmentAndParameters, FragmentCollector, FragmentCollector> {
            return Collector.of(
                { FragmentCollector() },
                { fc: FragmentCollector, fp: FragmentAndParameters -> fc.add(fp) },
                { fc1: FragmentCollector, fc2: FragmentCollector -> fc1.merge(fc2) }
            )
        }

        @JvmStatic
        fun collect(initialFragment: FragmentAndParameters): Collector<FragmentAndParameters, FragmentCollector, FragmentCollector> {
            return Collector.of(
                { FragmentCollector(initialFragment) },
                { fc: FragmentCollector, fp: FragmentAndParameters -> fc.add(fp) },
                { fc1: FragmentCollector, fc2: FragmentCollector -> fc1.merge(fc2) }
            )
        }
    }
}
