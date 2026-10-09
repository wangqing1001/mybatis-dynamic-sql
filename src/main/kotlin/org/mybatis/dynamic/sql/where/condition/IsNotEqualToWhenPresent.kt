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

import org.mybatis.dynamic.sql.AbstractSingleValueCondition
import java.util.NoSuchElementException
import java.util.function.Function
import java.util.function.Predicate

/**
 * 不等于 when present 条件。当值为 null 时渲染为空条件。
 */
open class IsNotEqualToWhenPresent<T> private constructor(value: T) : AbstractSingleValueCondition<T>(value),
    AbstractSingleValueCondition.Filterable<T>, AbstractSingleValueCondition.Mappable<T> {

    override fun operator(): String {
        return "<>" //$NON-NLS-1$
    }

    override fun filter(predicate: Predicate<in T>): IsNotEqualToWhenPresent<T> {
        return filterSupport(predicate, { empty() }, this)
    }

    override fun <R> map(mapper: Function<in T, out R>): IsNotEqualToWhenPresent<R> {
        return mapSupport(mapper, { r: R -> of(r) }, { empty() })
    }

    companion object {
        private val EMPTY: IsNotEqualToWhenPresent<Any> = object : IsNotEqualToWhenPresent<Any>(-1) {
            override fun value(): Any {
                throw NoSuchElementException("No value present") //$NON-NLS-1$
            }

            override fun isEmpty(): Boolean {
                return true
            }
        }

        @JvmStatic
        fun <T> empty(): IsNotEqualToWhenPresent<T> {
            @Suppress("UNCHECKED_CAST")
            return EMPTY as IsNotEqualToWhenPresent<T>
        }

        @JvmStatic
        fun <T> of(value: T?): IsNotEqualToWhenPresent<T> {
            return if (value == null) empty() else IsNotEqualToWhenPresent(value)
        }
    }
}
