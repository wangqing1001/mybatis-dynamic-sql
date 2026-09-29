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
package org.mybatis.dynamic.sql.select.caseexpression

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionModel
import java.util.Objects

/**
 * 搜索型 case when 条件。
 */
class SearchedCaseWhenCondition private constructor(builder: Builder) : AbstractBooleanExpressionModel(builder) {
    private val thenValue: BasicColumn

    init {
        thenValue = Objects.requireNonNull(builder.thenValue)
    }

    fun thenValue(): BasicColumn {
        return thenValue
    }

    class Builder : AbstractBuilder<Builder>() {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        lateinit var thenValue: BasicColumn

        fun withThenValue(thenValue: BasicColumn): Builder {
            this.thenValue = thenValue
            return this
        }

        fun build(): SearchedCaseWhenCondition {
            return SearchedCaseWhenCondition(this)
        }

        override fun self(): Builder {
            return this
        }
    }
}
