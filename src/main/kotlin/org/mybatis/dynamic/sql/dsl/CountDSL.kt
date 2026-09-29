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

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.select.SelectModel

/**
 * 用于构建 count 查询的 DSL。count 查询是 select 查询的特化:它们有 join 和 where 子句,
 * 但没有 select 的其它部分(group by、order by 等)。count 查询总是返回一个 long 值。
 * 如果这些限制不可接受,请使用 Select DSL 构建不受限制的 select 语句。
 */
class CountDSL private constructor(column: BasicColumn) : AbstractCountDSL<SelectModel, CountDSL>(column) {

    override fun getThis(): CountDSL {
        return this
    }

    override fun build(): SelectModel {
        return buildSelectModel()
    }

    companion object {
        @JvmStatic
        fun countFrom(table: SqlTable): CountDSL {
            return CountDSL(SqlBuilder.count()).from(table)
        }

        @JvmStatic
        fun countFrom(table: SqlTable, tableAlias: String): CountDSL {
            return CountDSL(SqlBuilder.count()).from(table, tableAlias)
        }

        @JvmStatic
        fun count(column: BasicColumn): CountDSL {
            return CountDSL(SqlBuilder.count(column))
        }

        @JvmStatic
        fun countDistinct(column: BasicColumn): CountDSL {
            return CountDSL(SqlBuilder.countDistinct(column))
        }
    }
}
