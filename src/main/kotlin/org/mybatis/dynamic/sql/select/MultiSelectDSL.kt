/*
 *    Copyright 2016-2025 the original author or authors.
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

import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.common.OrderByModel
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.ConfigurableStatement
import java.util.Arrays
import java.util.Optional
import java.util.function.Consumer

/**
 * 多 select 语句 DSL。支持初始 select 与多个 union 查询。
 */
class MultiSelectDSL(builder: Buildable<SelectModel>) : Buildable<MultiSelectModel>, ConfigurableStatement<MultiSelectDSL> {
    private val unionQueries: MutableList<UnionQuery> = ArrayList()
    private val initialSelect: SelectModel
    private var orderByModel: OrderByModel? = null
    private var limit: Long? = null
    private var offset: Long? = null
    private var fetchFirstRows: Long? = null
    private val statementConfiguration: StatementConfiguration = StatementConfiguration()

    init {
        initialSelect = builder.build()
    }

    fun union(builder: Buildable<SelectModel>): MultiSelectDSL {
        unionQueries.add(UnionQuery("union", builder.build())) //$NON-NLS-1$
        return this
    }

    fun unionAll(builder: Buildable<SelectModel>): MultiSelectDSL {
        unionQueries.add(UnionQuery("union all", builder.build())) //$NON-NLS-1$
        return this
    }

    fun orderBy(vararg columns: SortSpecification): MultiSelectDSL {
        return orderBy(Arrays.asList(*columns))
    }

    fun orderBy(columns: Collection<SortSpecification>): MultiSelectDSL {
        orderByModel = OrderByModel.of(columns)
        return this
    }

    fun limit(limit: Long): LimitFinisher {
        return limitWhenPresent(limit)
    }

    fun limitWhenPresent(limit: Long?): LimitFinisher {
        this.limit = limit
        return LimitFinisher()
    }

    fun offset(offset: Long): OffsetFirstFinisher {
        return offsetWhenPresent(offset)
    }

    fun offsetWhenPresent(offset: Long?): OffsetFirstFinisher {
        this.offset = offset
        return OffsetFirstFinisher()
    }

    fun fetchFirst(fetchFirstRows: Long): FetchFirstFinisher {
        return fetchFirstWhenPresent(fetchFirstRows)
    }

    fun fetchFirstWhenPresent(fetchFirstRows: Long?): FetchFirstFinisher {
        this.fetchFirstRows = fetchFirstRows
        return FetchFirstFinisher()
    }

    override fun build(): MultiSelectModel {
        return MultiSelectModel.Builder()
            .withInitialSelect(initialSelect)
            .withUnionQueries(unionQueries)
            .withOrderByModel(orderByModel)
            .withPagingModel(buildPagingModel().orElse(null))
            .withStatementConfiguration(statementConfiguration)
            .build()
    }

    private fun buildPagingModel(): Optional<PagingModel> {
        return PagingModel.Builder()
            .withLimit(limit)
            .withOffset(offset)
            .withFetchFirstRows(fetchFirstRows)
            .build()
    }

    override fun configureStatement(consumer: Consumer<StatementConfiguration>): MultiSelectDSL {
        consumer.accept(statementConfiguration)
        return this
    }

    inner class OffsetFirstFinisher : Buildable<MultiSelectModel> {
        fun fetchFirst(fetchFirstRows: Long): FetchFirstFinisher {
            return fetchFirstWhenPresent(fetchFirstRows)
        }

        fun fetchFirstWhenPresent(fetchFirstRows: Long?): FetchFirstFinisher {
            this@MultiSelectDSL.fetchFirstRows = fetchFirstRows
            return FetchFirstFinisher()
        }

        override fun build(): MultiSelectModel {
            return this@MultiSelectDSL.build()
        }
    }

    inner class LimitFinisher : Buildable<MultiSelectModel> {
        fun offset(offset: Long): MultiSelectDSL {
            return offsetWhenPresent(offset)
        }

        fun offsetWhenPresent(offset: Long?): MultiSelectDSL {
            this@MultiSelectDSL.offset = offset
            return this@MultiSelectDSL
        }

        override fun build(): MultiSelectModel {
            return this@MultiSelectDSL.build()
        }
    }

    inner class FetchFirstFinisher {
        fun rowsOnly(): MultiSelectDSL {
            return this@MultiSelectDSL
        }
    }
}
