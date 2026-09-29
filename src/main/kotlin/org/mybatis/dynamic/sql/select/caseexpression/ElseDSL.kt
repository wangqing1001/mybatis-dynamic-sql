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
package org.mybatis.dynamic.sql.select.caseexpression

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.Constant
import org.mybatis.dynamic.sql.StringConstant

/**
 * else 子句 DSL 接口。提供各种类型的 else_ 便捷方法。
 */
interface ElseDSL<T> {

    @Suppress("FunctionName")
    fun else_(value: String): T {
        return else_(StringConstant.of(value))
    }

    @Suppress("FunctionName")
    fun else_(value: Boolean): T {
        return else_(Constant.of<Any>(value.toString()))
    }

    @Suppress("FunctionName")
    fun else_(value: Int): T {
        return else_(Constant.of<Any>(value.toString()))
    }

    @Suppress("FunctionName")
    fun else_(value: Long): T {
        return else_(Constant.of<Any>(value.toString()))
    }

    @Suppress("FunctionName")
    fun else_(value: Double): T {
        return else_(Constant.of<Any>(value.toString()))
    }

    @Suppress("FunctionName")
    fun else_(column: BasicColumn): T
}
