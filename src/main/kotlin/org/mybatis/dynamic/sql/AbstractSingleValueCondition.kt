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
import org.mybatis.dynamic.sql.util.FragmentAndParameters.Companion.withFragment
import org.mybatis.dynamic.sql.util.StringUtilities
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier

abstract class AbstractSingleValueCondition<T : Any> protected constructor(protected val value: T) : RenderableCondition<T> {

    open fun value(): T {
        return value
    }

    protected fun <S : AbstractSingleValueCondition<T>> filterSupport(predicate: Predicate<in T>,emptySupplier: Supplier<S>, self: S): S {
        return if (isEmpty()) {
            self
        } else {
            if (predicate.test(value)) self else emptySupplier.get()
        }
    }

    protected fun <R, S : AbstractSingleValueCondition<R>> mapSupport(mapper: Function<in T, out R>,constructor: Function<R, S>, emptySupplier: Supplier<S>): S {
        return if (isEmpty()) {
            emptySupplier.get()
        } else {
            constructor.apply(mapper.apply(value))
        }
    }

    abstract fun operator(): String

    override fun renderCondition(renderingContext: RenderingContext,leftColumn: BindableColumn<T>): FragmentAndParameters {
        val parameterInfo = renderingContext.calculateParameterInfo(leftColumn)
        val finalFragment = operator() + StringUtilities.spaceBefore(parameterInfo.renderedPlaceHolder)
        return withFragment(finalFragment).withParameter(parameterInfo.parameterMapKey, leftColumn.convertParameterType(value()))
            .build()
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
     *
     * @param <T> the Java type related to the database column type
    </T> */
    interface Filterable<T : Any> {
        /**
         * If renderable and the value matches the predicate, returns this condition. Else returns a condition
         * that will not render.
         *
         * @param predicate predicate applied to the value, if renderable
         * @return this condition if renderable and the value matches the predicate, otherwise a condition
         * that will not render.
         */
        fun filter(predicate: Predicate<in T>): AbstractSingleValueCondition<T>
    }

    /**
     * Conditions may implement Mappable to alter condition values or types during rendering.
     *
     *
     * If a condition is Mappable, then a user may add a mapper to the usage of the condition that can alter the
     * values of a condition, or change that datatype.
     *
     *
     * Implementations of Mappable may call
     * [mapSupport] as
     * a common implementation of the mapping algorithm.
     *
     * @param <T> the Java type related to the database column type
    </T> */
    interface Mappable<T> {

        /**
         * If renderable, apply the mapping to the value and return a new condition with the new value. Else return a
         * condition that will not render (this).
         *
         * @param mapper a mapping function to apply to the value, if renderable
         * @param <R> type of the new condition
         * @return a new condition with the result of applying the mapper to the value of this condition,
         * if renderable, otherwise a condition that will not render.
        </R> */
        fun <R: Any> map(mapper: Function<in T, out R>): AbstractSingleValueCondition<R>

    }
}
