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
package org.mybatis.dynamic.sql.util.spring

import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils

/**
 * 用于将行列表转换为 SqlParameterSource 数组的工具类。
 *
 * 该类是必要的,因为库为批量 insert 语句生成绑定的方式。
 * 绑定形式为 <code>:row.propertyName</code>。本类中的 <code>createBatch</code> 方法
 * 将所有输入行包装在名为 RowHolder 的类中,该类只有一个名为 "row" 的属性。
 * 这将允许生成的绑定在 Spring 批量 insert 中正常工作。
 */
object BatchInsertUtility {
    @JvmStatic
    fun <T> createBatch(rows: List<T>): Array<SqlParameterSource> {
        val tt: List<RowHolder<T>> = rows.stream()
            .map { row: T -> RowHolder(row) }
            .toList()

        return SqlParameterSourceUtils.createBatch(tt)
    }

    data class RowHolder<T>(val row: T)
}
