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
import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.common.OrderByModel
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.dsl.AbstractLimitAndOffsetSupport
import org.mybatis.dynamic.sql.dsl.ForAndWaitOperations
import org.mybatis.dynamic.sql.dsl.LimitAndOffsetOperations
import org.mybatis.dynamic.sql.dsl.OrderByOperations
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.ConfigurableStatement
import org.mybatis.dynamic.sql.util.Validator
import java.util.ArrayList
import java.util.Arrays
import java.util.Objects
import java.util.function.Consumer
import java.util.function.Function

/**
 * 实现用于构建 select 语句的 SQL DSL。
 *
 * @param R 该构建器生成的模型类型,通常为 SelectModel
 */
class SelectDSL<R> private constructor(adapterFunction: Function<SelectModel, R>) :
    ForAndWaitOperations<SelectDSL<R>>,
    LimitAndOffsetOperations<SelectDSL<R>, R>,
    OrderByOperations<SelectDSL<R>>,
    ConfigurableStatement<SelectDSL<R>>,
    Buildable<R> {

    private val adapterFunction: Function<SelectModel, R>
    private val queryExpressions: MutableList<QueryExpressionDSL<R>> = ArrayList()
    private var orderByModel: OrderByModel? = null
    private val limitAndOffsetSupport: LimitAndOffsetSupport = LimitAndOffsetSupport()
    val statementConfiguration: StatementConfiguration = StatementConfiguration()
    private var forClause: String? = null
    private var waitClause: String? = null

    init {
        this.adapterFunction = Objects.requireNonNull(adapterFunction)
    }

    companion object {
        @JvmStatic
        fun select(vararg selectList: BasicColumn): QueryExpressionDSL<SelectModel> {
            return select(Arrays.asList(*selectList))
        }

        @JvmStatic
        fun select(selectList: Collection<BasicColumn>): QueryExpressionDSL<SelectModel> {
            return select(Function.identity<SelectModel>(), selectList)
        }

        @JvmStatic
        fun <R> select(adapterFunction: Function<SelectModel, R>, vararg selectList: BasicColumn): QueryExpressionDSL<R> {
            return select(adapterFunction, Arrays.asList(*selectList))
        }

        @JvmStatic
        fun <R> select(adapterFunction: Function<SelectModel, R>, selectList: Collection<BasicColumn>): QueryExpressionDSL<R> {
            return QueryExpressionDSL.Builder<R>()
                .withSelectList(selectList)
                .withSelectDSL(SelectDSL(adapterFunction))
                .build()
        }

        @JvmStatic
        fun selectDistinct(vararg selectList: BasicColumn): QueryExpressionDSL<SelectModel> {
            return selectDistinct(Arrays.asList(*selectList))
        }

        @JvmStatic
        fun selectDistinct(selectList: Collection<BasicColumn>): QueryExpressionDSL<SelectModel> {
            return selectDistinct(Function.identity<SelectModel>(), selectList)
        }

        @JvmStatic
        fun <R> selectDistinct(adapterFunction: Function<SelectModel, R>, vararg selectList: BasicColumn): QueryExpressionDSL<R> {
            return selectDistinct(adapterFunction, Arrays.asList(*selectList))
        }

        @JvmStatic
        fun <R> selectDistinct(adapterFunction: Function<SelectModel, R>, selectList: Collection<BasicColumn>): QueryExpressionDSL<R> {
            return QueryExpressionDSL.Builder<R>()
                .withSelectList(selectList)
                .withSelectDSL(SelectDSL(adapterFunction))
                .isDistinct()
                .build()
        }
    }

    fun registerQueryExpression(queryExpression: QueryExpressionDSL<R>) {
        queryExpressions.add(queryExpression)
    }

    override fun orderBy(columns: Collection<SortSpecification>): SelectDSL<R> {
        orderByModel = OrderByModel.of(columns)
        return this
    }

    override fun limitWhenPresent(limit: Long?): LimitAndOffsetOperations.LimitFinisher<SelectDSL<R>, R> {
        return limitAndOffsetSupport.limitWhenPresent(limit)
    }

    override fun offsetWhenPresent(offset: Long?): LimitAndOffsetOperations.OffsetFirstFinisher<SelectDSL<R>, R> {
        return limitAndOffsetSupport.offsetWhenPresent(offset)
    }

    override fun fetchFirstWhenPresent(fetchFirstRows: Long?): LimitAndOffsetOperations.FetchFirstFinisher<SelectDSL<R>> {
        return limitAndOffsetSupport.fetchFirstWhenPresent(fetchFirstRows)
    }

    override fun setWaitClause(waitClause: String): SelectDSL<R> {
        Validator.assertNull(this.waitClause, "ERROR.49") //$NON-NLS-1$
        this.waitClause = waitClause
        return this
    }

    override fun setForClause(forClause: String): SelectDSL<R> {
        Validator.assertNull(this.forClause, "ERROR.48") //$NON-NLS-1$
        this.forClause = forClause
        return this
    }

    override fun configureStatement(consumer: Consumer<StatementConfiguration>): SelectDSL<R> {
        consumer.accept(statementConfiguration)
        return this
    }

    override fun build(): R {
        val selectModel = SelectModel.withQueryExpressions(buildModels())
            .withOrderByModel(orderByModel)
            .withPagingModel(limitAndOffsetSupport.buildPagingModel())
            .withStatementConfiguration(statementConfiguration)
            .withForClause(forClause)
            .withWaitClause(waitClause)
            .build()
        return adapterFunction.apply(selectModel)
    }

    private fun buildModels(): List<QueryExpressionModel> {
        return queryExpressions.stream()
            .map { qe: QueryExpressionDSL<R> -> qe.buildModel() }
            .toList()
    }

    private inner class LimitAndOffsetSupport : AbstractLimitAndOffsetSupport<SelectDSL<R>, R> {

        constructor() : super(this@SelectDSL)

        fun buildPagingModel(): PagingModel? {
            return toPagingModel().orElse(null)
        }

        override fun getThis(): SelectDSL<R> {
            return this@SelectDSL
        }
    }
}
