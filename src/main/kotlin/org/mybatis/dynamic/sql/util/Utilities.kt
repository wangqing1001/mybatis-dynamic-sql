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
package org.mybatis.dynamic.sql.util

import java.util.Objects
import java.util.stream.Stream

/**
 * 通用工具类。
 */
interface Utilities {
    companion object {
        @JvmStatic
        fun safelyUnbox(l: Long?): Long {
            return l ?: 0
        }

        @JvmStatic
        fun <T> filterNulls(values: Collection<T?>): Stream<T> {
            // 此方法帮助 IntelliJ 理解预期的可空性
            return values.stream().filter { Objects.nonNull(it) }.map { it!! }
        }
    }
}
