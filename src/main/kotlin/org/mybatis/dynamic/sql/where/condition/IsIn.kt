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
 * in 条件,如 column in (values)。
 */
class IsIn<T> private constructor(values: Collection<T>) : AbstractListValueCondition<T>(values),
    AbstractListValueCondition.Filterable<T>, AbstractListValueCondition.Mappable<T> {

    override fun shouldRender(renderingContext: RenderingContext): Boolean {
        Validator.assertNotEmpty(values, "ERROR.44", "IsIn") //$NON-NLS-1$ //$NON-NLS-2$
        return true
    }

    override fun operator(): String {
        return "in" //$NON-NLS-1$
    }

    override fun filter(predicate: Predicate<in T>): IsIn<T> {
        return filterSupport(predicate, { IsIn(it) }, this, { empty() })
    }

    override fun <R> map(mapper: Function<in T, out R>): IsIn<R> {
        return mapSupport(mapper, { IsIn(it) }, { empty() })
    }

    companion object {
        private val EMPTY: IsIn<Any> = IsIn(Collections.emptyList<Any>())

        @JvmStatic
        fun <T> empty(): IsIn<T> {
            @Suppress("UNCHECKED_CAST")
            return EMPTY as IsIn<T>
        }

        @JvmStatic
        fun <T> of(vararg values: T): IsIn<T> {
            return of(listOf(*values))
        }

        @JvmStatic
        fun <T> of(values: Collection<T>): IsIn<T> {
            return IsIn(values)
        }
    }
}
