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
import java.sql.JDBCType

/**
 * 表示不改变底层数据类型的函数。
 *
 * @param T 底层列的类型
 * @param U 实现该函数的具体子类型
 */
abstract class AbstractUniTypeFunction<T, U : AbstractUniTypeFunction<T, U>> protected constructor(
    column: BasicColumn
) : AbstractTypeConvertingFunction<T, T, U>(column) {

    override fun jdbcType(): JDBCType? {
        return column.jdbcType()
    }

    override fun typeHandler(): String? {
        return column.typeHandler()
    }
}
