package org.mybatis.dynamic.sql.insert.select

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.util.Validator

/**
 * insert 语句的列清单模型。
 */
class InsertColumnListModel(private val columns: List<SqlColumn<*>>) {

    init {
        Validator.assertNotEmpty(columns, "ERROR.4") //$NON-NLS-1$
    }

    fun columns(): List<SqlColumn<*>> {
        return columns
    }

}