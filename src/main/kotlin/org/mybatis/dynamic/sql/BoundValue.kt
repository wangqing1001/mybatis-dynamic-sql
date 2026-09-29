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

import org.mybatis.dynamic.sql.exception.InvalidSqlException
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.FragmentAndParameters.Companion.withFragment
import org.mybatis.dynamic.sql.util.Messages
import java.util.*

/**
 * BoundValues are added to rendered SQL as a parameter marker only.
 *
 *
 * BoundValues are most useful in the context of functions. For example, a column value could be
 * incremented with an update statement like this:
 * `
 * UpdateStatementProvider updateStatement = update(person)
 * .set(age).equalTo(add(age, value(1)))
 * .where(id, isEqualTo(5))
 * .build()
 * .render(RenderingStrategies.MYBATIS3);
` *
 *
 * @param <T> the column type
 * @since 1.5.1
</T> */
class BoundValue<T: Any> private constructor(private val value: T) : BindableColumn<T> {


    override fun render(renderingContext: RenderingContext): FragmentAndParameters {
        val rpi = renderingContext.calculateParameterInfo<T>(this)
        return withFragment(rpi.renderedPlaceHolder)
            .withParameter(rpi.parameterMapKey, value)
            .build()
    }

    override fun alias(): String? {
        return null
    }

    override fun `as`(alias: String): BoundValue<T> {
        throw InvalidSqlException(Messages.getString("ERROR.38")) //$NON-NLS-1$
    }

    companion object {
        @JvmStatic
        fun <T : Any> of(value: T): BoundValue<T> {
            return BoundValue(value)
        }
    }
}
