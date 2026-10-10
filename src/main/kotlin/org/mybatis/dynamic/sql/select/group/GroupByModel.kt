package org.mybatis.dynamic.sql.select.group

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.util.toFragmentCollector

/**
 * group by 子句模型。
 */
class GroupByModel(private val columns: List<BasicColumn>) {

    init {
        Validator.assertNotEmpty(columns, "ERROR.11") //$NON-NLS-1$
    }

    fun columns(): List<BasicColumn> {
        return columns
    }

    fun render(renderingContext: RenderingContext): FragmentAndParameters {
        return columns.map { it.render(renderingContext) }.toFragmentCollector()
            .toFragmentAndParameters(", ", "group by ", "")
    }

}