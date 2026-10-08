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
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.StringUtilities
import java.util.function.UnaryOperator

abstract class AbstractColumnComparisonCondition<T> protected constructor(protected val rightColumn: BasicColumn) : RenderableCondition<T> {

    abstract fun operator(): String

    override fun renderCondition(renderingContext: RenderingContext,leftColumn: BindableColumn<T>): FragmentAndParameters {
        return rightColumn.render(renderingContext)
            .mapFragment { operator() + StringUtilities.spaceBefore(it) }
    }

}
