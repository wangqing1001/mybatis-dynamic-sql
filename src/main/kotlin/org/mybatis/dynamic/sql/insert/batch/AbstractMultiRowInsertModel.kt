package org.mybatis.dynamic.sql.insert.batch

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.util.AbstractColumnMapping

/**
 * 多行 insert 模型的抽象基类,持有表、记录与列映射。
 */
abstract class AbstractMultiRowInsertModel<T> protected constructor(
    private val table: SqlTable,
    private val records: List<T>,
    private val columnMappings: List<AbstractColumnMapping> = emptyList()
) {

    fun columnMappings(): List<AbstractColumnMapping> {
        return columnMappings
    }

    fun records(): List<T> {
        return records
    }

    fun table(): SqlTable {
        return table
    }

    fun recordCount(): Int {
        return records.size
    }

}