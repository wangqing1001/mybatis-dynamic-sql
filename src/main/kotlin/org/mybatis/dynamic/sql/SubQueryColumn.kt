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
package org.mybatis.dynamic.sql

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.FragmentAndParameters

class SubQueryColumn private constructor(private val selectModel: SelectModel) : BasicColumn {

    private var alias: String? = null

    override fun alias(): String? {
        return alias
    }

    override fun `as`(alias: String): SubQueryColumn {
        val answer = SubQueryColumn(selectModel)
        answer.alias = alias
        return answer
    }

    override fun render(renderingContext: RenderingContext): FragmentAndParameters {
        return selectModel.render(renderingContext,"(",")")
    }

    companion object {
        @JvmStatic
        fun of(selectModel: SelectModel): SubQueryColumn {
            return SubQueryColumn(selectModel)
        }
    }
}
