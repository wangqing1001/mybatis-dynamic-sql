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

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.exception.DuplicateTableAliasException
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.SubQuery
import org.mybatis.dynamic.sql.select.join.JoinModel
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.Validator

/**
 * 查询 DSL 的抽象基类。
 *
 * <p>本类不实现任何特定接口,这是有意为之,以便 DSL 实现可以自由组合需要实现的接口。本类只是
 * 多个查询 DSL 实现之间共享通用功能的落地之处。</p>
 */
abstract class AbstractQueryingDSL {
    private val tableAliases: MutableMap<SqlTable, String> = mutableMapOf()
    private var table: TableExpression? = null
    private val joinSpecifications: MutableList<AbstractJoinSupport<*, *>> = mutableListOf()

    fun addTableAlias(table: SqlTable, tableAlias: String) {
        if (tableAliases.containsKey(table)) {
            throw DuplicateTableAliasException(table, tableAlias, tableAliases[table]!!)
        }
        tableAliases[table] = tableAlias
    }

    private fun buildSubQuery(selectModel: Buildable<SelectModel>): SubQuery {
        return SubQuery(selectModel.build())
    }

    private fun buildSubQuery(selectModel: Buildable<SelectModel>, alias: String?): SubQuery {
        return SubQuery(selectModel.build(),alias)
    }

    protected fun tableAliases(): Map<SqlTable, String> {
        return tableAliases
    }

    protected fun table(): TableExpression {
        Validator.assertTrue(table != null, ERROR_27)
        return table!!
    }

    fun setTable(table: SqlTable) {
        Validator.assertNull(this.table, ERROR_27)
        this.table = table
    }

    fun setTable(table: SqlTable, tableAlias: String) {
        Validator.assertNull(this.table, ERROR_27)
        this.table = table
        addTableAlias(table, tableAlias)
    }

    fun setTable(select: Buildable<SelectModel>) {
        Validator.assertNull(this.table, ERROR_27)
        table = buildSubQuery(select)
    }

    fun setTable(select: Buildable<SelectModel>, tableAlias: String) {
        Validator.assertNull(this.table, ERROR_27)
        table = buildSubQuery(select, tableAlias)
    }

    fun addJoinSpecification(joinSpecification: AbstractJoinSupport<*, *>) {
        joinSpecifications.add(joinSpecification)
    }

    fun buildJoinModel(): JoinModel? {
        if (joinSpecifications.isEmpty()) {
            return null
        }
        return JoinModel(joinSpecifications.map { it.toJoinSpecification() })
    }

    companion object {
        private const val ERROR_27 = "ERROR.27" //$NON-NLS-1$
    }
}
