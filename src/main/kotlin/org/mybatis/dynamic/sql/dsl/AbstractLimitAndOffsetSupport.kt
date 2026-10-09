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

import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.select.paging.PagingModel
import org.mybatis.dynamic.sql.util.Buildable
import java.util.Optional

/**
 * limit 与 offset 支持的抽象基类。
 *
 * @param T DSL 类型
 * @param M 模型类型
 */
abstract class AbstractLimitAndOffsetSupport<T, M> :
    LimitAndOffsetOperations<T, M> where T : ForAndWaitOperations<T>,T : OrderByOperations<T>,T : Buildable<M> {

    private var limit: Long? = null
    private var offset: Long? = null
    private var fetchFirstRows: Long? = null
    private val delegate: T

    protected constructor(delegate: T) {
        this.delegate = delegate
    }

    override fun limitWhenPresent(limit: Long?): LimitAndOffsetOperations.LimitFinisher<T, M> {
        this.limit = limit
        return LimitFinisherImpl()
    }

    override fun offsetWhenPresent(offset: Long?): LimitAndOffsetOperations.OffsetFirstFinisher<T, M> {
        this.offset = offset
        return OffsetFirstFinisherImpl()
    }

    override fun fetchFirstWhenPresent(fetchFirstRows: Long?): LimitAndOffsetOperations.FetchFirstFinisher<T> {
        this.fetchFirstRows = fetchFirstRows
        return FetchFirstFinisherImpl()
    }

    protected abstract fun getThis(): T

    protected fun toPagingModel(): PagingModel? {
        if(limit==null && offset==null && fetchFirstRows==null ){
            return null
        }
        return PagingModel(limit,offset,fetchFirstRows)
    }

    open inner class ExtraMethods : ForAndWaitOperations<T>, OrderByOperations<T>, Buildable<M> {
        override fun setForClause(forClause: String): T {
            return this@AbstractLimitAndOffsetSupport.delegate.setForClause(forClause)
        }

        override fun setWaitClause(waitClause: String): T {
            return this@AbstractLimitAndOffsetSupport.delegate.setWaitClause(waitClause)
        }

        override fun orderBy(columns: List<SortSpecification>): T {
            return this@AbstractLimitAndOffsetSupport.delegate.orderBy(columns)
        }

        override fun build(): M {
            return delegate.build()
        }
    }

    inner class LimitFinisherImpl : ExtraMethods(), LimitAndOffsetOperations.LimitFinisher<T, M> {
        override fun offsetWhenPresent(offset: Long?): T {
            this@AbstractLimitAndOffsetSupport.offset = offset
            return this@AbstractLimitAndOffsetSupport.getThis()
        }
    }

    inner class OffsetFirstFinisherImpl : ExtraMethods(), LimitAndOffsetOperations.OffsetFirstFinisher<T, M> {

        override fun fetchFirstWhenPresent(fetchFirstRows: Long?): LimitAndOffsetOperations.FetchFirstFinisher<T> {
            return this@AbstractLimitAndOffsetSupport.fetchFirstWhenPresent(fetchFirstRows)
        }

    }

    inner class FetchFirstFinisherImpl : LimitAndOffsetOperations.FetchFirstFinisher<T> {
        override fun rowsOnly(): T {
            return this@AbstractLimitAndOffsetSupport.getThis()
        }
    }
}
