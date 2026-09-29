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
import org.mybatis.dynamic.sql.util.Validator
import java.util.Arrays
import java.util.Collections
import java.util.function.Function
import java.util.function.Predicate

/**
 * not in 条件,如 column not in (values)。
 */
class IsNotIn<T> private constructor(values: Collection<T>) : AbstractListValueCondition<T>(values),
    AbstractListValueCondition.Filterable<T>, AbstractListValueCondition.Mappable<T> {

    override fun shouldRender(renderingContext: RenderingContext): Boolean {
        Validator.assertNotEmpty(values, "ERROR.44", "IsNotIn") //$NON-NLS-1$ //$NON-NLS-2$
        return true
    }

    override fun operator(): String {
        return "not in" //$NON-NLS-1$
    }

    override fun filter(predicate: Predicate<in T>): IsNotIn<T> {
        return filterSupport(predicate, { IsNotIn(it) }, this, { empty() })
    }

    override fun <R> map(mapper: Function<in T, out R>): IsNotIn<R> {
        return mapSupport(mapper, { IsNotIn(it) }, { empty() })
    }

    companion object {
        private val EMPTY: IsNotIn<Any> = IsNotIn(Collections.emptyList<Any>())

        @JvmStatic
        fun <T> empty(): IsNotIn<T> {
            @Suppress("UNCHECKED_CAST")
            return EMPTY as IsNotIn<T>
        }

        @JvmStatic
        fun <T> of(vararg values: T): IsNotIn<T> {
            return of(Arrays.asList(*values))
        }

        @JvmStatic
        fun <T> of(values: Collection<T>): IsNotIn<T> {
            return IsNotIn(values)
        }
    }
}
