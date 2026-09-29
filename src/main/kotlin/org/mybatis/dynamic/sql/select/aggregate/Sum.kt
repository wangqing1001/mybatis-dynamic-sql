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
package org.mybatis.dynamic.sql.select.aggregate

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.RenderableCondition
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.select.function.AbstractUniTypeFunction
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.where.render.ColumnAndConditionRenderer
import java.util.function.Function

/**
 * sum 聚合函数。支持简单列求和,以及带条件的条件求和。
 */
class Sum<T> private constructor(
    column: BasicColumn,
    private val renderer: Function<RenderingContext, FragmentAndParameters>
) : AbstractUniTypeFunction<T, Sum<T>>(column) {

    private constructor(column: BasicColumn) : this(column, Function { rc: RenderingContext ->
        column.render(rc).mapFragment {  "sum($it)" }
    })

    private constructor(column: BindableColumn<T>, condition: RenderableCondition<T>) : this(
        column,
        Function { rc: RenderingContext ->
            Validator.assertTrue(condition.shouldRender(rc), "ERROR.37", "sum") //$NON-NLS-1$ //$NON-NLS-2$

            ColumnAndConditionRenderer.Builder<T>()
                .withColumn(column)
                .withCondition(condition)
                .withRenderingContext(rc)
                .build()
                .render()
                .mapFragment {  "sum($it)" }
        }
    )

    override fun render(renderingContext: RenderingContext): FragmentAndParameters {
        return renderer.apply(renderingContext)
    }



    override fun copy(): Sum<T> {
        return Sum(column, renderer)
    }

    companion object {
        @JvmStatic
        fun <T> of(column: BindableColumn<T>): Sum<T> {
            return Sum(column)
        }

        @JvmStatic
        fun of(column: BasicColumn): Sum<Any> {
            return Sum<Any>(column)
        }

        @JvmStatic
        fun <T> of(column: BindableColumn<T>, condition: RenderableCondition<T>): Sum<T> {
            return Sum(column, condition)
        }
    }
}
