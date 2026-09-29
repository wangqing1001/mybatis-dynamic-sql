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
package org.mybatis.dynamic.sql.select.function

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters

/**
 * substring 函数。
 */
class Substring<T> private constructor(column: BasicColumn, private val offset: Int, private val length: Int) :
    AbstractUniTypeFunction<T, Substring<T>>(column) {

    override fun render(renderingContext: RenderingContext): FragmentAndParameters {
        return column.render(renderingContext).mapFragment {
            "substring($it, $offset, $length)"
        }
    }

    override fun copy(): Substring<T> {
        return Substring(column, offset, length)
    }

    companion object {
        @JvmStatic
        fun <T> of(column: BindableColumn<T>, offset: Int, length: Int): Substring<T> {
            return Substring(column, offset, length)
        }
    }
}
