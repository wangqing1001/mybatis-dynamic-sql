package org.mybatis.dynamic.sql.insert.batch

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.insert.InsertRenderingUtilities
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.FieldAndValueCollector
import org.mybatis.dynamic.sql.util.StringUtilities
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.util.toFieldAndValueCollector

/**
 * 多行 insert 模型。
 */
class MultiRowInsertModel<T> @JvmOverloads constructor(
    table: SqlTable,
    records: List<T> = emptyList(),
    columnMappings: List<AbstractColumnMapping> = emptyList()
) : AbstractMultiRowInsertModel<T>(table,records,columnMappings) {

    init {
        Validator.assertNotEmpty(records(), "ERROR.20") //$NON-NLS-1$
        Validator.assertNotEmpty(columnMappings(), "ERROR.8") //$NON-NLS-1$
    }

    fun render(renderingStrategy: RenderingStrategy): MultiRowInsertStatementProvider<T> {
        val visitor = MultiRowValuePhraseVisitor(renderingStrategy, "records[%s]")
        val collector = columnMappings().map { m -> m.accept(visitor) }.toFieldAndValueCollector()
        val insertStatement = calculateInsertStatement(collector)
        return DefaultMultiRowInsertStatementProvider(insertStatement, records())
    }

    private fun calculateInsertStatement(collector: FieldAndValueCollector): String {
        val statementStart = InsertRenderingUtilities.calculateInsertStatementStart(table())
        val columnsPhrase = collector.columnsPhrase()
        val valuesPhrase = collector.multiRowInsertValuesPhrase(recordCount())
        return statementStart + StringUtilities.spaceBefore(columnsPhrase) + StringUtilities.spaceBefore(valuesPhrase)
    }

}