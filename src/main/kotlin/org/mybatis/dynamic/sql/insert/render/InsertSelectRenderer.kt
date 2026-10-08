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

import org.mybatis.dynamic.sql.insert.InsertSelectModel
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.render.SubQueryRenderer
import org.mybatis.dynamic.sql.util.StringUtilities


/**
 * insert-select 渲染器。
 */
class InsertSelectRenderer(
    private val model: InsertSelectModel,
    renderingStrategy: RenderingStrategy
) {
    private val renderingContext: RenderingContext = RenderingContext(renderingStrategy,model.statementConfiguration())

    fun render(): InsertSelectStatementProvider {
        val statementStart = InsertRenderingUtilities.calculateInsertStatementStart(model.table())
        val columnsPhrase = calculateColumnsPhrase()
        val prefix = statementStart + StringUtilities.spaceAfter(columnsPhrase)
        val fragmentAndParameters = SubQueryRenderer(model.selectModel(),renderingContext,prefix).render()
        return DefaultGeneralInsertStatementProvider(fragmentAndParameters.fragment(),fragmentAndParameters.parameters())
    }

    private fun calculateColumnsPhrase(): String {
        val columnList = model.columnList() ?: return ""
        return columnList.columns().joinToString(", ", " (", ")") { column -> column.name() }
    }

}
