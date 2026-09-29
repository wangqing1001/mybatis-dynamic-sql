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
package org.mybatis.dynamic.sql


class ColumnAndConditionCriterion<T: Any> private constructor(builder: Builder<T>) : SqlCriterion(builder) {

    private val column: BindableColumn<T>
    private val condition: RenderableCondition<T>

    init {
        column = builder.column
        condition = builder.condition
    }

    fun column(): BindableColumn<T> {
        return column
    }

    fun condition(): RenderableCondition<T> {
        return condition
    }

    override fun <R> accept(visitor: SqlCriterionVisitor<R>): R {
        return visitor.visit(this)
    }

    class Builder<T: Any> : AbstractBuilder<Builder<T>>() {
        lateinit var column: BindableColumn<T>
        lateinit var condition: RenderableCondition<T>

        fun withColumn(column: BindableColumn<T>): Builder<T> {
            this.column = column
            return this
        }

        fun withCondition(condition: RenderableCondition<T>): Builder<T> {
            this.condition = condition
            return this
        }

        override fun self(): Builder<T> {
            return this
        }

        fun build(): ColumnAndConditionCriterion<T> {
            return ColumnAndConditionCriterion<T>(this)
        }
    }

    companion object {

        @JvmStatic
        fun <T: Any> withColumn(column: BindableColumn<T>): Builder<T> {
            return Builder<T>().withColumn(column)
        }

    }
}
