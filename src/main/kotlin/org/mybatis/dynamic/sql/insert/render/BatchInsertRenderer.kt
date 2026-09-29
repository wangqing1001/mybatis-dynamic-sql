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

import org.mybatis.dynamic.sql.insert.BatchInsertModel
import org.mybatis.dynamic.sql.render.RenderingStrategy
import java.util.Objects

/**
 * 批量 insert 渲染器。
 */
class BatchInsertRenderer<T> private constructor(builder: Builder<T>) {
    private val model: BatchInsertModel<T> = builder.model
    private val visitor: MultiRowValuePhraseVisitor = MultiRowValuePhraseVisitor( builder.renderingStrategy,"row" )

    fun render(): BatchInsert<T> {
        val collector = model.columnMappings()
            .map { m -> m.accept(visitor) }
            .collect(FieldAndValueCollector.collect())

        val insertStatement = InsertRenderingUtilities.calculateInsertStatement(model.table(), collector)

        return BatchInsert.withRecords(model.records())
            .withInsertStatement(insertStatement)
            .build()
    }

    companion object {
        @JvmStatic
        fun <T> withBatchInsertModel(model: BatchInsertModel<T>): Builder<T> {
            return Builder<T>().withBatchInsertModel(model)
        }
    }

    class Builder<T> {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        lateinit var model: BatchInsertModel<T>
        lateinit var renderingStrategy: RenderingStrategy

        fun withBatchInsertModel(model: BatchInsertModel<T>): Builder<T> {
            this.model = model
            return this
        }

        fun withRenderingStrategy(renderingStrategy: RenderingStrategy): Builder<T> {
            this.renderingStrategy = renderingStrategy
            return this
        }

        fun build(): BatchInsertRenderer<T> {
            return BatchInsertRenderer(this)
        }
    }
}
