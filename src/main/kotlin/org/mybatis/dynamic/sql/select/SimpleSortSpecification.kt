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
package org.mybatis.dynamic.sql.select

import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import java.util.Objects

/**
 * 用于没有合适列名的 order by 短语(例如计算列或聚合列)。
 *
 * @author Jeff Butler
 */
class SimpleSortSpecification private constructor(
    private val name: String,
    private val descendingPhrase: String
) : SortSpecification {

    private constructor(name: String) : this(name, "") //$NON-NLS-1$

    init {
        Objects.requireNonNull(name)
    }

    override fun descending(): SortSpecification {
        return SimpleSortSpecification(name, " DESC") //$NON-NLS-1$
    }

    override fun renderForOrderBy(renderingContext: RenderingContext): FragmentAndParameters {
        return FragmentAndParameters.fromFragment(name + descendingPhrase)
    }

    companion object {
        @JvmStatic
        fun of(name: String): SimpleSortSpecification {
            return SimpleSortSpecification(name)
        }
    }
}
