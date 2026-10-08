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
package org.mybatis.dynamic.sql.render

import org.mybatis.dynamic.sql.SqlTable
import java.util.Objects
import java.util.Optional

/**
 * 带父级表别名计算器。先查询子计算器,再回退到父计算器。
 */
class TableAliasCalculatorWithParent(
    private val parent: TableAliasCalculator,
    private val child: TableAliasCalculator
) : TableAliasCalculator {

    override fun aliasForColumn(table: SqlTable): String? {
        val answer = child.aliasForColumn(table)
        return answer ?: parent.aliasForColumn(table)
    }

    override fun aliasForTable(table: SqlTable): String? {
        val answer = child.aliasForTable(table)
        return answer ?: parent.aliasForTable(table)
    }

}
