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

import java.util.Optional

/**
 * 分页模型。包含 limit、offset、fetchFirstRows 三个可选值。
 */
class PagingModel private constructor(builder: Builder) {
    private val limit: Long?
    private val offset: Long?
    private val fetchFirstRows: Long?

    init {
        limit = builder.limit
        offset = builder.offset
        fetchFirstRows = builder.fetchFirstRows
    }

    fun limit(): Optional<Long> {
        return Optional.ofNullable(limit)
    }

    fun offset(): Optional<Long> {
        return Optional.ofNullable(offset)
    }

    fun fetchFirstRows(): Optional<Long> {
        return Optional.ofNullable(fetchFirstRows)
    }

    class Builder {
        // 字段公开,以便外部类 PagingModel 访问(Kotlin 嵌套类与 Java 不同,外部类无法访问嵌套类私有成员)
        var limit: Long? = null
        var offset: Long? = null
        var fetchFirstRows: Long? = null

        fun withLimit(limit: Long?): Builder {
            this.limit = limit
            return this
        }

        fun withOffset(offset: Long?): Builder {
            this.offset = offset
            return this
        }

        fun withFetchFirstRows(fetchFirstRows: Long?): Builder {
            this.fetchFirstRows = fetchFirstRows
            return this
        }

        fun build(): Optional<PagingModel> {
            return if (limit == null && offset == null && fetchFirstRows == null) {
                Optional.empty()
            } else {
                Optional.of(PagingModel(this))
            }
        }
    }
}
