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
import java.util.Objects

/**
 * 表示可以改变底层类型的函数。例如,将二进制字段转换为 base64 字符串,或将整数转换为字符串等。
 *
 * <p>感谢 @endink 的想法。
 *
 * @param T 底层列的类型。例如,如果函数将 VARCHAR 转换为 INT,则底层类型将是 String
 * @param R 转换后列的类型。例如,如果函数将 VARCHAR 转换为 INT,则转换后的类型将是 Integer
 * @param U 实现该函数的具体子类型
 */
abstract class AbstractTypeConvertingFunction<T, R, U : AbstractTypeConvertingFunction<T, R, U>> protected constructor(
    protected val column: BasicColumn
) : BindableColumn<R> {

    protected var alias: String? = null

    override fun alias(): String? {
        return alias
    }

    override fun `as`(alias: String): U {
        val newThing = copy()
        newThing.alias = alias
        return newThing
    }

    protected abstract fun copy(): U
}
