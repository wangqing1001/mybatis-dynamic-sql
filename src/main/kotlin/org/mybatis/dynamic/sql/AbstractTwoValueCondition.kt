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
import java.util.function.*
import java.util.function.Function

abstract class AbstractTwoValueCondition<T : Any>(
    protected val value1: T,
    protected val value2: T
) : RenderableCondition<T> {

    open fun value1(): T {
        return value1
    }

    open fun value2(): T {
        return value2
    }

    protected fun <S : AbstractTwoValueCondition<T>> filterSupport(predicate: BiPredicate<in T, in T>,emptySupplier: Supplier<S>, self: S): S {
        if (isEmpty()) {
            return self
        } else {
            return if (predicate.test(value1, value2)) self else emptySupplier.get()
        }
    }

    protected fun <S : AbstractTwoValueCondition<T>> filterSupport(
        predicate: Predicate<in T>,
        emptySupplier: Supplier<S>, self: S
    ): S {
        return filterSupport(
            BiPredicate { v1: T, v2: T -> predicate.test(v1) && predicate.test(v2) },
            emptySupplier,
            self
        )
    }

    protected fun <R, S : AbstractTwoValueCondition<R>> mapSupport(
        mapper1: Function<in T, out R>,
        mapper2: Function<in T, out R>, constructor: BiFunction<R, R, S>, emptySupplier: Supplier<S>
    ): S {
        if (isEmpty()) {
            return emptySupplier.get()
        } else {
            return constructor.apply(mapper1.apply(value1), mapper2.apply(value2))
        }
    }

    abstract fun operator1(): String?

    abstract fun operator2(): String?

    override fun renderCondition(
        renderingContext: RenderingContext,
        leftColumn: BindableColumn<T>
    ): FragmentAndParameters {
        val parameterInfo1 = renderingContext.calculateParameterInfo(leftColumn)
        val parameterInfo2 = renderingContext.calculateParameterInfo(leftColumn)

        val finalFragment = (operator1()
                + StringUtilities.spaceBefore(parameterInfo1.renderedPlaceHolder)
                + StringUtilities.spaceBefore(operator2()!!)
                + StringUtilities.spaceBefore(parameterInfo2.renderedPlaceHolder))

        return withFragment(finalFragment)
            .withParameter(parameterInfo1.parameterMapKey, leftColumn.convertParameterType(value1())!!)
            .withParameter(parameterInfo2.parameterMapKey, leftColumn.convertParameterType(value2())!!)
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
     * [filterSupport]
     * or [filterSupport] as
     * a common implementation of the filtering algorithm.
     *
     * @param <T> the Java type related to the database column type
    </T> */
    interface Filterable<T : Any> {
        /**
         * If renderable and the values match the predicate, returns this condition. Else returns a condition
         * that will not render.
         *
         * @param predicate predicate applied to the values, if renderable
         * @return this condition if renderable and the values match the predicate, otherwise a condition
         * that will not render.
         */
        fun filter(predicate: BiPredicate<in T, in T>): AbstractTwoValueCondition<T>

        /**
         * If renderable and both values match the predicate, returns this condition. Else returns a condition
         * that will not render. This function implements a short-circuiting test. If the
         * first value does not match the predicate, then the second value will not be tested.
         *
         * @param predicate predicate applied to both values, if renderable
         * @return this condition if renderable and the values match the predicate, otherwise a condition
         * that will not render.
         */
        fun filter(predicate: Predicate<in T>): AbstractTwoValueCondition<T>

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
         * If renderable, apply the mappings to the values and return a new condition with the new values. Else return a
         * condition that will not render (this).
         *
         * @param mapper1 a mapping function to apply to the first value, if renderable
         * @param mapper2 a mapping function to apply to the second value, if renderable
         * @param <R> type of the new condition
         * @return a new condition with the result of applying the mappers to the values of this condition,
         * if renderable, otherwise a condition that will not render.
        </R> */
        fun <R : Any> map(
            mapper1: Function<in T, out R>,
            mapper2: Function<in T, out R>
        ): AbstractTwoValueCondition<R>

        /**
         * If renderable, apply the mapping to both values and return a new condition with the new values. Else return a
         * condition that will not render (this).
         *
         * @param mapper a mapping function to apply to both values, if renderable
         * @param <R> type of the new condition
         * @return a new condition with the result of applying the mappers to the values of this condition,
         * if renderable, otherwise a condition that will not render.
        </R> */
        fun <R : Any> map(mapper: Function<in T, out R>): AbstractTwoValueCondition<R>
    }
}
