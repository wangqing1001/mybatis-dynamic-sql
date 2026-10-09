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
package org.mybatis.dynamic.sql.select

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.TableExpressionVisitor
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters

/**
 * 表表达式渲染器,实现 TableExpressionVisitor 访问者模式。
 */
class TableExpressionRenderer(
    private val renderingContext: RenderingContext
) : TableExpressionVisitor<FragmentAndParameters> {

    override fun visit(table: SqlTable): FragmentAndParameters {
        return FragmentAndParameters(renderingContext.aliasedTableName(table))
    }

    override fun visit(subQuery: SubQuery): FragmentAndParameters {
        var suffix = ")"
        val alias = subQuery.alias()
        if(alias != null) {
            suffix = "$suffix $alias"
        }
        return SubQueryRenderer(subQuery.selectModel(),renderingContext,"(",suffix).render()
    }

}
