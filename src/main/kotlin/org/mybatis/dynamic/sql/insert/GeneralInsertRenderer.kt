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

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.util.toFieldAndValueCollector

/**
 * 通用 insert 渲染器。
 */
class GeneralInsertRenderer(
    private val model: GeneralInsertModel,
    renderingStrategy: RenderingStrategy
) {

    private val visitor: GeneralInsertValuePhraseVisitor

    init {
        val statementConfiguration = model.statementConfiguration()
        val renderingContext = RenderingContext(renderingStrategy,statementConfiguration)
        visitor = GeneralInsertValuePhraseVisitor(renderingContext)
    }

    fun render(): GeneralInsertStatementProvider {
        val collector =  model.columnMappings().mapNotNull {  it.accept(visitor) }.toFieldAndValueCollector()
        Validator.assertFalse(collector.isEmpty(), "ERROR.9")
        val insertStatement = InsertRenderingUtilities.calculateInsertStatement(model.table(), collector)
        return DefaultGeneralInsertStatementProvider(insertStatement,collector.parameters())
    }

}
