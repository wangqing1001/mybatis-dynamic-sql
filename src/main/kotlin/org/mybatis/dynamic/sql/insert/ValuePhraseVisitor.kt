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

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.util.ConstantMapping
import org.mybatis.dynamic.sql.util.FieldAndValueAndParameters
import org.mybatis.dynamic.sql.util.InsertMappingVisitor
import org.mybatis.dynamic.sql.util.MappedColumnMapping
import org.mybatis.dynamic.sql.util.MappedColumnWhenPresentMapping
import org.mybatis.dynamic.sql.util.NullMapping
import org.mybatis.dynamic.sql.util.PropertyMapping
import org.mybatis.dynamic.sql.util.PropertyWhenPresentMapping
import org.mybatis.dynamic.sql.util.RowMapping
import org.mybatis.dynamic.sql.util.StringConstantMapping
import org.mybatis.dynamic.sql.util.StringUtilities

/**
 * 值短语访问器,负责将列映射转换为插入语句中的值短语。
 */
open class ValuePhraseVisitor(
    protected val renderingStrategy: RenderingStrategy
) : InsertMappingVisitor<FieldAndValueAndParameters?>() {

    override fun visit(mapping: NullMapping): FieldAndValueAndParameters? {
        return FieldAndValueAndParameters(mapping.columnName(), "null")
    }

    override fun visit(mapping: ConstantMapping): FieldAndValueAndParameters? {
        return FieldAndValueAndParameters(mapping.columnName(), mapping.constant())
    }

    override fun visit(mapping: StringConstantMapping): FieldAndValueAndParameters? {
        val valuePhrase = StringUtilities.formatConstantForSQL(mapping.constant())
        return FieldAndValueAndParameters(mapping.columnName(), valuePhrase)
    }

    override fun visit(mapping: PropertyMapping): FieldAndValueAndParameters? {
        val valuePhrase = calculateJdbcPlaceholder(mapping.column(), mapping.property())
        return FieldAndValueAndParameters(mapping.columnName(), valuePhrase)
    }

    override fun visit(mapping: PropertyWhenPresentMapping): FieldAndValueAndParameters? {
        return if (mapping.shouldRender()) {
            visit(mapping as PropertyMapping)
        } else {
            null
        }
    }

    override fun visit(mapping: RowMapping): FieldAndValueAndParameters? {
        val valuePhrase = calculateJdbcPlaceholder(mapping.column())
        return FieldAndValueAndParameters(mapping.columnName(), valuePhrase)
    }

    override fun visit(mapping: MappedColumnMapping): FieldAndValueAndParameters? {
        val valuePhrase = calculateJdbcPlaceholder(
            mapping.column(),
            InsertRenderingUtilities.getMappedPropertyName(mapping.column())
        )
        return FieldAndValueAndParameters(mapping.columnName(), valuePhrase)
    }

    override fun visit(mapping: MappedColumnWhenPresentMapping): FieldAndValueAndParameters? {
        return if (mapping.shouldRender()) {
            visit(mapping as MappedColumnMapping)
        } else {
            null
        }
    }

    private fun calculateJdbcPlaceholder(column: SqlColumn<*>): String {
        var renderingStrategy = column.renderingStrategy()
        if (renderingStrategy == null) {
            renderingStrategy = this.renderingStrategy
        }
        return renderingStrategy.getRecordBasedInsertBinding(column, "row") //$NON-NLS-1$
    }

    private fun calculateJdbcPlaceholder(column: SqlColumn<*>, parameterName: String): String {
        var renderingStrategy = column.renderingStrategy()
        if (renderingStrategy == null) {
            renderingStrategy = this.renderingStrategy
        }
        return renderingStrategy.getRecordBasedInsertBinding(column, "row", parameterName) //$NON-NLS-1$
    }
}
