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
import org.mybatis.dynamic.sql.util.toFragmentCollector
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier


abstract class AbstractListValueCondition<T>(private val values: Collection<T>) : RenderableCondition<T> {

    fun values(): Collection<T> {
        return values
    }

    override fun isEmpty(): Boolean {
        return values.isEmpty()
    }

    protected fun <S : AbstractListValueCondition<T>> filterSupport(predicate:Predicate<in T>,constructor: Function<Collection<T>,S>,self: S,emptySupplier: Supplier<S>): S {
        if (isEmpty()) {
            return self
        }
        val filtered = values.stream().filter(predicate).toList()
        return if (filtered.isEmpty()) emptySupplier.get() else constructor.apply(filtered)
    }

    protected fun <R, S : AbstractListValueCondition<R>> mapSupport(mapper: Function<in T,out R>,constructor: Function<Collection<R>,S>, emptySupplier: Supplier<S>): S {
        if (isEmpty()) {
            return emptySupplier.get()
        }
        return constructor.apply(values.stream().map(mapper).toList()  )
    }

    abstract fun operator(): String

    override fun renderCondition(renderingContext: RenderingContext,leftColumn: BindableColumn<T>): FragmentAndParameters {
        return values.map{ toFragmentAndParameters(it, renderingContext,leftColumn) }
            .toFragmentCollector().toFragmentAndParameters(",","${operator()} (", ")")
    }

    private fun toFragmentAndParameters(value: T,renderingContext: RenderingContext,leftColumn: BindableColumn<T>): FragmentAndParameters{
        val parameterInfo = renderingContext.calculateParameterInfo(leftColumn)
        val parameters = mapOf(parameterInfo.parameterMapKey to leftColumn.convertParameterType(value))
        return FragmentAndParameters(parameterInfo.renderedPlaceHolder,parameters)
    }


    interface Filterable<T> {

        fun filter(predicate: Predicate<in T>): AbstractListValueCondition<T>

    }

    interface Mappable<T> {

        fun <R> map(mapper: Function<in T,out R>): AbstractListValueCondition<R>

    }

}
