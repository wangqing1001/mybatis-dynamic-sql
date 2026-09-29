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
package org.mybatis.dynamic.sql.select.function

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.BindableColumn
import java.util.Arrays

/**
 * 乘法函数。
 */
class Multiply<T> private constructor(
    firstColumn: BasicColumn,
    secondColumn: BasicColumn,
    subsequentColumns: List<BasicColumn>
) : OperatorFunction<T>("*", firstColumn, secondColumn, subsequentColumns) { //$NON-NLS-1$

    override fun copy(): Multiply<T> {
        return Multiply(column, secondColumn, subsequentColumns)
    }

    companion object {
        @JvmStatic
        fun <T> of(
            firstColumn: BindableColumn<T>,
            secondColumn: BasicColumn,
            vararg subsequentColumns: BasicColumn
        ): Multiply<T> {
            return of(firstColumn, secondColumn, Arrays.asList(*subsequentColumns))
        }

        @JvmStatic
        fun <T> of(
            firstColumn: BindableColumn<T>,
            secondColumn: BasicColumn,
            subsequentColumns: List<BasicColumn>
        ): Multiply<T> {
            return Multiply(firstColumn, secondColumn, subsequentColumns)
        }
    }
}
