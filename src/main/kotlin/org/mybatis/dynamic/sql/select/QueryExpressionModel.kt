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

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.select.join.JoinModel
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.where.WhereModel
import java.util.Objects
import java.util.Optional
import java.util.stream.Stream

/**
 * 查询表达式模型。表示单个 select 查询的全部信息。
 */
class QueryExpressionModel private constructor(builder: Builder) {
    private val connector: String?
    private val isDistinct: Boolean
    private val selectList: List<BasicColumn>
    private val table: TableExpression
    private val joinModel: JoinModel?
    private val tableAliases: Map<SqlTable, String>
    private val whereModel: WhereModel?
    private val groupByModel: GroupByModel?
    private val havingModel: HavingModel?

    init {
        connector = builder.connector
        isDistinct = builder.isDistinct
        selectList = Objects.requireNonNull(builder.selectList)
        table = builder.table
        joinModel = builder.joinModel
        tableAliases = builder.tableAliases
        whereModel = builder.whereModel
        groupByModel = builder.groupByModel
        havingModel = builder.havingModel
        Validator.assertNotEmpty(selectList, "ERROR.13") //$NON-NLS-1$
    }

    fun connector(): Optional<String> {
        return Optional.ofNullable(connector)
    }

    fun isDistinct(): Boolean {
        return isDistinct
    }

    fun columns(): Stream<BasicColumn> {
        return selectList.stream()
    }

    fun table(): TableExpression {
        return table
    }

    fun tableAliases(): Map<SqlTable, String> {
        return tableAliases
    }

    fun whereModel(): Optional<WhereModel> {
        return Optional.ofNullable(whereModel)
    }

    fun joinModel(): Optional<JoinModel> {
        return Optional.ofNullable(joinModel)
    }

    fun groupByModel(): Optional<GroupByModel> {
        return Optional.ofNullable(groupByModel)
    }

    fun havingModel(): Optional<HavingModel> {
        return Optional.ofNullable(havingModel)
    }

    companion object {
        @JvmStatic
        fun withSelectList(columnList: List<BasicColumn>): Builder {
            return Builder().withSelectList(columnList)
        }
    }

    class Builder {
        var connector: String? = null
        var isDistinct: Boolean = false
        val selectList: MutableList<BasicColumn> = ArrayList()
        lateinit var table: TableExpression
        val tableAliases: MutableMap<SqlTable, String> = HashMap()
        var whereModel: WhereModel? = null
        var joinModel: JoinModel? = null
        var groupByModel: GroupByModel? = null
        var havingModel: HavingModel? = null

        fun withConnector(connector: String?): Builder {
            this.connector = connector
            return this
        }

        fun withTable(table: TableExpression): Builder {
            this.table = table
            return this
        }

        fun isDistinct(isDistinct: Boolean): Builder {
            this.isDistinct = isDistinct
            return this
        }

        fun withSelectColumn(selectColumn: BasicColumn): Builder {
            this.selectList.add(selectColumn)
            return this
        }

        fun withSelectList(selectList: Collection<BasicColumn>): Builder {
            this.selectList.addAll(selectList)
            return this
        }

        fun withTableAliases(tableAliases: Map<SqlTable, String>): Builder {
            this.tableAliases.putAll(tableAliases)
            return this
        }

        fun withWhereModel(whereModel: WhereModel?): Builder {
            this.whereModel = whereModel
            return this
        }

        fun withJoinModel(joinModel: JoinModel?): Builder {
            this.joinModel = joinModel
            return this
        }

        fun withGroupByModel(groupByModel: GroupByModel?): Builder {
            this.groupByModel = groupByModel
            return this
        }

        fun withHavingModel(havingModel: HavingModel?): Builder {
            this.havingModel = havingModel
            return this
        }

        fun build(): QueryExpressionModel {
            return QueryExpressionModel(this)
        }
    }
}
