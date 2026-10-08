/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.dsl

import org.mybatis.dynamic.sql.util.Buildable

/**
 * limit 与 offset 操作接口。
 */
interface LimitAndOffsetOperations<T, M> {

    fun limit(limit: Long): LimitFinisher<T, M> {
        return limitWhenPresent(limit)
    }

    fun limitWhenPresent(limit: Long?): LimitFinisher<T, M>

    fun offset(offset: Long): OffsetFirstFinisher<T, M> {
        return offsetWhenPresent(offset)
    }

    fun offsetWhenPresent(offset: Long?): OffsetFirstFinisher<T, M>

    fun fetchFirst(fetchFirstRows: Long): FetchFirstFinisher<T> {
        return fetchFirstWhenPresent(fetchFirstRows)
    }

    fun fetchFirstWhenPresent(fetchFirstRows: Long?): FetchFirstFinisher<T>

    interface OffsetFirstFinisher<T, M> : ForAndWaitOperations<T>, OrderByOperations<T>, Buildable<M> {

        fun fetchFirst(fetchFirstRows: Long): FetchFirstFinisher<T> {
            return fetchFirstWhenPresent(fetchFirstRows)
        }

        fun fetchFirstWhenPresent(fetchFirstRows: Long?): FetchFirstFinisher<T>

    }

    interface LimitFinisher<T, M> : ForAndWaitOperations<T>, OrderByOperations<T>, Buildable<M> {

        fun offset(offset: Long): T {
            return offsetWhenPresent(offset)
        }

        fun offsetWhenPresent(offset: Long?): T

    }

    interface FetchFirstFinisher<T> {

        fun rowsOnly(): T

    }
}
