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
import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.common.OrderByModel
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.select.GroupByModel
import org.mybatis.dynamic.sql.select.HavingApplier
import org.mybatis.dynamic.sql.select.HavingModel
import org.mybatis.dynamic.sql.select.PagingModel
import org.mybatis.dynamic.sql.select.QueryExpressionModel
import org.mybatis.dynamic.sql.select.SelectModel
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
 * select 语句 DSL。支持 from、join、where、group by、having、order by、limit/offset、
 * for/wait 子句与 union 查询。
 */
class SelectDSL private constructor(selectList: Collection<BasicColumn>, isDistinct: Boolean) :
    JoinOperations<SelectDSL.JoinSpecificationFinisher>,
    WhereOperations<SelectDSL.QueryExpressionWhereBuilder>,
    OrderByOperations<SelectDSL>,
    GroupByOperations<SelectDSL>,
    HavingOperations<SelectDSL.QueryExpressionHavingBuilder>,
    LimitAndOffsetOperations<SelectDSL, SelectModel>,
    ForAndWaitOperations<SelectDSL>,
    ConfigurableStatement<SelectDSL>,
    Buildable<SelectModel> {

    private val statementConfiguration: StatementConfiguration = StatementConfiguration()
    private var currentQueryValues: CurrentQueryValues = CurrentQueryValues()
    private val unionQueries: MutableList<QueryExpressionModel> = ArrayList()
    private var orderByModel: OrderByModel? = null
    private val limitAndOffsetSupport: LimitAndOffsetSupport = LimitAndOffsetSupport()
    private var forClause: String? = null
    private var waitClause: String? = null

    init {
        currentQueryValues.builder.withSelectList(selectList)
        currentQueryValues.builder.isDistinct(isDistinct)
    }

    companion object {
        @JvmStatic
        fun select(vararg selectList: BasicColumn): SelectDSL {
            return select(listOf(*selectList))
        }

        @JvmStatic
        fun select(selectList: Collection<BasicColumn>): SelectDSL {
            return SelectDSL(selectList, false)
        }

        @JvmStatic
        fun selectDistinct(vararg selectList: BasicColumn): SelectDSL {
            return selectDistinct(listOf(*selectList))
        }

        @JvmStatic
        fun selectDistinct(selectList: Collection<BasicColumn>): SelectDSL {
            return SelectDSL(selectList, true)
        }
    }

    fun from(select: Buildable<SelectModel>): SelectDSL {
        currentQueryValues.setTable(select)
        return this
    }

    fun from(select: Buildable<SelectModel>, tableAlias: String): SelectDSL {
        currentQueryValues.setTable(select, tableAlias)
        return this
    }

    fun from(table: SqlTable): SelectDSL {
        currentQueryValues.setTable(table)
        return this
    }

    fun from(table: SqlTable, tableAlias: String): SelectDSL {
        currentQueryValues.setTable(table, tableAlias)
        return this
    }

    override fun join(joinType: JoinType, joinTable: TableExpression, initialCriterion: SqlCriterion): JoinSpecificationFinisher {
        val finisher = JoinSpecificationFinisher(joinType, joinTable, initialCriterion)
        currentQueryValues.addJoinSpecification(finisher)
        return finisher
    }

    override fun join(
        joinType: JoinType,
        joinTable: SqlTable,
        tableAlias: String,
        initialCriterion: SqlCriterion
    ): JoinSpecificationFinisher {
        currentQueryValues.addTableAlias(joinTable, tableAlias)
        return join(joinType, joinTable, initialCriterion)
    }

    override fun where(): QueryExpressionWhereBuilder {
        currentQueryValues.whereBuilder = Objects.requireNonNullElseGet(currentQueryValues.whereBuilder) {
            QueryExpressionWhereBuilder(NullCriterion())
        }
        return currentQueryValues.whereBuilder!!
    }

    override fun where(initialCriterion: SqlCriterion): QueryExpressionWhereBuilder {
        Validator.assertNull(currentQueryValues.whereBuilder, Validator.ERROR_32)
        currentQueryValues.whereBuilder = QueryExpressionWhereBuilder(initialCriterion)
        return currentQueryValues.whereBuilder!!
    }

    override fun applyWhere(whereApplier: WhereApplier): QueryExpressionWhereBuilder {
        Validator.assertNull(currentQueryValues.whereBuilder, Validator.ERROR_32)
        currentQueryValues.whereBuilder =
            QueryExpressionWhereBuilder(whereApplier.initialCriterion(), whereApplier.subCriteria())
        return currentQueryValues.whereBuilder!!
    }

    override fun orderBy(columns: Collection<SortSpecification>): SelectDSL {
        orderByModel = OrderByModel.of(columns)
        return this
    }

    override fun groupBy(columns: Collection<BasicColumn>): SelectDSL {
        currentQueryValues.builder.withGroupByModel(GroupByModel.of(columns))
        return this
    }

    override fun having(initialCriterion: SqlCriterion): QueryExpressionHavingBuilder {
        Validator.assertNull(currentQueryValues.havingBuilder, "ERROR.31") //$NON-NLS-1$
        currentQueryValues.havingBuilder = QueryExpressionHavingBuilder(initialCriterion)
        return currentQueryValues.havingBuilder!!
    }

    override fun applyHaving(havingApplier: HavingApplier): QueryExpressionHavingBuilder {
        Validator.assertNull(currentQueryValues.havingBuilder, "ERROR.31") //$NON-NLS-1$
        currentQueryValues.havingBuilder =
            QueryExpressionHavingBuilder(havingApplier.initialCriterion(), havingApplier.subCriteria())
        return currentQueryValues.havingBuilder!!
    }

    override fun limitWhenPresent(limit: Long?): LimitAndOffsetOperations.LimitFinisher<SelectDSL, SelectModel> {
        return limitAndOffsetSupport.limitWhenPresent(limit)
    }

    override fun offsetWhenPresent(offset: Long?): LimitAndOffsetOperations.OffsetFirstFinisher<SelectDSL, SelectModel> {
        return limitAndOffsetSupport.offsetWhenPresent(offset)
    }

    override fun fetchFirstWhenPresent(fetchFirstRows: Long?): LimitAndOffsetOperations.FetchFirstFinisher<SelectDSL> {
        return limitAndOffsetSupport.fetchFirstWhenPresent(fetchFirstRows)
    }

    override fun setWaitClause(waitClause: String): SelectDSL {
        Validator.assertNull(this.waitClause, "ERROR.49") //$NON-NLS-1$
        this.waitClause = waitClause
        return this
    }

    override fun setForClause(forClause: String): SelectDSL {
        Validator.assertNull(this.forClause, "ERROR.48") //$NON-NLS-1$
        this.forClause = forClause
        return this
    }

    fun union(): UnionBuilder {
        return UnionBuilder("union") //$NON-NLS-1$
    }

    fun unionAll(): UnionBuilder {
        return UnionBuilder("union all") //$NON-NLS-1$
    }

    override fun configureStatement(consumer: Consumer<StatementConfiguration>): SelectDSL {
        consumer.accept(statementConfiguration)
        return this
    }

    override fun build(): SelectModel {
        return SelectModel.Builder()
            .withStatementConfiguration(statementConfiguration)
            .withQueryExpressions(unionQueries)
            .withQueryExpression(currentQueryValues.toQueryExpressionModel())
            .withOrderByModel(orderByModel)
            .withPagingModel(limitAndOffsetSupport.buildPagingModel())
            .withForClause(forClause)
            .withWaitClause(waitClause)
            .build()
    }

    private inner class CurrentQueryValues : AbstractQueryingDSL() {
        // 字段公开,以便外部类 SelectDSL 访问(Kotlin 嵌套类与 Java 不同,外部类无法访问嵌套类私有成员)
        val builder: QueryExpressionModel.Builder = QueryExpressionModel.Builder()
        var whereBuilder: QueryExpressionWhereBuilder? = null
        var havingBuilder: QueryExpressionHavingBuilder? = null

        fun toQueryExpressionModel(): QueryExpressionModel {
            return builder
                .withTableAliases(tableAliases())
                .withTable(table())
                .withJoinModel(buildJoinModel())
                .withWhereModel(if (whereBuilder == null) null else whereBuilder!!.buildWhereModel())
                .withHavingModel(if (havingBuilder == null) null else havingBuilder!!.buildHavingModel())
                .build()
        }
    }

    inner class QueryExpressionWhereBuilder @JvmOverloads constructor(
        private val initialCriterion: SqlCriterion,
        subCriteria: List<AndOrCriteriaGroup>? = null
    ) : BooleanOperations<QueryExpressionWhereBuilder>,
        ConfigurableStatement<QueryExpressionWhereBuilder>,
        OrderByOperations<SelectDSL>,
        GroupByOperations<SelectDSL>,
        LimitAndOffsetOperations<SelectDSL, SelectModel>,
        ForAndWaitOperations<SelectDSL>,
        Buildable<SelectModel> {

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
            return this@SelectDSL.union()
        }

        fun unionAll(): UnionBuilder {
            return this@SelectDSL.unionAll()
        }

        override fun limitWhenPresent(limit: Long?): LimitAndOffsetOperations.LimitFinisher<SelectDSL, SelectModel> {
            return this@SelectDSL.limitWhenPresent(limit)
        }

        override fun offsetWhenPresent(offset: Long?): LimitAndOffsetOperations.OffsetFirstFinisher<SelectDSL, SelectModel> {
            return this@SelectDSL.offsetWhenPresent(offset)
        }

        override fun fetchFirstWhenPresent(fetchFirstRows: Long?): LimitAndOffsetOperations.FetchFirstFinisher<SelectDSL> {
            return this@SelectDSL.fetchFirstWhenPresent(fetchFirstRows)
        }

        override fun orderBy(columns: Collection<SortSpecification>): SelectDSL {
            return this@SelectDSL.orderBy(columns)
        }

        override fun groupBy(columns: Collection<BasicColumn>): SelectDSL {
            return this@SelectDSL.groupBy(columns)
        }

        override fun build(): SelectModel {
            return this@SelectDSL.build()
        }

        fun buildWhereModel(): WhereModel {
            return WhereModel.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build()
        }

        override fun configureStatement(consumer: Consumer<StatementConfiguration>): QueryExpressionWhereBuilder {
            this@SelectDSL.configureStatement(consumer)
            return this
        }

        override fun setWaitClause(waitClause: String): SelectDSL {
            return this@SelectDSL.setWaitClause(waitClause)
        }

        override fun setForClause(forClause: String): SelectDSL {
            return this@SelectDSL.setForClause(forClause)
        }
    }

    inner class JoinSpecificationFinisher(
        joinType: JoinType,
        joinTable: TableExpression,
        initialCriterion: SqlCriterion
    ) : AbstractJoinSupport<SelectDSL, JoinSpecificationFinisher>(joinType, joinTable, initialCriterion),
        WhereOperations<QueryExpressionWhereBuilder>,
        ConfigurableStatement<JoinSpecificationFinisher>,
        GroupByOperations<SelectDSL>,
        OrderByOperations<SelectDSL>,
        LimitAndOffsetOperations<SelectDSL, SelectModel>,
        ForAndWaitOperations<SelectDSL>,
        Buildable<SelectModel> {

        override fun build(): SelectModel {
            return this@SelectDSL.build()
        }

        override fun configureStatement(consumer: Consumer<StatementConfiguration>): JoinSpecificationFinisher {
            this@SelectDSL.configureStatement(consumer)
            return this
        }

        override fun where(): QueryExpressionWhereBuilder {
            return this@SelectDSL.where()
        }

        override fun where(initialCriterion: SqlCriterion): QueryExpressionWhereBuilder {
            return this@SelectDSL.where(initialCriterion)
        }

        override fun applyWhere(whereApplier: WhereApplier): QueryExpressionWhereBuilder {
            return this@SelectDSL.applyWhere(whereApplier)
        }

        override fun groupBy(columns: Collection<BasicColumn>): SelectDSL {
            return this@SelectDSL.groupBy(columns)
        }

        fun union(): UnionBuilder {
            return this@SelectDSL.union()
        }

        fun unionAll(): UnionBuilder {
            return this@SelectDSL.unionAll()
        }

        override fun orderBy(columns: Collection<SortSpecification>): SelectDSL {
            return this@SelectDSL.orderBy(columns)
        }

        override fun getThis(): JoinSpecificationFinisher {
            return this
        }

        override fun limitWhenPresent(limit: Long?): LimitAndOffsetOperations.LimitFinisher<SelectDSL, SelectModel> {
            return this@SelectDSL.limitWhenPresent(limit)
        }

        override fun offsetWhenPresent(offset: Long?): LimitAndOffsetOperations.OffsetFirstFinisher<SelectDSL, SelectModel> {
            return this@SelectDSL.offsetWhenPresent(offset)
        }

        override fun fetchFirstWhenPresent(fetchFirstRows: Long?): LimitAndOffsetOperations.FetchFirstFinisher<SelectDSL> {
            return this@SelectDSL.fetchFirstWhenPresent(fetchFirstRows)
        }

        override fun setWaitClause(waitClause: String): SelectDSL {
            return this@SelectDSL.setWaitClause(waitClause)
        }

        override fun setForClause(forClause: String): SelectDSL {
            return this@SelectDSL.setForClause(forClause)
        }

        override fun join(joinType: JoinType, joinTable: TableExpression, initialCriterion: SqlCriterion): JoinSpecificationFinisher {
            return this@SelectDSL.join(joinType, joinTable, initialCriterion)
        }

        override fun join(
            joinType: JoinType,
            joinTable: SqlTable,
            tableAlias: String,
            initialCriterion: SqlCriterion
        ): JoinSpecificationFinisher {
            return this@SelectDSL.join(joinType, joinTable, tableAlias, initialCriterion)
        }

        override fun endJoin(): SelectDSL {
            return this@SelectDSL
        }
    }

    inner class QueryExpressionHavingBuilder @JvmOverloads constructor(
        private val initialCriterion: SqlCriterion,
        subCriteria: List<AndOrCriteriaGroup>? = null
    ) : BooleanOperations<QueryExpressionHavingBuilder>,
        OrderByOperations<SelectDSL>,
        LimitAndOffsetOperations<SelectDSL, SelectModel>,
        ForAndWaitOperations<SelectDSL>,
        Buildable<SelectModel> {

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

        override fun orderBy(columns: Collection<SortSpecification>): SelectDSL {
            return this@SelectDSL.orderBy(columns)
        }

        fun union(): UnionBuilder {
            return this@SelectDSL.union()
        }

        fun unionAll(): UnionBuilder {
            return this@SelectDSL.unionAll()
        }

        override fun build(): SelectModel {
            return this@SelectDSL.build()
        }

        fun buildHavingModel(): HavingModel {
            return HavingModel.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build()
        }

        override fun limitWhenPresent(limit: Long?): LimitAndOffsetOperations.LimitFinisher<SelectDSL, SelectModel> {
            return this@SelectDSL.limitWhenPresent(limit)
        }

        override fun offsetWhenPresent(offset: Long?): LimitAndOffsetOperations.OffsetFirstFinisher<SelectDSL, SelectModel> {
            return this@SelectDSL.offsetWhenPresent(offset)
        }

        override fun fetchFirstWhenPresent(fetchFirstRows: Long?): LimitAndOffsetOperations.FetchFirstFinisher<SelectDSL> {
            return this@SelectDSL.fetchFirstWhenPresent(fetchFirstRows)
        }

        override fun setWaitClause(waitClause: String): SelectDSL {
            return this@SelectDSL.setWaitClause(waitClause)
        }

        override fun setForClause(forClause: String): SelectDSL {
            return this@SelectDSL.setForClause(forClause)
        }
    }

    private inner class LimitAndOffsetSupport : AbstractLimitAndOffsetSupport<SelectDSL, SelectModel> {
        constructor() : super(this@SelectDSL)

        fun buildPagingModel(): PagingModel? {
            return toPagingModel().orElse(null)
        }

        override fun getThis(): SelectDSL {
            return this@SelectDSL
        }
    }

    inner class UnionBuilder(val connector: String) {

        fun select(vararg selectList: BasicColumn): SelectDSL {
            return select(Arrays.asList(*selectList))
        }

        fun select(selectList: List<BasicColumn>): SelectDSL {
            unionQueries.add(currentQueryValues.toQueryExpressionModel())
            currentQueryValues = CurrentQueryValues()
            currentQueryValues.builder.withConnector(connector)
            currentQueryValues.builder.withSelectList(selectList)
            return this@SelectDSL
        }

        fun selectDistinct(vararg selectList: BasicColumn): SelectDSL {
            return selectDistinct(Arrays.asList(*selectList))
        }

        fun selectDistinct(selectList: List<BasicColumn>): SelectDSL {
            select(selectList)
            currentQueryValues.builder.isDistinct(true)
            return this@SelectDSL
        }
    }
}
