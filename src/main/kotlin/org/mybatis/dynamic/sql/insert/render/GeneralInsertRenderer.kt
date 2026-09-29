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
package org.mybatis.dynamic.sql.insert.render

import org.mybatis.dynamic.sql.insert.GeneralInsertModel
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.util.Validator
import java.util.Objects
import java.util.Optional

/**
 * 通用 insert 渲染器。
 */
class GeneralInsertRenderer private constructor(builder: Builder) {
    private val model: GeneralInsertModel = builder.model
    private val visitor: GeneralInsertValuePhraseVisitor

    init {
        val renderingContext = RenderingContext
            .withRenderingStrategy(builder.renderingStrategy)
            .withStatementConfiguration(model.statementConfiguration())
            .build()
        visitor = GeneralInsertValuePhraseVisitor(renderingContext)
    }

    fun render(): GeneralInsertStatementProvider {
        val collector = model.columnMappings()
            .map { m -> m.accept(visitor) }
            .flatMap { optional: Optional<FieldAndValueAndParameters> -> optional.stream() }
            .collect(FieldAndValueCollector.collect())

        Validator.assertFalse(collector.isEmpty(), "ERROR.9") //$NON-NLS-1$

        val insertStatement = InsertRenderingUtilities.calculateInsertStatement(model.table(), collector)

        return DefaultGeneralInsertStatementProvider.withInsertStatement(insertStatement)
            .withParameters(collector.parameters())
            .build()
    }

    companion object {
        @JvmStatic
        fun withInsertModel(model: GeneralInsertModel): Builder {
            return Builder().withInsertModel(model)
        }
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        lateinit var model: GeneralInsertModel
        lateinit var renderingStrategy: RenderingStrategy

        fun withInsertModel(model: GeneralInsertModel): Builder {
            this.model = model
            return this
        }

        fun withRenderingStrategy(renderingStrategy: RenderingStrategy): Builder {
            this.renderingStrategy = renderingStrategy
            return this
        }

        fun build(): GeneralInsertRenderer {
            return GeneralInsertRenderer(this)
        }
    }
}
