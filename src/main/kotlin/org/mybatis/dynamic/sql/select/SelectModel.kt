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
package org.mybatis.dynamic.sql.select

import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.render.SelectRenderer
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.util.Validator
import java.util.Objects
import java.util.Optional
import java.util.function.Function
import java.util.stream.Stream

/**
 * select 语句模型。
 */
class SelectModel private constructor(builder: Builder) : AbstractSelectModel(builder) {
    private val queryExpressions: List<QueryExpressionModel>
    private val forClause: String?
    private val waitClause: String?

    init {
        queryExpressions = Objects.requireNonNull(builder.queryExpressions)
        Validator.assertNotEmpty(queryExpressions, "ERROR.14") //$NON-NLS-1$
        forClause = builder.forClause
        waitClause = builder.waitClause
    }

    fun queryExpressions(): Stream<QueryExpressionModel> {
        return queryExpressions.stream()
    }

    fun forClause(): Optional<String> {
        return Optional.ofNullable(forClause)
    }

    fun waitClause(): Optional<String> {
        return Optional.ofNullable(waitClause)
    }

    fun render(renderingStrategy: RenderingStrategy): SelectStatementProvider {
        return SelectRenderer.withSelectModel(this)
            .withRenderingStrategy(renderingStrategy)
            .build()
            .render()
    }

    fun <R> map(mapper: Function<SelectModel, R>): R {
        return mapper.apply(this)
    }

    companion object {
        @JvmStatic
        fun withQueryExpressions(queryExpressions: List<QueryExpressionModel>): Builder {
            return Builder().withQueryExpressions(queryExpressions)
        }
    }

    class Builder : AbstractBuilder<Builder>() {
        // 字段公开,以便外部类 SelectModel 访问(Kotlin 嵌套类与 Java 不同,外部类无法访问嵌套类私有成员)
        val queryExpressions: MutableList<QueryExpressionModel> = ArrayList()
        var forClause: String? = null
        var waitClause: String? = null

        fun withQueryExpression(queryExpression: QueryExpressionModel): Builder {
            this.queryExpressions.add(queryExpression)
            return this
        }

        fun withQueryExpressions(queryExpressions: List<QueryExpressionModel>): Builder {
            this.queryExpressions.addAll(queryExpressions)
            return this
        }

        fun withForClause(forClause: String?): Builder {
            this.forClause = forClause
            return this
        }

        fun withWaitClause(waitClause: String?): Builder {
            this.waitClause = waitClause
            return this
        }

        override fun getThis(): Builder {
            return this
        }

        fun build(): SelectModel {
            return SelectModel(this)
        }
    }
}
