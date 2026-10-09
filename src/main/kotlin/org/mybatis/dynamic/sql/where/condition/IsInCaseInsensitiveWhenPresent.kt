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

import org.mybatis.dynamic.sql.AbstractListValueCondition
import org.mybatis.dynamic.sql.util.StringUtilities
import org.mybatis.dynamic.sql.util.Utilities
import java.util.Arrays
import java.util.Collections
import java.util.function.Function
import java.util.function.Predicate

/**
 * 不区分大小写的 in when present 条件。当值为 null 时渲染为空条件,并过滤集合中的 null 值后转大写。
 */
class IsInCaseInsensitiveWhenPresent<T> private constructor(values: Collection<T>) : AbstractListValueCondition<T>(
    values.filterNotNull().map { StringUtilities.upperCaseIfPossible(it) }
), CaseInsensitiveRenderableCondition<T>, AbstractListValueCondition.Filterable<T>, AbstractListValueCondition.Mappable<T> {

    override fun operator(): String {
        return "in" //$NON-NLS-1$
    }

    override fun filter(predicate: Predicate<in T>): IsInCaseInsensitiveWhenPresent<T> {
        return filterSupport(predicate, { IsInCaseInsensitiveWhenPresent(it) }, this, { empty() })
    }

    override fun <R> map(mapper: Function<in T, out R>): IsInCaseInsensitiveWhenPresent<R> {
        return mapSupport(mapper, { of(it) }, { empty() })
    }

    companion object {
        private val EMPTY: IsInCaseInsensitiveWhenPresent<Any> = IsInCaseInsensitiveWhenPresent(Collections.emptyList<Any>())

        @JvmStatic
        fun <T> empty(): IsInCaseInsensitiveWhenPresent<T> {
            @Suppress("UNCHECKED_CAST")
            return EMPTY as IsInCaseInsensitiveWhenPresent<T>
        }

        @JvmStatic
        fun <T> of(vararg values: T?): IsInCaseInsensitiveWhenPresent<T> {
            return of(listOf(*values))
        }

        @JvmStatic
        fun <T> of(values: Collection<T?>?): IsInCaseInsensitiveWhenPresent<T> {
            return if (values == null) empty() else IsInCaseInsensitiveWhenPresent(values.filterNotNull())
        }
    }
}
