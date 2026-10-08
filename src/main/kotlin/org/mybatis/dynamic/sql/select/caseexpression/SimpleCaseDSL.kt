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
import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.RenderableCondition
import java.util.Arrays
import java.util.Objects

/**
 * 简单 case 表达式 DSL。
 */
class SimpleCaseDSL<T> private constructor(column: BindableColumn<T>) : ElseDSL<SimpleCaseDSL<T>.SimpleCaseEnder> {
    private val column: BindableColumn<T>
    private val whenConditions: MutableList<SimpleCaseWhenCondition<T>> = mutableListOf()
    private var elseValue: BasicColumn? = null

    init {
        this.column = Objects.requireNonNull(column)
    }

    @SafeVarargs
    fun `when`(condition: RenderableCondition<T>, vararg subsequentConditions: RenderableCondition<T>): ConditionBasedWhenFinisher {
        return `when`(condition, listOf(*subsequentConditions))
    }

    fun `when`(
        condition: RenderableCondition<T>,
        subsequentConditions: List<RenderableCondition<T>>
    ): ConditionBasedWhenFinisher {
        return ConditionBasedWhenFinisher(condition, subsequentConditions)
    }

    @SafeVarargs
    fun `when`(condition: T, vararg subsequentConditions: T): BasicWhenFinisher {
        return `when`(condition, listOf(*subsequentConditions))
    }

    fun `when`(condition: T, subsequentConditions: List<T>): BasicWhenFinisher {
        return BasicWhenFinisher(condition, subsequentConditions)
    }

    @Suppress("FunctionName")
    override fun else_(column: BasicColumn): SimpleCaseEnder {
        elseValue = column
        return SimpleCaseEnder()
    }

    fun end(): SimpleCaseModel<T> {
        return SimpleCaseModel.Builder<T>()
            .withColumn(column)
            .withWhenConditions(whenConditions)
            .withElseValue(elseValue)
            .build()
    }

    inner class ConditionBasedWhenFinisher(
        condition: RenderableCondition<T>,
        subsequentConditions: List<RenderableCondition<T>>
    ) : ThenDSL<SimpleCaseDSL<T>> {
        private val conditions: MutableList<RenderableCondition<T>> = ArrayList()

        init {
            conditions.add(condition)
            conditions.addAll(subsequentConditions)
        }

        override fun then(column: BasicColumn): SimpleCaseDSL<T> {
            whenConditions.add(ConditionBasedWhenCondition(column,conditions))
            return this@SimpleCaseDSL
        }
    }

    inner class BasicWhenFinisher(
        value: T,
        subsequentValues: List<T>
    ) : ThenDSL<SimpleCaseDSL<T>> {
        private val values: MutableList<T> = ArrayList()

        init {
            values.add(value)
            values.addAll(subsequentValues)
        }

        override fun then(column: BasicColumn): SimpleCaseDSL<T> {
            whenConditions.add(BasicWhenCondition(column,values))
            return this@SimpleCaseDSL
        }
    }

    inner class SimpleCaseEnder {
        fun end(): SimpleCaseModel<T> {
            return this@SimpleCaseDSL.end()
        }
    }

    companion object {
        @JvmStatic
        fun <T> simpleCase(column: BindableColumn<T>): SimpleCaseDSL<T> {
            return SimpleCaseDSL(column)
        }
    }
}
