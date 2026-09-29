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
import java.util.Optional

/**
 * 如果表指定了别名则返回别名,否则返回表名本身。
 * 这对于 join 渲染很有用,当我们总是希望表有别名时。
 *
 * @author Jeff Butler
 */
class GuaranteedTableAliasCalculator private constructor(aliases: Map<SqlTable, String>) :
    ExplicitTableAliasCalculator(aliases) {

    override fun aliasForColumn(table: SqlTable): Optional<String> {
        val alias = super.aliasForColumn(table)
        return if (alias.isPresent) {
            alias
        } else {
            Optional.of(table.tableName())
        }
    }

    companion object {
        @JvmStatic
        fun of(aliases: Map<SqlTable, String>): TableAliasCalculator {
            return GuaranteedTableAliasCalculator(aliases)
        }
    }
}
