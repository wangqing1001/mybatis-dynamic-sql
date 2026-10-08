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
class RenderedCriterion @JvmOverloads constructor(
    private val connector: String? = null,
    private val fragmentAndParameters: FragmentAndParameters
) {

    fun fragmentAndParameters(): FragmentAndParameters {
        return fragmentAndParameters
    }

    fun fragmentAndParametersWithConnector(): FragmentAndParameters {
        if (connector == null) {
            return fragmentAndParameters
        }
        return fragmentAndParameters.mapFragment { connector + StringUtilities.spaceBefore(it) }
    }

    fun withConnector(connector: String): RenderedCriterion {
        return RenderedCriterion(connector, fragmentAndParameters)
    }

}
