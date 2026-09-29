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

import org.mybatis.dynamic.sql.common.OrderByModel
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import java.util.Objects
import java.util.Optional

/**
 * select 模型基类。封装 order by、分页和语句配置。
 */
abstract class AbstractSelectModel protected constructor(builder: AbstractBuilder<*>) {
    private val orderByModel: OrderByModel?
    private val pagingModel: PagingModel?
    protected val statementConfiguration: StatementConfiguration

    init {
        orderByModel = builder.orderByModel
        pagingModel = builder.pagingModel
        statementConfiguration = Objects.requireNonNull(builder.statementConfiguration!!)
    }

    fun orderByModel(): Optional<OrderByModel> {
        return Optional.ofNullable(orderByModel)
    }

    fun pagingModel(): Optional<PagingModel> {
        return Optional.ofNullable(pagingModel)
    }

    fun statementConfiguration(): StatementConfiguration {
        return statementConfiguration
    }

    abstract class AbstractBuilder<T : AbstractBuilder<T>> {
        // 字段公开,以便外部抽象类访问(Kotlin 嵌套类与 Java 不同,外部类无法访问嵌套类私有成员)
        var orderByModel: OrderByModel? = null
        var pagingModel: PagingModel? = null
        var statementConfiguration: StatementConfiguration? = null

        fun withOrderByModel(orderByModel: OrderByModel?): T {
            this.orderByModel = orderByModel
            return getThis()
        }

        fun withPagingModel(pagingModel: PagingModel?): T {
            this.pagingModel = pagingModel
            return getThis()
        }

        fun withStatementConfiguration(statementConfiguration: StatementConfiguration): T {
            this.statementConfiguration = statementConfiguration
            return getThis()
        }

        protected abstract fun getThis(): T
    }
}
