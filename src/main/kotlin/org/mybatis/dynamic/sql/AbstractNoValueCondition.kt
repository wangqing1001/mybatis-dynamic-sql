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
import java.util.function.BooleanSupplier
import java.util.function.Supplier

abstract class AbstractNoValueCondition<T> : RenderableCondition<T> {

    protected fun <S : AbstractNoValueCondition<*>> filterSupport(booleanSupplier: BooleanSupplier,emptySupplier: Supplier<S>, self: S): S {
        return if (isEmpty()) {
            self
        } else {
            if (booleanSupplier.asBoolean) self else emptySupplier.get()
        }
    }

    abstract fun operator(): String

    override fun renderCondition(renderingContext: RenderingContext,leftColumn: BindableColumn<T>): FragmentAndParameters {
        return FragmentAndParameters.fromFragment(operator())
    }

    /**
     * Conditions may implement Filterable to add optionality to rendering.
     *
     *
     * If a condition is Filterable, then a user may add a filter to the usage of the condition that makes a decision
     * whether to render the condition at runtime. Conditions that fail the filter will be dropped from the
     * rendered SQL.
     *
     *
     * Implementations of Filterable may call
     * [filterSupport] as
     * a common implementation of the filtering algorithm.
     */
    interface Filterable {
        /**
         * If renderable and the supplier returns true, returns this condition. Else returns a condition that will not
         * render.
         *
         * @param booleanSupplier
         * function that specifies whether the condition should render
         * @param <S>
         * condition type - not used except for compilation compliance
         *
         * @return this condition if renderable and the supplier returns true, otherwise a condition that will not
         * render.
        </S> */
        fun <S> filter(booleanSupplier: BooleanSupplier): AbstractNoValueCondition<S>
    }
}
