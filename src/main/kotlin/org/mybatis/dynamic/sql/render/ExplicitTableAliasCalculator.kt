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
import java.util.HashMap
import java.util.Objects
import java.util.Optional

/**
 * 显式表别名计算器。基于显式指定的别名映射计算别名。
 */
open class ExplicitTableAliasCalculator protected constructor(aliases: Map<SqlTable, String>) : TableAliasCalculator {
    private val aliases: Map<SqlTable, String> = Objects.requireNonNull(aliases)

    override fun aliasForColumn(table: SqlTable): Optional<String> {
        return explicitAliasOrTableAlias(table)
    }

    override fun aliasForTable(table: SqlTable): Optional<String> {
        return explicitAliasOrTableAlias(table)
    }

    private fun explicitAliasOrTableAlias(table: SqlTable): Optional<String> {
        val alias = aliases[table]
        return if (alias == null) {
            table.tableAlias()
        } else {
            Optional.of(alias)
        }
    }

    companion object {
        @JvmStatic
        fun of(table: SqlTable, alias: String): TableAliasCalculator {
            val tableAliases = HashMap<SqlTable, String>()
            tableAliases[table] = alias
            return of(tableAliases)
        }

        @JvmStatic
        fun of(aliases: Map<SqlTable, String>): TableAliasCalculator {
            return ExplicitTableAliasCalculator(aliases)
        }
    }
}
