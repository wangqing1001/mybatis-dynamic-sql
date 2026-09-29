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
package org.mybatis.dynamic.sql.insert

import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertRenderer
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.render.RenderingStrategy

/**
 * 多行 insert 模型。
 */
class MultiRowInsertModel<T> private constructor(builder: Builder<T>) : AbstractMultiRowInsertModel<T>(builder) {

    init {
        Validator.assertNotEmpty(records(), "ERROR.20") //$NON-NLS-1$
        Validator.assertNotEmpty(columnMappings, "ERROR.8") //$NON-NLS-1$
    }

    fun render(renderingStrategy: RenderingStrategy): MultiRowInsertStatementProvider<T> {
        return MultiRowInsertRenderer.withMultiRowInsertModel(this)
            .withRenderingStrategy(renderingStrategy)
            .build()
            .render()
    }

    companion object {
        @JvmStatic
        fun <T> withRecords(records: Collection<T>): Builder<T> {
            return Builder<T>().withRecords(records)
        }
    }

    class Builder<T> : AbstractBuilder<T, Builder<T>>() {
        override fun getThis(): Builder<T> {
            return this
        }

        fun build(): MultiRowInsertModel<T> {
            return MultiRowInsertModel(this)
        }
    }
}
