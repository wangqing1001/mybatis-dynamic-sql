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
import org.mybatis.dynamic.sql.NullCriterion
import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.order.OrderByModel
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.delete.DeleteModel
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.ConfigurableStatement
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.where.WhereApplier
import org.mybatis.dynamic.sql.where.WhereModel
import java.util.ArrayList
import java.util.function.Consumer

/**
 * delete 语句 DSL 的抽象基类。
 */
abstract class AbstractDeleteDSL<M, D : AbstractDeleteDSL<M, D>> protected constructor(
    private val table: SqlTable,
    private val tableAlias: String?
) : WhereOperations<AbstractDeleteDSL<M, D>.DeleteWhereBuilder>,
    ConfigurableStatement<D>,
    OrderByOperations<D>,
    Buildable<M> {

    private var whereBuilder: DeleteWhereBuilder? = null
    private val statementConfiguration: StatementConfiguration = StatementConfiguration()
    private var limit: Long? = null
    private var orderByModel: OrderByModel? = null



    override fun where(): DeleteWhereBuilder {
        if(whereBuilder==null) {
            whereBuilder = DeleteWhereBuilder(NullCriterion())
        }
        return whereBuilder!!
    }

    override fun where(initialCriterion: SqlCriterion): DeleteWhereBuilder {
        Validator.assertNull(whereBuilder, Validator.ERROR_32)
        whereBuilder = DeleteWhereBuilder(initialCriterion)
        return whereBuilder!!
    }

    override fun applyWhere(whereApplier: WhereApplier): DeleteWhereBuilder {
        Validator.assertNull(whereBuilder, Validator.ERROR_32)
        whereBuilder = DeleteWhereBuilder(whereApplier.initialCriterion(), whereApplier.subCriteria())
        return whereBuilder!!
    }

    fun limit(limit: Long): D {
        return limitWhenPresent(limit)
    }

    fun limitWhenPresent(limit: Long?): D {
        this.limit = limit
        return getThis()
    }

    override fun orderBy(columns: List<SortSpecification>): D {
        orderByModel = OrderByModel(columns)
        return getThis()
    }

    protected abstract fun getThis(): D

    /**
     * 警告!调用此方法可能生成删除表中所有行的 delete 语句。
     *
     * @return 模型类
     */
    protected fun buildDeleteModel(): DeleteModel {
        val whereModel = if (whereBuilder == null) null else whereBuilder!!.buildWhereModel()
        return DeleteModel(table,statementConfiguration, tableAlias,whereModel,limit,orderByModel)
    }

    override fun configureStatement(consumer: Consumer<StatementConfiguration>): D {
        consumer.accept(statementConfiguration)
        return getThis()
    }

    inner class DeleteWhereBuilder @JvmOverloads constructor(
        private val initialCriterion: SqlCriterion,
        subCriteria: List<AndOrCriteriaGroup>? = null
    ) : BooleanOperations<DeleteWhereBuilder>,
        ConfigurableStatement<DeleteWhereBuilder>,
        Buildable<M> {

        private val subCriteria: MutableList<AndOrCriteriaGroup> = ArrayList()

        init {
            if (subCriteria != null) {
                this.subCriteria.addAll(subCriteria)
            }
        }

        override fun addSubCriterion(subCriterion: AndOrCriteriaGroup): DeleteWhereBuilder {
            subCriteria.add(subCriterion)
            return this
        }

        fun limit(limit: Long): D {
            return limitWhenPresent(limit)
        }

        fun limitWhenPresent(limit: Long?): D {
            return this@AbstractDeleteDSL.limitWhenPresent(limit)
        }

        fun orderBy(vararg columns: SortSpecification): D {
            return orderBy(listOf(*columns))
        }

        fun orderBy(columns: List<SortSpecification>): D {
            orderByModel = OrderByModel(columns)
            return this@AbstractDeleteDSL.getThis()
        }

        override fun configureStatement(consumer: Consumer<StatementConfiguration>): DeleteWhereBuilder {
            this@AbstractDeleteDSL.configureStatement(consumer)
            return this
        }

        override fun build(): M {
            return this@AbstractDeleteDSL.build()
        }

        fun buildWhereModel(): WhereModel {
            return WhereModel(initialCriterion,subCriteria)
        }
    }
}
