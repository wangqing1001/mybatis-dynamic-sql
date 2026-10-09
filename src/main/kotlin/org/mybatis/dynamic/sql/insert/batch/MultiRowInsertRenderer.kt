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
package org.mybatis.dynamic.sql.insert.batch

import org.mybatis.dynamic.sql.insert.InsertRenderingUtilities
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.util.FieldAndValueCollector
import org.mybatis.dynamic.sql.util.StringUtilities
import org.mybatis.dynamic.sql.util.toFieldAndValueCollector

/**
 * 多行 insert 渲染器。
 */
class MultiRowInsertRenderer<T>(
    private val model: MultiRowInsertModel<T>,
    renderingStrategy: RenderingStrategy
) {
    private val visitor: MultiRowValuePhraseVisitor = MultiRowValuePhraseVisitor(renderingStrategy, "records[%s]")

    fun render(): MultiRowInsertStatementProvider<T> {
        val collector = model.columnMappings().map { m -> m.accept(visitor) }.toFieldAndValueCollector()
        val insertStatement = calculateInsertStatement(collector)
        return DefaultMultiRowInsertStatementProvider(insertStatement, model.records())
    }

    private fun calculateInsertStatement(collector: FieldAndValueCollector): String {
        val statementStart = InsertRenderingUtilities.calculateInsertStatementStart(model.table())
        val columnsPhrase = collector.columnsPhrase()
        val valuesPhrase = collector.multiRowInsertValuesPhrase(model.recordCount())
        return statementStart + StringUtilities.spaceBefore(columnsPhrase) + StringUtilities.spaceBefore(valuesPhrase)
    }


}
