package org.mybatis.dynamic.sql.select.group

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.util.Validator

/**
 * group by 子句模型。
 */
class GroupByModel(
    private val columns: List<BasicColumn>
) {

    init {
        Validator.assertNotEmpty(columns, "ERROR.11") //$NON-NLS-1$
    }

    fun columns(): List<BasicColumn> {
        return columns
    }

}