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
package org.mybatis.dynamic.sql.select

import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.util.Buildable
import java.util.function.Function

/**
 * 表示可用于创建通用 select 方法的函数。使用此函数可以创建一个不需要用户调用
 * build() 和 render() 的方法,使客户端代码看起来更简洁。
 *
 * 该函数旨在与 MyBatis3Utils 等工具方法配合使用。
 *
 * @author Jeff Butler
 */
fun interface SelectDSLCompleter :
    Function<QueryExpressionDSL<SelectModel>, Buildable<SelectModel>> {

    companion object {
        /**
         * 返回一个可用于选择表中每一行的 completer。
         *
         * @return 将选择表中每一行的 completer
         */
        @JvmStatic
        fun allRows(): SelectDSLCompleter {
            return SelectDSLCompleter { c -> c }
        }

        /**
         * 返回一个可用于选择表中每一行并按指定顺序排列的 completer。
         *
         * @param columns 用于 order by 子句的排序规范列表
         * @return 将选择表中每一行并按指定顺序排列的 completer
         */
        @JvmStatic
        fun allRowsOrderedBy(vararg columns: SortSpecification): SelectDSLCompleter {
            return SelectDSLCompleter { c -> c.orderBy(*columns) }
        }
    }
}
