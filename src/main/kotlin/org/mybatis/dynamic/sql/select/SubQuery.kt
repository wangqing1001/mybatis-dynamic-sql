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

import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.TableExpressionVisitor
import java.util.Objects
import java.util.Optional

/**
 * 子查询表表达式。包装一个 SelectModel 并可选指定别名。
 */
class SubQuery private constructor(builder: Builder) : TableExpression {
    private val selectModel: SelectModel
    private val alias: String?

    init {
        selectModel = Objects.requireNonNull(builder.selectModel!!)
        alias = builder.alias
    }

    fun selectModel(): SelectModel {
        return selectModel
    }

    fun alias(): Optional<String> {
        return Optional.ofNullable(alias)
    }

    override val isSubQuery: Boolean
        get() = true

    override fun <R> accept(visitor: TableExpressionVisitor<R>): R {
        return visitor.visit(this)
    }

    class Builder {
        // 字段公开,以便外部类 SubQuery 访问(Kotlin 嵌套类与 Java 不同,外部类无法访问嵌套类私有成员)
        var selectModel: SelectModel? = null
        var alias: String? = null

        fun withSelectModel(selectModel: SelectModel): Builder {
            this.selectModel = selectModel
            return this
        }

        fun withAlias(alias: String?): Builder {
            this.alias = alias
            return this
        }

        fun build(): SubQuery {
            return SubQuery(this)
        }
    }
}
