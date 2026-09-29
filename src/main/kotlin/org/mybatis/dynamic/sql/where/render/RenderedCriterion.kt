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
package org.mybatis.dynamic.sql.where.render

import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.StringUtilities
import java.util.Objects

/**
 * 渲染后的条件。包含可选的连接符(and/or)与片段参数。
 */
class RenderedCriterion private constructor(builder: Builder) {
    private val connector: String?
    private val fragmentAndParameters: FragmentAndParameters

    init {
        connector = builder.connector
        fragmentAndParameters = Objects.requireNonNull(builder.fragmentAndParameters!!)
    }

    fun fragmentAndParameters(): FragmentAndParameters {
        return fragmentAndParameters
    }

    fun fragmentAndParametersWithConnector(): FragmentAndParameters {
        return if (connector == null) {
            fragmentAndParameters
        } else {
            prependFragment(fragmentAndParameters, connector)
        }
    }

    fun withConnector(connector: String): RenderedCriterion {
        return Builder()
            .withFragmentAndParameters(fragmentAndParameters)
            .withConnector(connector)
            .build()
    }

    private fun prependFragment(fragmentAndParameters: FragmentAndParameters, connector: String): FragmentAndParameters {
        return fragmentAndParameters.mapFragment { s: String -> connector + StringUtilities.spaceBefore(s) }
    }

    class Builder {
        var connector: String? = null
            private set
        var fragmentAndParameters: FragmentAndParameters? = null
            private set

        fun withConnector(connector: String): Builder {
            this.connector = connector
            return this
        }

        fun withFragmentAndParameters(fragmentAndParameters: FragmentAndParameters): Builder {
            this.fragmentAndParameters = fragmentAndParameters
            return this
        }

        fun build(): RenderedCriterion {
            return RenderedCriterion(this)
        }
    }
}
