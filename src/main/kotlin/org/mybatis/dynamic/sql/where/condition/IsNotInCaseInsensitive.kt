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
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.StringUtilities
import org.mybatis.dynamic.sql.util.Validator
import java.util.Arrays
import java.util.Collections
import java.util.function.Function
import java.util.function.Predicate

/**
 * 不区分大小写的 not in 条件,如 column not in (upper(values))。
 */
class IsNotInCaseInsensitive<T> private constructor(values: Collection<T>) : AbstractListValueCondition<T>(
    values.map { StringUtilities.upperCaseIfPossible(it) }
), CaseInsensitiveRenderableCondition<T>, AbstractListValueCondition.Filterable<T>, AbstractListValueCondition.Mappable<T> {

    override fun shouldRender(renderingContext: RenderingContext): Boolean {
        Validator.assertNotEmpty(values(), "ERROR.44", "IsNotInCaseInsensitive") //$NON-NLS-1$ //$NON-NLS-2$
        return true
    }

    override fun operator(): String {
        return "not in" //$NON-NLS-1$
    }

    override fun filter(predicate: (T) -> Boolean): AbstractListValueCondition<T> {
        return filterSupport(predicate, { IsNotInCaseInsensitive(it) }, this, { empty() })
    }

    override fun <R> map(mapper: (T) -> R): AbstractListValueCondition<R> {
        return mapSupport(mapper, { IsNotInCaseInsensitive(it) }, { empty() })
    }

    companion object {
        private val EMPTY: IsNotInCaseInsensitive<Any> = IsNotInCaseInsensitive(Collections.emptyList<Any>())

        @JvmStatic
        fun <T> empty(): IsNotInCaseInsensitive<T> {
            @Suppress("UNCHECKED_CAST")
            return EMPTY as IsNotInCaseInsensitive<T>
        }

        @JvmStatic
        fun <T> of(vararg values: T): IsNotInCaseInsensitive<T> {
            return of(Arrays.asList(*values))
        }

        @JvmStatic
        fun <T> of(values: Collection<T>): IsNotInCaseInsensitive<T> {
            return IsNotInCaseInsensitive(values)
        }
    }
}
