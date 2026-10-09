package org.mybatis.dynamic.sql.insert.batch

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.insert.InsertRenderingUtilities
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.util.ConstantMapping
import org.mybatis.dynamic.sql.util.FieldAndValueAndParameters
import org.mybatis.dynamic.sql.util.MappedColumnMapping
import org.mybatis.dynamic.sql.util.MultiRowInsertMappingVisitor
import org.mybatis.dynamic.sql.util.NullMapping
import org.mybatis.dynamic.sql.util.PropertyMapping
import org.mybatis.dynamic.sql.util.RowMapping
import org.mybatis.dynamic.sql.util.StringConstantMapping
import org.mybatis.dynamic.sql.util.StringUtilities

/**
 * 多行 insert 值短语访问器。
 */
open class MultiRowValuePhraseVisitor internal constructor(
    protected val renderingStrategy: RenderingStrategy,
    protected val prefix: String
) : MultiRowInsertMappingVisitor<FieldAndValueAndParameters>() {

    override fun visit(mapping: NullMapping): FieldAndValueAndParameters {
        return FieldAndValueAndParameters(mapping.columnName(), "null")
    }

    override fun visit(mapping: ConstantMapping): FieldAndValueAndParameters {
        return FieldAndValueAndParameters(mapping.columnName(), mapping.constant())
    }

    override fun visit(mapping: StringConstantMapping): FieldAndValueAndParameters {
        val valuePhrase = StringUtilities.formatConstantForSQL(mapping.constant())
        return FieldAndValueAndParameters(mapping.columnName(), valuePhrase)
    }

    override fun visit(mapping: PropertyMapping): FieldAndValueAndParameters {
        val valuePhrase = calculateJdbcPlaceholder(mapping.column(), mapping.property())
        return FieldAndValueAndParameters(mapping.columnName(), valuePhrase)
    }

    override fun visit(mapping: RowMapping): FieldAndValueAndParameters {
        val valuePhrase = calculateJdbcPlaceholder(mapping.column())
        return FieldAndValueAndParameters(mapping.columnName(), valuePhrase)
    }

    override fun visit(mapping: MappedColumnMapping): FieldAndValueAndParameters {
        val valuePhrase = calculateJdbcPlaceholder(
            mapping.column(),
            InsertRenderingUtilities.getMappedPropertyName(mapping.column())
        )
        return FieldAndValueAndParameters(mapping.columnName(), valuePhrase)
    }

    private fun calculateJdbcPlaceholder(column: SqlColumn<*>): String {
        var renderingStrategy = column.renderingStrategy()
        if (renderingStrategy == null) {
            renderingStrategy = this.renderingStrategy
        }
        return renderingStrategy.getRecordBasedInsertBinding(column, prefix)
    }

    private fun calculateJdbcPlaceholder(column: SqlColumn<*>, parameterName: String): String {
        var renderingStrategy = column.renderingStrategy()
        if (renderingStrategy == null) {
            renderingStrategy = this.renderingStrategy
        }
        return renderingStrategy.getRecordBasedInsertBinding(column, prefix, parameterName)
    }
}