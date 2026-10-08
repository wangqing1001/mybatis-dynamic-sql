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

import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.NullCriterion
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.select.QueryExpressionModel
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.join.JoinType
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.ConfigurableStatement
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.where.WhereApplier
import org.mybatis.dynamic.sql.where.WhereModel
import java.util.ArrayList
import java.util.function.Consumer

/**
 * 用于构建 count 查询的 DSL。count 查询是 select 查询的特化:它们有 join 和 where 子句,
 * 但没有 select 的其它部分(group by、order by 等)。count 查询总是返回一个 long 值。
 * 如果这些限制不可接受,请使用 Select DSL 构建不受限制的 select 语句。
 *
 * @param M 此构建器构建的模型类型,通常为 SelectModel
 * @param D DSL 构建器类型
 */
abstract class AbstractCountDSL<M, D : AbstractCountDSL<M, D>> protected constructor(column: BasicColumn) :
    AbstractQueryingDSL(),
    JoinOperations<AbstractCountDSL<M, D>.JoinSpecificationFinisher>,
    WhereOperations<AbstractCountDSL<M, D>.CountWhereBuilder>,
    ConfigurableStatement<D>,
    Buildable<M> {

    private val countColumn: BasicColumn = column
    private var whereBuilder: CountWhereBuilder? = null
    private val statementConfiguration: StatementConfiguration = StatementConfiguration()

    fun from(table: SqlTable): D {
        setTable(table)
        return getThis()
    }

    fun from(table: SqlTable, tableAlias: String): D {
        setTable(table, tableAlias)
        return getThis()
    }


    override fun where(): CountWhereBuilder {
        var whereBuilder = this.whereBuilder
        if(whereBuilder==null) {
            whereBuilder = CountWhereBuilder(NullCriterion())
            this.whereBuilder = whereBuilder
        }
        return whereBuilder
    }



    override fun where(initialCriterion: SqlCriterion): CountWhereBuilder {
        Validator.assertNull(whereBuilder, Validator.ERROR_32)
        whereBuilder = CountWhereBuilder(initialCriterion)
        return whereBuilder!!
    }

    override fun applyWhere(whereApplier: WhereApplier): CountWhereBuilder {
        Validator.assertNull(whereBuilder, Validator.ERROR_32)
        whereBuilder = CountWhereBuilder(whereApplier.initialCriterion(), whereApplier.subCriteria())
        return whereBuilder!!
    }

    override fun configureStatement(consumer: Consumer<StatementConfiguration>): D {
        consumer.accept(statementConfiguration)
        return getThis()
    }

    protected fun buildSelectModel(): SelectModel {
        val whereModel = whereBuilder?.buildWhereModel()
        val queryExpressionModel = QueryExpressionModel(table(),listOf(countColumn),whereModel,
            tableAliases(),buildJoinModel())
        return SelectModel(listOf(queryExpressionModel),statementConfiguration)
    }

    protected abstract fun getThis(): D

    override fun join(joinType: JoinType, joinTable: TableExpression, initialCriterion: SqlCriterion): JoinSpecificationFinisher {
        val finisher = JoinSpecificationFinisher(joinType, joinTable, initialCriterion)
        addJoinSpecification(finisher)
        return finisher
    }

    override fun join(
        joinType: JoinType,
        joinTable: SqlTable,
        tableAlias: String,
        initialCriterion: SqlCriterion
    ): JoinSpecificationFinisher {
        addTableAlias(joinTable, tableAlias)
        return join(joinType, joinTable, initialCriterion)
    }

    inner class JoinSpecificationFinisher(
        joinType: JoinType,
        joinTable: TableExpression,
        initialCriterion: SqlCriterion
    ) : AbstractJoinSupport<D, JoinSpecificationFinisher>(joinType, joinTable, initialCriterion),
        WhereOperations<CountWhereBuilder>,
        ConfigurableStatement<JoinSpecificationFinisher>,
        Buildable<M> {

        override fun getThis(): JoinSpecificationFinisher {
            return this
        }

        override fun where(): CountWhereBuilder {
            return this@AbstractCountDSL.where()
        }

        override fun where(initialCriterion: SqlCriterion): CountWhereBuilder {
            return this@AbstractCountDSL.where(initialCriterion)
        }

        override fun applyWhere(whereApplier: WhereApplier): CountWhereBuilder {
            return this@AbstractCountDSL.applyWhere(whereApplier)
        }

        override fun build(): M {
            return this@AbstractCountDSL.build()
        }

        override fun configureStatement(consumer: Consumer<StatementConfiguration>): JoinSpecificationFinisher {
            this@AbstractCountDSL.configureStatement(consumer)
            return this
        }

        override fun endJoin(): D {
            return this@AbstractCountDSL.getThis()
        }

        override fun join(joinType: JoinType, joinTable: TableExpression, initialCriterion: SqlCriterion): JoinSpecificationFinisher {
            return this@AbstractCountDSL.join(joinType, joinTable, initialCriterion)
        }

        override fun join(
            joinType: JoinType,
            joinTable: SqlTable,
            tableAlias: String,
            initialCriterion: SqlCriterion
        ): JoinSpecificationFinisher {
            return this@AbstractCountDSL.join(joinType, joinTable, tableAlias, initialCriterion)
        }
    }

    open inner class CountWhereBuilder(
        private val initialCriterion: SqlCriterion,
        subCriteria: List<AndOrCriteriaGroup>
    ) : BooleanOperations<CountWhereBuilder>,ConfigurableStatement<CountWhereBuilder>,Buildable<M> {

        private val subCriteria: MutableList<AndOrCriteriaGroup> = ArrayList()

        constructor(initialCriterion: SqlCriterion): this(initialCriterion, emptyList())

        init {
            this.subCriteria.addAll(subCriteria)
        }

        override fun addSubCriterion(subCriterion: AndOrCriteriaGroup): CountWhereBuilder {
            subCriteria.add(subCriterion)
            return this
        }

        override fun configureStatement(consumer: Consumer<StatementConfiguration>): CountWhereBuilder {
            this@AbstractCountDSL.configureStatement(consumer)
            return this
        }

        override fun build(): M {
            return this@AbstractCountDSL.build()
        }

        fun buildWhereModel(): WhereModel {
            return WhereModel(initialCriterion,subCriteria)
        }
    }
}
