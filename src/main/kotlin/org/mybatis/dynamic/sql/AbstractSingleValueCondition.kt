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

abstract class AbstractSingleValueCondition<T> protected constructor(protected val value: T) : RenderableCondition<T> {

    open fun value(): T {
        return value
    }

    protected fun <S : AbstractSingleValueCondition<T>> filterSupport(predicate: (T)->Boolean,emptySupplier: ()->S, self: S): S {
        if (isEmpty()) {
            return self
        }
        return if (predicate(value)) self else emptySupplier()
    }

    protected fun <R, S : AbstractSingleValueCondition<R>> mapSupport(mapper:  (T)->R,constructor: (R)->S, emptySupplier: ()->S): S {
        if (isEmpty()) {
            return emptySupplier()
        }
        return constructor(mapper(value))
    }

    abstract fun operator(): String

    override fun renderCondition(renderingContext: RenderingContext,leftColumn: BindableColumn<T>): FragmentAndParameters {
        val parameterInfo = renderingContext.calculateParameterInfo(leftColumn)
        val finalFragment = operator() + StringUtilities.spaceBefore(parameterInfo.renderedPlaceHolder)
        val parameters = mapOf(parameterInfo.parameterMapKey to leftColumn.convertParameterType(value()))
        return FragmentAndParameters(finalFragment, parameters)
    }

    interface Filterable<T> {

        fun filter(predicate: (T)->Boolean): AbstractSingleValueCondition<T>

    }

    interface Mappable<T> {

        fun <R> map(mapper: (T)->R): AbstractSingleValueCondition<R>

    }
}
