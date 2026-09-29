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
import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.NullCriterion
import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.dsl.AbstractJoinSupport
import org.mybatis.dynamic.sql.dsl.AbstractQueryingDSL
import org.mybatis.dynamic.sql.dsl.BooleanOperations
import org.mybatis.dynamic.sql.dsl.ForAndWaitOperations
import org.mybatis.dynamic.sql.dsl.GroupByOperations
import org.mybatis.dynamic.sql.dsl.HavingOperations
import org.mybatis.dynamic.sql.dsl.JoinOperations
import org.mybatis.dynamic.sql.dsl.LimitAndOffsetOperations
import org.mybatis.dynamic.sql.dsl.OrderByOperations
import org.mybatis.dynamic.sql.dsl.WhereOperations
import org.mybatis.dynamic.sql.select.join.JoinType
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.ConfigurableStatement
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.where.WhereApplier
import org.mybatis.dynamic.sql.where.WhereModel
import java.util.ArrayList
import java.util.Arrays
import java.util.Objects
import java.util.function.Consumer

/**
 * 查询表达式 DSL。表示 select 语句中的一个查询表达式。
 *
 * @param R 该 DSL 构建的模型类型,通常为 SelectModel
 */
open class QueryExpressionDSL<R> protected constructor(builder: Builder<R>) : AbstractQueryingDSL(),
    JoinOperations<QueryExpressionDSL<R>.JoinSpecificationFinisher>,
    WhereOperations<QueryExpressionDSL<R>.QueryExpressionWhereBuilder>,
    GroupByOperations<QueryExpressionDSL<R>>,
    HavingOperations<QueryExpressionDSL<R>.QueryExpressionHavingBuilder>,
    ConfigurableStatement<QueryExpressionDSL<R>>,
    LimitAndOffsetOperations<SelectDSL<R>, R>,
    ForAndWaitOperations<SelectDSL<R>>,
    OrderByOperations<SelectDSL<R>>,
    Buildable<R> {

    private val connector: String?
    private val selectDSL: SelectDSL<R>
    private val isDistinct: Boolean
    private val selectList: List<BasicColumn>
    private var whereBuilder: QueryExpressionWhereBuilder? = null
    private var groupByModel: GroupByModel? = null
    private var havingBuilder: QueryExpressionHavingBuilder? = null

    init {
        connector = builder.connector
        selectList = builder.selectList
        isDistinct = builder.isDistinct
        selectDSL = Objects.requireNonNull(builder.selectDSL!!)
        selectDSL.registerQueryExpression(this)
    }

    fun from(select: Buildable<SelectModel>): QueryExpressionDSL<R> {
        setTable(select)
        return this
    }

    fun from(select: Buildable<SelectModel>, tableAlias: String): QueryExpressionDSL<R> {
        setTable(select, tableAlias)
        return this
    }

    fun from(table: SqlTable): QueryExpressionDSL<R> {
        setTable(table)
        return this
    }

    fun from(table: SqlTable, tableAlias: String): QueryExpressionDSL<R> {
        setTable(table, tableAlias)
        return this
    }

    override fun join(joinType: JoinType, joinTable: TableExpression, initialCriterion: SqlCriterion): JoinSpecificationFinisher {
        val finisher = JoinSpecificationFinisher(joinType, joinTable, initialCriterion)
        addJoinSpecification(finisher)
        return finisher
    }

    override fun join(joinType: JoinType, joinTable: SqlTable, tableAlias: String, initialCriterion: SqlCriterion): JoinSpecificationFinisher {
        addTableAlias(joinTable, tableAlias)
        return join(joinType, joinTable, initialCriterion)
    }

    override fun where(): QueryExpressionWhereBuilder {
        whereBuilder = Objects.requireNonNullElseGet(whereBuilder) { QueryExpressionWhereBuilder(NullCriterion()) }
        return whereBuilder!!
    }

    override fun where(initialCriterion: SqlCriterion): QueryExpressionWhereBuilder {
        Validator.assertNull(whereBuilder, Validator.ERROR_32)
        whereBuilder = QueryExpressionWhereBuilder(initialCriterion)
        return whereBuilder!!
    }

    override fun applyWhere(whereApplier: WhereApplier): QueryExpressionWhereBuilder {
        Validator.assertNull(whereBuilder, Validator.ERROR_32)
        whereBuilder = QueryExpressionWhereBuilder(whereApplier.initialCriterion(), whereApplier.subCriteria())
        return whereBuilder!!
    }

    override fun configureStatement(consumer: Consumer<StatementConfiguration>): QueryExpressionDSL<R> {
        selectDSL.configureStatement(consumer)
        return this
    }

    override fun having(initialCriterion: SqlCriterion): QueryExpressionHavingBuilder {
        Validator.assertNull(havingBuilder, "ERROR.31") //$NON-NLS-1$
        havingBuilder = QueryExpressionHavingBuilder(initialCriterion)
        return havingBuilder!!
    }

    override fun applyHaving(havingApplier: HavingApplier): QueryExpressionHavingBuilder {
        Validator.assertNull(havingBuilder, "ERROR.31") //$NON-NLS-1$
        havingBuilder = QueryExpressionHavingBuilder(havingApplier.initialCriterion(), havingApplier.subCriteria())
        return havingBuilder!!
    }

    override fun build(): R {
        return selectDSL.build()
    }

    override fun groupBy(columns: Collection<BasicColumn>): QueryExpressionDSL<R> {
        groupByModel = GroupByModel.of(columns)
        return this
    }

    override fun orderBy(columns: Collection<SortSpecification>): SelectDSL<R> {
        return selectDSL.orderBy(columns)
    }

    fun union(): UnionBuilder {
        return UnionBuilder("union") //$NON-NLS-1$
    }

    fun unionAll(): UnionBuilder {
        return UnionBuilder("union all") //$NON-NLS-1$
    }

    internal fun buildModel(): QueryExpressionModel {
        return QueryExpressionModel.withSelectList(selectList)
            .withConnector(connector)
            .withTable(table())
            .isDistinct(isDistinct)
            .withTableAliases(tableAliases())
            .withJoinModel(buildJoinModel())
            .withGroupByModel(groupByModel)
            .withWhereModel(if (whereBuilder == null) null else whereBuilder!!.buildWhereModel())
            .withHavingModel(if (havingBuilder == null) null else havingBuilder!!.buildHavingModel())
            .build()
    }

    override fun setWaitClause(waitClause: String): SelectDSL<R> {
        return selectDSL.setWaitClause(waitClause)
    }

    override fun setForClause(forClause: String): SelectDSL<R> {
        return selectDSL.setForClause(forClause)
    }

    override fun limitWhenPresent(limit: Long?): LimitAndOffsetOperations.LimitFinisher<SelectDSL<R>, R> {
        return selectDSL.limitWhenPresent(limit)
    }

    override fun offsetWhenPresent(offset: Long?): LimitAndOffsetOperations.OffsetFirstFinisher<SelectDSL<R>, R> {
        return selectDSL.offsetWhenPresent(offset)
    }

    override fun fetchFirstWhenPresent(fetchFirstRows: Long?): LimitAndOffsetOperations.FetchFirstFinisher<SelectDSL<R>> {
        return selectDSL.fetchFirstWhenPresent(fetchFirstRows)
    }

    /**
     * where 构建器。
     */
    inner class QueryExpressionWhereBuilder @JvmOverloads constructor(
        private val initialCriterion: SqlCriterion,
        subCriteria: List<AndOrCriteriaGroup>? = null
    ) : BooleanOperations<QueryExpressionWhereBuilder>,
        ConfigurableStatement<QueryExpressionWhereBuilder>,
        OrderByOperations<SelectDSL<R>>,
        GroupByOperations<QueryExpressionDSL<R>>,
        ForAndWaitOperations<SelectDSL<R>>,
        LimitAndOffsetOperations<SelectDSL<R>, R>,
        Buildable<R> {

        private val subCriteria: MutableList<AndOrCriteriaGroup> = ArrayList()

        init {
            if (subCriteria != null) {
                this.subCriteria.addAll(subCriteria)
            }
        }

        override fun addSubCriterion(subCriterion: AndOrCriteriaGroup): QueryExpressionWhereBuilder {
            subCriteria.add(subCriterion)
            return this
        }

        fun union(): UnionBuilder {
            return this@QueryExpressionDSL.union()
        }

        fun unionAll(): UnionBuilder {
            return this@QueryExpressionDSL.unionAll()
        }

        override fun orderBy(columns: Collection<SortSpecification>): SelectDSL<R> {
            return this@QueryExpressionDSL.orderBy(columns)
        }

        override fun groupBy(columns: Collection<BasicColumn>): QueryExpressionDSL<R> {
            return this@QueryExpressionDSL.groupBy(columns)
        }

        override fun configureStatement(consumer: Consumer<StatementConfiguration>): QueryExpressionWhereBuilder {
            this@QueryExpressionDSL.configureStatement(consumer)
            return this
        }

        override fun build(): R {
            return this@QueryExpressionDSL.build()
        }

        override fun setWaitClause(waitClause: String): SelectDSL<R> {
            return this@QueryExpressionDSL.setWaitClause(waitClause)
        }

        override fun setForClause(forClause: String): SelectDSL<R> {
            return this@QueryExpressionDSL.setForClause(forClause)
        }

        override fun limitWhenPresent(limit: Long?): LimitAndOffsetOperations.LimitFinisher<SelectDSL<R>, R> {
            return this@QueryExpressionDSL.limitWhenPresent(limit)
        }

        override fun offsetWhenPresent(offset: Long?): LimitAndOffsetOperations.OffsetFirstFinisher<SelectDSL<R>, R> {
            return this@QueryExpressionDSL.offsetWhenPresent(offset)
        }

        override fun fetchFirstWhenPresent(fetchFirstRows: Long?): LimitAndOffsetOperations.FetchFirstFinisher<SelectDSL<R>> {
            return this@QueryExpressionDSL.fetchFirstWhenPresent(fetchFirstRows)
        }

        fun buildWhereModel(): WhereModel {
            return WhereModel.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build()
        }
    }

    /**
     * join 完成器。
     */
    inner class JoinSpecificationFinisher(
        joinType: JoinType,
        joinTable: TableExpression,
        initialCriterion: SqlCriterion
    ) : AbstractJoinSupport<QueryExpressionDSL<R>, JoinSpecificationFinisher>(
        joinType, joinTable, initialCriterion
    ),
        WhereOperations<QueryExpressionWhereBuilder>,
        ConfigurableStatement<JoinSpecificationFinisher>,
        GroupByOperations<QueryExpressionDSL<R>>,
        ForAndWaitOperations<SelectDSL<R>>,
        LimitAndOffsetOperations<SelectDSL<R>, R>,
        OrderByOperations<SelectDSL<R>>,
        Buildable<R> {

        override fun build(): R {
            return this@QueryExpressionDSL.build()
        }

        override fun configureStatement(consumer: Consumer<StatementConfiguration>): JoinSpecificationFinisher {
            selectDSL.configureStatement(consumer)
            return this
        }

        override fun where(): QueryExpressionWhereBuilder {
            return this@QueryExpressionDSL.where()
        }

        override fun where(initialCriterion: SqlCriterion): QueryExpressionWhereBuilder {
            return this@QueryExpressionDSL.where(initialCriterion)
        }

        override fun applyWhere(whereApplier: WhereApplier): QueryExpressionWhereBuilder {
            return this@QueryExpressionDSL.applyWhere(whereApplier)
        }

        override fun groupBy(columns: Collection<BasicColumn>): QueryExpressionDSL<R> {
            return this@QueryExpressionDSL.groupBy(columns)
        }

        fun union(): UnionBuilder {
            return this@QueryExpressionDSL.union()
        }

        fun unionAll(): UnionBuilder {
            return this@QueryExpressionDSL.unionAll()
        }

        override fun orderBy(columns: Collection<SortSpecification>): SelectDSL<R> {
            return this@QueryExpressionDSL.orderBy(columns)
        }

        override fun getThis(): JoinSpecificationFinisher {
            return this
        }

        override fun join(joinType: JoinType, joinTable: TableExpression, initialCriterion: SqlCriterion): JoinSpecificationFinisher {
            return this@QueryExpressionDSL.join(joinType, joinTable, initialCriterion)
        }

        override fun join(joinType: JoinType, joinTable: SqlTable, tableAlias: String, initialCriterion: SqlCriterion): JoinSpecificationFinisher {
            return this@QueryExpressionDSL.join(joinType, joinTable, tableAlias, initialCriterion)
        }

        override fun endJoin(): QueryExpressionDSL<R> {
            return this@QueryExpressionDSL
        }

        override fun limitWhenPresent(limit: Long?): LimitAndOffsetOperations.LimitFinisher<SelectDSL<R>, R> {
            return this@QueryExpressionDSL.limitWhenPresent(limit)
        }

        override fun offsetWhenPresent(offset: Long?): LimitAndOffsetOperations.OffsetFirstFinisher<SelectDSL<R>, R> {
            return this@QueryExpressionDSL.offsetWhenPresent(offset)
        }

        override fun fetchFirstWhenPresent(fetchFirstRows: Long?): LimitAndOffsetOperations.FetchFirstFinisher<SelectDSL<R>> {
            return this@QueryExpressionDSL.fetchFirstWhenPresent(fetchFirstRows)
        }

        override fun setWaitClause(waitClause: String): SelectDSL<R> {
            return this@QueryExpressionDSL.setWaitClause(waitClause)
        }

        override fun setForClause(forClause: String): SelectDSL<R> {
            return this@QueryExpressionDSL.setForClause(forClause)
        }
    }

    /**
     * union 构建器。
     */
    inner class UnionBuilder(val connector: String) {

        fun select(vararg selectList: BasicColumn): QueryExpressionDSL<R> {
            return select(Arrays.asList(*selectList))
        }

        fun select(selectList: List<BasicColumn>): QueryExpressionDSL<R> {
            return Builder<R>()
                .withConnector(connector)
                .withSelectList(selectList)
                .withSelectDSL(selectDSL)
                .build()
        }

        fun selectDistinct(vararg selectList: BasicColumn): QueryExpressionDSL<R> {
            return selectDistinct(Arrays.asList(*selectList))
        }

        fun selectDistinct(selectList: List<BasicColumn>): QueryExpressionDSL<R> {
            return Builder<R>()
                .withConnector(connector)
                .withSelectList(selectList)
                .withSelectDSL(selectDSL)
                .isDistinct()
                .build()
        }
    }

    /**
     * having 构建器。
     */
    inner class QueryExpressionHavingBuilder @JvmOverloads constructor(
        private val initialCriterion: SqlCriterion,
        subCriteria: List<AndOrCriteriaGroup>? = null
    ) : BooleanOperations<QueryExpressionHavingBuilder>,
        ForAndWaitOperations<SelectDSL<R>>,
        LimitAndOffsetOperations<SelectDSL<R>, R>,
        OrderByOperations<SelectDSL<R>>,
        Buildable<R> {

        private val subCriteria: MutableList<AndOrCriteriaGroup> = ArrayList()

        init {
            if (subCriteria != null) {
                this.subCriteria.addAll(subCriteria)
            }
        }

        override fun addSubCriterion(subCriterion: AndOrCriteriaGroup): QueryExpressionHavingBuilder {
            subCriteria.add(subCriterion)
            return this
        }

        override fun orderBy(columns: Collection<SortSpecification>): SelectDSL<R> {
            return this@QueryExpressionDSL.orderBy(columns)
        }

        fun union(): UnionBuilder {
            return this@QueryExpressionDSL.union()
        }

        fun unionAll(): UnionBuilder {
            return this@QueryExpressionDSL.unionAll()
        }

        override fun build(): R {
            return this@QueryExpressionDSL.build()
        }

        fun buildHavingModel(): HavingModel {
            return HavingModel.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build()
        }

        override fun setWaitClause(waitClause: String): SelectDSL<R> {
            return this@QueryExpressionDSL.setWaitClause(waitClause)
        }

        override fun setForClause(forClause: String): SelectDSL<R> {
            return this@QueryExpressionDSL.setForClause(forClause)
        }

        override fun limitWhenPresent(limit: Long?): LimitAndOffsetOperations.LimitFinisher<SelectDSL<R>, R> {
            return this@QueryExpressionDSL.limitWhenPresent(limit)
        }

        override fun offsetWhenPresent(offset: Long?): LimitAndOffsetOperations.OffsetFirstFinisher<SelectDSL<R>, R> {
            return this@QueryExpressionDSL.offsetWhenPresent(offset)
        }

        override fun fetchFirstWhenPresent(fetchFirstRows: Long?): LimitAndOffsetOperations.FetchFirstFinisher<SelectDSL<R>> {
            return this@QueryExpressionDSL.fetchFirstWhenPresent(fetchFirstRows)
        }
    }

    /**
     * 构建器。
     */
    class Builder<R> {
        var connector: String? = null
        val selectList: MutableList<BasicColumn> = ArrayList()
        var selectDSL: SelectDSL<R>? = null
        var isDistinct: Boolean = false

        fun withConnector(connector: String?): Builder<R> {
            this.connector = connector
            return this
        }

        fun withSelectList(selectList: Collection<BasicColumn>): Builder<R> {
            this.selectList.addAll(selectList)
            return this
        }

        fun withSelectDSL(selectDSL: SelectDSL<R>): Builder<R> {
            this.selectDSL = selectDSL
            return this
        }

        fun isDistinct(): Builder<R> {
            this.isDistinct = true
            return this
        }

        fun build(): QueryExpressionDSL<R> {
            return QueryExpressionDSL(this)
        }
    }
}
