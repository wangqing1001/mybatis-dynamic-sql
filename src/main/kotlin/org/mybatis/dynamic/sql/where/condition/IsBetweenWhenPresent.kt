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
package org.mybatis.dynamic.sql.where.condition

import org.mybatis.dynamic.sql.AbstractTwoValueCondition
import java.util.NoSuchElementException
import java.util.function.BiPredicate
import java.util.function.Function
import java.util.function.Predicate

/**
 * between when present 条件。当任一值为 null 时渲染为空条件。
 */
open class IsBetweenWhenPresent<T> private constructor(value1: T, value2: T) : AbstractTwoValueCondition<T>(value1, value2),
    AbstractTwoValueCondition.Filterable<T>, AbstractTwoValueCondition.Mappable<T> {

    override fun operator1(): String {
        return "between" //$NON-NLS-1$
    }

    override fun operator2(): String {
        return "and" //$NON-NLS-1$
    }

    override fun filter(predicate: BiPredicate<in T, in T>): IsBetweenWhenPresent<T> {
        return filterSupport(predicate, { empty() }, this)
    }

    override fun filter(predicate: Predicate<in T>): IsBetweenWhenPresent<T> {
        return filterSupport(predicate, { empty() }, this)
    }

    override fun <R> map(
        mapper1: Function<in T, out R>,
        mapper2: Function<in T, out R>
    ): IsBetweenWhenPresent<R> {
        return mapSupport(mapper1, mapper2, { r1: R, r2: R -> of(r1, r2) }, { empty() })
    }

    override fun <R> map(mapper: Function<in T, out R>): IsBetweenWhenPresent<R> {
        return map(mapper, mapper)
    }

    class Builder<T> internal constructor(value1: T?) : AndWhenPresentGatherer<T, IsBetweenWhenPresent<T>>(value1) {

        override fun build(value2: T?): IsBetweenWhenPresent<T> {
            return of(value1, value2)
        }
    }

    companion object {
        private val EMPTY: IsBetweenWhenPresent<Any> = object : IsBetweenWhenPresent<Any>(-1, -1) {
            override fun value1(): Any {
                throw NoSuchElementException("No value present") //$NON-NLS-1$
            }

            override fun value2(): Any {
                throw NoSuchElementException("No value present") //$NON-NLS-1$
            }

            override fun isEmpty(): Boolean {
                return true
            }
        }

        @JvmStatic
        fun <T> empty(): IsBetweenWhenPresent<T> {
            @Suppress("UNCHECKED_CAST")
            return EMPTY as IsBetweenWhenPresent<T>
        }

        @JvmStatic
        fun <T> of(value1: T?, value2: T?): IsBetweenWhenPresent<T> {
            return if (value1 == null || value2 == null) empty() else IsBetweenWhenPresent(value1, value2)
        }

        @JvmStatic
        fun <T> isBetweenWhenPresent(value1: T?): Builder<T> {
            return Builder(value1)
        }
    }
}
