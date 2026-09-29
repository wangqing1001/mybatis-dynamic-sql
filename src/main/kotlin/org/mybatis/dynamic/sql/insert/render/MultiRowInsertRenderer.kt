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

import org.mybatis.dynamic.sql.insert.MultiRowInsertModel
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.util.StringUtilities
import java.util.Objects

/**
 * 多行 insert 渲染器。
 */
class MultiRowInsertRenderer<T> private constructor(builder: Builder<T>) {
    private val model: MultiRowInsertModel<T>
    private val visitor: MultiRowValuePhraseVisitor

    init {
        model = Objects.requireNonNull(builder.model)
        // 前缀是通用格式,稍后将通过 String.format(...) 解析
        visitor = MultiRowValuePhraseVisitor(
            Objects.requireNonNull(builder.renderingStrategy),
            "records[%s]" //$NON-NLS-1$
        )
    }

    fun render(): MultiRowInsertStatementProvider<T> {
        val collector = model.columnMappings()
            .map { m -> m.accept(visitor) }
            .collect(FieldAndValueCollector.collect())

        val insertStatement = calculateInsertStatement(collector)

        return DefaultMultiRowInsertStatementProvider.Builder<T>().withRecords(model.records())
            .withInsertStatement(insertStatement)
            .build()
    }

    private fun calculateInsertStatement(collector: FieldAndValueCollector): String {
        val statementStart = InsertRenderingUtilities.calculateInsertStatementStart(model.table())
        val columnsPhrase = collector.columnsPhrase()
        val valuesPhrase = collector.multiRowInsertValuesPhrase(model.recordCount())

        return statementStart + StringUtilities.spaceBefore(columnsPhrase) + StringUtilities.spaceBefore(valuesPhrase)
    }

    companion object {
        @JvmStatic
        fun <T> withMultiRowInsertModel(model: MultiRowInsertModel<T>): Builder<T> {
            return Builder<T>().withMultiRowInsertModel(model)
        }
    }

    class Builder<T> {
        lateinit var model: MultiRowInsertModel<T>
        lateinit var renderingStrategy: RenderingStrategy

        fun withMultiRowInsertModel(model: MultiRowInsertModel<T>): Builder<T> {
            this.model = model
            return this
        }

        fun withRenderingStrategy(renderingStrategy: RenderingStrategy): Builder<T> {
            this.renderingStrategy = renderingStrategy
            return this
        }

        fun build(): MultiRowInsertRenderer<T> {
            return MultiRowInsertRenderer(this)
        }
    }
}
