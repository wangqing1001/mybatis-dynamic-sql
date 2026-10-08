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
import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.select.render.SimpleCaseRenderer
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.Validator
import java.util.ArrayList
import java.util.Objects
import java.util.Optional
import java.util.stream.Stream

/**
 * 简单 case 表达式模型。
 */
class SimpleCaseModel<T> private constructor(builder: Builder<T>) : BasicColumn, SortSpecification {
    private val column: BindableColumn<T>
    private val whenConditions: List<SimpleCaseWhenCondition<T>>
    private val elseValue: BasicColumn?
    private val alias: String?
    private val descendingPhrase: String

    init {
        column = Objects.requireNonNull(builder.column)
        whenConditions = builder.whenConditions
        elseValue = builder.elseValue
        alias = builder.alias
        descendingPhrase = builder.descendingPhrase
        Validator.assertNotEmpty(whenConditions, "ERROR.40") //$NON-NLS-1$
    }

    fun column(): BindableColumn<T> {
        return column
    }

    fun whenConditions(): Collection<SimpleCaseWhenCondition<T>> {
        return whenConditions
    }

    fun elseValue(): Optional<BasicColumn> {
        return Optional.ofNullable(elseValue)
    }

    override fun alias(): String? {
        return alias
    }

    override fun `as`(alias: String): SimpleCaseModel<T> {
        return Builder<T>()
            .withColumn(column)
            .withWhenConditions(whenConditions)
            .withElseValue(elseValue)
            .withAlias(alias)
            .withDescendingPhrase(descendingPhrase)
            .build()
    }

    override fun descending(): SimpleCaseModel<T> {
        return Builder<T>()
            .withColumn(column)
            .withWhenConditions(whenConditions)
            .withElseValue(elseValue)
            .withAlias(alias)
            .withDescendingPhrase(" DESC") //$NON-NLS-1$
            .build()
    }

    override fun renderForOrderBy(renderingContext: RenderingContext): FragmentAndParameters {
        return render(renderingContext).mapFragment { f: String -> f + descendingPhrase }
    }

    override fun render(renderingContext: RenderingContext): FragmentAndParameters {
        return SimpleCaseRenderer(this, renderingContext).render()
    }

    class Builder<T> {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        lateinit var column: BindableColumn<T>
        val whenConditions: MutableList<SimpleCaseWhenCondition<T>> = ArrayList()
        var elseValue: BasicColumn? = null
        var alias: String? = null
        var descendingPhrase: String = "" //$NON-NLS-1$

        fun withColumn(column: BindableColumn<T>): Builder<T> {
            this.column = column
            return this
        }

        fun withWhenConditions(whenConditions: List<SimpleCaseWhenCondition<T>>): Builder<T> {
            this.whenConditions.addAll(whenConditions)
            return this
        }

        fun withElseValue(elseValue: BasicColumn?): Builder<T> {
            this.elseValue = elseValue
            return this
        }

        fun withAlias(alias: String?): Builder<T> {
            this.alias = alias
            return this
        }

        fun withDescendingPhrase(descendingPhrase: String): Builder<T> {
            this.descendingPhrase = descendingPhrase
            return this
        }

        fun build(): SimpleCaseModel<T> {
            return SimpleCaseModel(this)
        }
    }
}
