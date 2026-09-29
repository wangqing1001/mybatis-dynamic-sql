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

import org.mybatis.dynamic.sql.insert.InsertColumnListModel
import org.mybatis.dynamic.sql.insert.InsertSelectModel
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.render.SubQueryRenderer
import org.mybatis.dynamic.sql.util.StringUtilities
import java.util.Objects
import java.util.stream.Collectors

/**
 * insert-select 渲染器。
 */
class InsertSelectRenderer private constructor(builder: Builder) {
    private val model: InsertSelectModel
    private val renderingContext: RenderingContext

    init {
        model = Objects.requireNonNull(builder.model)
        renderingContext = RenderingContext.withRenderingStrategy(Objects.requireNonNull(builder.renderingStrategy))
            .withStatementConfiguration(model.statementConfiguration())
            .build()
    }

    fun render(): InsertSelectStatementProvider {
        val statementStart = InsertRenderingUtilities.calculateInsertStatementStart(model.table())
        val columnsPhrase = calculateColumnsPhrase()
        val prefix = statementStart + StringUtilities.spaceAfter(columnsPhrase)

        val fragmentAndParameters = SubQueryRenderer.withSelectModel(model.selectModel())
            .withRenderingContext(renderingContext)
            .withPrefix(prefix)
            .build()
            .render()

        return DefaultGeneralInsertStatementProvider.withInsertStatement(fragmentAndParameters.fragment())
            .withParameters(fragmentAndParameters.parameters())
            .build()
    }

    private fun calculateColumnsPhrase(): String {
        return model.columnList().map { columnList -> calculateColumnsPhrase(columnList) }.orElse("") //$NON-NLS-1$
    }

    private fun calculateColumnsPhrase(columnList: InsertColumnListModel): String {
        return columnList.columns()
            .map { column -> column.name() }
            .collect(Collectors.joining(", ", " (", ")")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    companion object {
        @JvmStatic
        fun withInsertSelectModel(model: InsertSelectModel): Builder {
            return Builder().withInsertSelectModel(model)
        }
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        lateinit var model: InsertSelectModel
        lateinit var renderingStrategy: RenderingStrategy

        fun withInsertSelectModel(model: InsertSelectModel): Builder {
            this.model = model
            return this
        }

        fun withRenderingStrategy(renderingStrategy: RenderingStrategy): Builder {
            this.renderingStrategy = renderingStrategy
            return this
        }

        fun build(): InsertSelectRenderer {
            return InsertSelectRenderer(this)
        }
    }
}
