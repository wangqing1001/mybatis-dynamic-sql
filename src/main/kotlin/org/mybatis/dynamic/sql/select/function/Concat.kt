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
import org.mybatis.dynamic.sql.util.FragmentCollector
import java.util.ArrayList
import java.util.Arrays
import java.util.stream.Collectors

/**
 * concat 函数,将所有列连接为一个 concat(...) 表达式。
 */
class Concat<T> protected constructor(
    firstColumn: BasicColumn,
    subsequentColumns: List<BasicColumn>
) : AbstractUniTypeFunction<T, Concat<T>>(firstColumn) {
    private val allColumns: MutableList<BasicColumn> = ArrayList()

    init {
        allColumns.add(firstColumn)
        this.allColumns.addAll(subsequentColumns)
    }

    override fun render(renderingContext: RenderingContext): FragmentAndParameters {
        // 注意 - 下面的类型标注是为了解决某些编译器中的类型推断问题
        return allColumns.stream()
            .map { column: BasicColumn -> column.render(renderingContext) }
            .collect(FragmentCollector.collect()).toFragmentAndParameters(
                Collectors.joining(", ", "concat(", ")") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            )
    }

    override fun copy(): Concat<T> {
        return Concat(column, allColumns.subList(1, allColumns.size))
    }

    companion object {
        @JvmStatic
        fun <T> concat(firstColumn: BindableColumn<T>, vararg subsequentColumns: BasicColumn): Concat<T> {
            return Concat(firstColumn, Arrays.asList(*subsequentColumns))
        }

        @JvmStatic
        fun <T> of(firstColumn: BindableColumn<T>, subsequentColumns: List<BasicColumn>): Concat<T> {
            return Concat(firstColumn, subsequentColumns)
        }
    }
}
