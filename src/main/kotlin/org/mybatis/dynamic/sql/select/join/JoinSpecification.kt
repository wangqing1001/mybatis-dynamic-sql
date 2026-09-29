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
package org.mybatis.dynamic.sql.select.join

import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionModel
import java.util.Objects

/**
 * 连接规格,描述一次 join 的表、连接类型与连接条件。
 */
class JoinSpecification private constructor(builder: Builder) : AbstractBooleanExpressionModel(builder) {
    private val table: TableExpression
    private val joinType: JoinType

    init {
        table = Objects.requireNonNull(builder.table!!)
        joinType = Objects.requireNonNull(builder.joinType!!)
    }

    fun table(): TableExpression {
        return table
    }

    fun joinType(): JoinType {
        return joinType
    }

    companion object {
        @JvmStatic
        fun withJoinTable(table: TableExpression): Builder {
            return Builder().withJoinTable(table)
        }
    }

    class Builder : AbstractBuilder<Builder>() {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        var table: TableExpression? = null
        var joinType: JoinType? = null

        fun withJoinTable(table: TableExpression): Builder {
            this.table = table
            return this
        }

        fun withJoinType(joinType: JoinType): Builder {
            this.joinType = joinType
            return this
        }

        fun build(): JoinSpecification {
            return JoinSpecification(this)
        }

        override fun self(): Builder {
            return this
        }
    }
}
