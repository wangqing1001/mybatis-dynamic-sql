package org.mybatis.dynamic.sql.insert.batch

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.Validator

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
        return MultiRowInsertRenderer(this, renderingStrategy).render()
    }

}