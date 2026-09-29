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

import java.util.function.Supplier

/**
 * 支持 between 条件 "and" 部分的工具类。此类支持构建器,因此是可变的。
 *
 * @param <T> between 条件的字段类型
 * @param <R> 正在构建的条件类型
 */
abstract class AndGatherer<T, R> protected constructor(protected val value1: T) {

    fun and(value2: T): R {
        return build(value2)
    }

    fun and(valueSupplier2: Supplier<T>): R {
        return and(valueSupplier2.get())
    }

    protected abstract fun build(value2: T): R
}
