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

import org.mybatis.dynamic.sql.insert.InsertModel
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.util.Validator
import java.util.Objects
import java.util.Optional

/**
 * 单行 insert 渲染器。
 */
class InsertRenderer<T> private constructor(builder: Builder<T>) {
    private val model: InsertModel<T>
    private val visitor: ValuePhraseVisitor

    init {
        model = builder.model
        visitor = ValuePhraseVisitor(builder.renderingStrategy)
    }

    fun render(): InsertStatementProvider<T> {
        val collector = model.columnMappings()
            .map { m -> m.accept(visitor) }
            .flatMap { optional: Optional<FieldAndValueAndParameters> -> optional.stream() }
            .collect(FieldAndValueCollector.collect())

        Validator.assertFalse(collector.isEmpty(), "ERROR.10") //$NON-NLS-1$

        val insertStatement = InsertRenderingUtilities.calculateInsertStatement(model.table(), collector)

        return DefaultInsertStatementProvider.withRow(model.row())
            .withInsertStatement(insertStatement)
            .build()
    }

    companion object {
        @JvmStatic
        fun <T> withInsertModel(model: InsertModel<T>): Builder<T> {
            return Builder<T>().withInsertModel(model)
        }
    }

    class Builder<T> {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        lateinit var model: InsertModel<T>
        lateinit var renderingStrategy: RenderingStrategy

        fun withInsertModel(model: InsertModel<T>): Builder<T> {
            this.model = model
            return this
        }

        fun withRenderingStrategy(renderingStrategy: RenderingStrategy): Builder<T> {
            this.renderingStrategy = renderingStrategy
            return this
        }

        fun build(): InsertRenderer<T> {
            return InsertRenderer(this)
        }
    }
}
