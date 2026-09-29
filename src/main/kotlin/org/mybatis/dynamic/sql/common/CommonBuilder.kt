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
package org.mybatis.dynamic.sql.common

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.where.WhereModel

/**
 * 在 delete 和 update 模型构建器之间共享的构建器类。
 *
 * @param <T> 实现构建器的类型
 */
abstract class CommonBuilder<T : CommonBuilder<T>> {
    private lateinit var table: SqlTable
    private lateinit var statementConfiguration: StatementConfiguration
    private var tableAlias: String? = null
    private var whereModel: WhereModel? = null
    private var limit: Long? = null
    private var orderByModel: OrderByModel? = null

    fun table(): SqlTable {
        return table
    }

    fun tableAlias(): String? {
        return tableAlias
    }

    fun whereModel(): WhereModel? {
        return whereModel
    }

    fun limit(): Long? {
        return limit
    }

    fun orderByModel(): OrderByModel? {
        return orderByModel
    }

    fun statementConfiguration(): StatementConfiguration {
        return statementConfiguration
    }

    fun withTable(table: SqlTable): T {
        this.table = table
        return self()
    }

    fun withTableAlias(tableAlias: String?): T {
        this.tableAlias = tableAlias
        return self()
    }

    fun withWhereModel(whereModel: WhereModel?): T {
        this.whereModel = whereModel
        return self()
    }

    fun withLimit(limit: Long?): T {
        this.limit = limit
        return self()
    }

    fun withOrderByModel(orderByModel: OrderByModel?): T {
        this.orderByModel = orderByModel
        return self()
    }

    fun withStatementConfiguration(statementConfiguration: StatementConfiguration): T {
        this.statementConfiguration = statementConfiguration
        return self()
    }

    protected abstract fun self(): T
}
