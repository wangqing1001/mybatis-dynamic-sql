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
import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.order.OrderByModel
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.paging.LimitModel
import org.mybatis.dynamic.sql.update.UpdateModel
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.ColumnToColumnMapping
import org.mybatis.dynamic.sql.util.ConfigurableStatement
import org.mybatis.dynamic.sql.util.ConstantMapping
import org.mybatis.dynamic.sql.util.NullMapping
import org.mybatis.dynamic.sql.util.SelectMapping
import org.mybatis.dynamic.sql.util.StringConstantMapping
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.util.ValueMapping
import org.mybatis.dynamic.sql.util.ValueOrNullMapping
import org.mybatis.dynamic.sql.util.ValueWhenPresentMapping
import org.mybatis.dynamic.sql.where.WhereApplier
import org.mybatis.dynamic.sql.where.WhereModel
import java.util.ArrayList
import java.util.Objects
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * update 语句 DSL 的抽象基类。
 */
abstract class AbstractUpdateDSL<M, D : AbstractUpdateDSL<M, D>> protected constructor(
    private val table: SqlTable,
    private val tableAlias: String?
) : WhereOperations<AbstractUpdateDSL<M, D>.UpdateWhereBuilder>,OrderByOperations<D>,ConfigurableStatement<D>,Buildable<M> {

    private val columnMappings: MutableList<AbstractColumnMapping> = ArrayList()
    private var whereBuilder: UpdateWhereBuilder? = null
    private val statementConfiguration: StatementConfiguration = StatementConfiguration()
    private var limit: Long? = null
    private var orderByModel: OrderByModel? = null

    fun <T : Any> set(column: SqlColumn<T>): SetClauseFinisher<T> {
        return SetClauseFinisher(column)
    }

    override fun where(): UpdateWhereBuilder {
        whereBuilder = Objects.requireNonNullElseGet(whereBuilder) { UpdateWhereBuilder(NullCriterion()) }
        return whereBuilder!!
    }

    override fun where(initialCriterion: SqlCriterion): UpdateWhereBuilder {
        Validator.assertNull(whereBuilder, Validator.ERROR_32)
        whereBuilder = UpdateWhereBuilder(initialCriterion)
        return whereBuilder!!
    }

    override fun applyWhere(whereApplier: WhereApplier): UpdateWhereBuilder {
        Validator.assertNull(whereBuilder, Validator.ERROR_32)
        whereBuilder = UpdateWhereBuilder(whereApplier.initialCriterion(), whereApplier.subCriteria())
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



    protected fun buildUpdateModel(): UpdateModel {
        val whereModel = whereBuilder?.buildWhereModel()
        val limitModel = limit?.let { LimitModel(it) }
        return UpdateModel(table,columnMappings,statementConfiguration,tableAlias,whereModel,orderByModel,limitModel)
    }

    override fun configureStatement(consumer: Consumer<StatementConfiguration>): D {
        consumer.accept(statementConfiguration)
        return getThis()
    }

    protected abstract fun getThis(): D

    inner class SetClauseFinisher<T : Any>(private val column: SqlColumn<T>) {

        fun equalToNull(): D {
            columnMappings.add(NullMapping.of(column))
            return this@AbstractUpdateDSL.getThis()
        }

        fun equalToConstant(constant: String): D {
            columnMappings.add(ConstantMapping.of(column, constant))
            return this@AbstractUpdateDSL.getThis()
        }

        fun equalToStringConstant(constant: String): D {
            columnMappings.add(StringConstantMapping.of(column, constant))
            return this@AbstractUpdateDSL.getThis()
        }

        fun equalTo(value: T): D {
            return equalTo(Supplier{ value })
        }

        fun equalTo(valueSupplier: Supplier<T>): D {
            columnMappings.add(ValueMapping.of(column, valueSupplier))
            return this@AbstractUpdateDSL.getThis()
        }

        fun equalTo(buildable: Buildable<SelectModel>): D {
            columnMappings.add(SelectMapping.of(column, buildable))
            return this@AbstractUpdateDSL.getThis()
        }

        fun equalTo(rightColumn: BasicColumn): D {
            columnMappings.add(ColumnToColumnMapping.of(column, rightColumn))
            return this@AbstractUpdateDSL.getThis()
        }

        fun equalToOrNull(value: T?): D {
            return equalToOrNull { value }
        }

        fun equalToOrNull(valueSupplier: Supplier<T?>): D {
            columnMappings.add(ValueOrNullMapping.of(column, valueSupplier))
            return this@AbstractUpdateDSL.getThis()
        }

        fun equalToWhenPresent(value: T?): D {
            return equalToWhenPresent { value }
        }

        fun equalToWhenPresent(valueSupplier: Supplier<T?>): D {
            columnMappings.add(ValueWhenPresentMapping(column, valueSupplier))
            return this@AbstractUpdateDSL.getThis()
        }
    }

    inner class UpdateWhereBuilder @JvmOverloads constructor(
        private val initialCriterion: SqlCriterion,
        subCriteria: List<AndOrCriteriaGroup>? = null
    ) : BooleanOperations<UpdateWhereBuilder>,
        ConfigurableStatement<UpdateWhereBuilder>,
        Buildable<M> {

        private val subCriteria: MutableList<AndOrCriteriaGroup> = ArrayList()

        init {
            if (subCriteria != null) {
                this.subCriteria.addAll(subCriteria)
            }
        }

        override fun addSubCriterion(subCriterion: AndOrCriteriaGroup): UpdateWhereBuilder {
            subCriteria.add(subCriterion)
            return this
        }

        fun limit(limit: Long): D {
            return limitWhenPresent(limit)
        }

        fun limitWhenPresent(limit: Long?): D {
            return this@AbstractUpdateDSL.limitWhenPresent(limit)
        }

        fun orderBy(vararg columns: SortSpecification): D {
            return orderBy(listOf(*columns))
        }

        fun orderBy(columns: List<SortSpecification>): D {
            orderByModel = OrderByModel(columns)
            return this@AbstractUpdateDSL.getThis()
        }

        override fun configureStatement(consumer: Consumer<StatementConfiguration>): UpdateWhereBuilder {
            this@AbstractUpdateDSL.configureStatement(consumer)
            return this
        }

        override fun build(): M {
            return this@AbstractUpdateDSL.build()
        }

        fun buildWhereModel(): WhereModel {
            return WhereModel(initialCriterion,subCriteria)
        }
    }
}
