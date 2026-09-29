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
package org.mybatis.dynamic.sql.select

import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.render.MultiSelectRenderer
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.util.Validator
import java.util.Objects
import java.util.stream.Stream

/**
 * 多 select 语句模型。包含初始 select 与多个 union 查询。
 */
class MultiSelectModel private constructor(builder: Builder) : AbstractSelectModel(builder) {
    private val initialSelect: SelectModel
    private val unionQueries: List<UnionQuery>

    init {
        initialSelect = Objects.requireNonNull(builder.initialSelect!!)
        unionQueries = builder.unionQueries
        Validator.assertNotEmpty(unionQueries, "ERROR.35") //$NON-NLS-1$
    }

    fun initialSelect(): SelectModel {
        return initialSelect
    }

    fun unionQueries(): Stream<UnionQuery> {
        return unionQueries.stream()
    }

    fun render(renderingStrategy: RenderingStrategy): SelectStatementProvider {
        return MultiSelectRenderer.withMultiSelectModel(this)
            .withRenderingStrategy(renderingStrategy)
            .build()
            .render()
    }

    class Builder : AbstractBuilder<Builder>() {
        // 字段公开,以便外部类 MultiSelectModel 访问(Kotlin 嵌套类与 Java 不同,外部类无法访问嵌套类私有成员)
        var initialSelect: SelectModel? = null
        val unionQueries: MutableList<UnionQuery> = ArrayList()

        fun withInitialSelect(initialSelect: SelectModel): Builder {
            this.initialSelect = initialSelect
            return this
        }

        fun withUnionQueries(unionQueries: List<UnionQuery>): Builder {
            this.unionQueries.addAll(unionQueries)
            return this
        }

        override fun getThis(): Builder {
            return this
        }

        fun build(): MultiSelectModel {
            return MultiSelectModel(this)
        }
    }
}
