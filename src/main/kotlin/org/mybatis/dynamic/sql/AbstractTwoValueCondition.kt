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
import java.util.function.*
import java.util.function.Function

abstract class AbstractTwoValueCondition<T>(
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
        }
        return if (predicate.test(value1, value2)) self else emptySupplier.get()
    }

    protected fun <S : AbstractTwoValueCondition<T>> filterSupport(predicate: Predicate<in T>,emptySupplier: Supplier<S>, self: S): S {
        return filterSupport({ v1: T, v2: T -> predicate.test(v1) && predicate.test(v2) },emptySupplier, self)
    }

    protected fun <R, S : AbstractTwoValueCondition<R>> mapSupport( mapper1: Function<in T, out R>,mapper2: Function<in T, out R>, constructor: BiFunction<R, R, S>, emptySupplier: Supplier<S>): S {
        if (isEmpty()) {
            return emptySupplier.get()
        }
        return constructor.apply(mapper1.apply(value1), mapper2.apply(value2))
    }

    abstract fun operator1(): String

    abstract fun operator2(): String

    override fun renderCondition(renderingContext: RenderingContext,leftColumn: BindableColumn<T>): FragmentAndParameters {
        val parameterInfo1 = renderingContext.calculateParameterInfo(leftColumn)
        val parameterInfo2 = renderingContext.calculateParameterInfo(leftColumn)
        val finalFragment = (operator1()
                + StringUtilities.spaceBefore(parameterInfo1.renderedPlaceHolder)
                + StringUtilities.spaceBefore(operator2())
                + StringUtilities.spaceBefore(parameterInfo2.renderedPlaceHolder))
        val parameters = mapOf(parameterInfo1.parameterMapKey to leftColumn.convertParameterType(value1())!!, parameterInfo2.parameterMapKey to leftColumn.convertParameterType(value2()))
        return FragmentAndParameters(finalFragment, parameters)
    }

    interface Filterable<T> {

        fun filter(predicate: BiPredicate<in T, in T>): AbstractTwoValueCondition<T>

        fun filter(predicate: Predicate<in T>): AbstractTwoValueCondition<T>

    }

    interface Mappable<T> {

        fun <R> map(mapper1: Function<in T, out R>,mapper2: Function<in T, out R> ): AbstractTwoValueCondition<R>

        fun <R> map(mapper: Function<in T, out R>): AbstractTwoValueCondition<R>

    }
}
