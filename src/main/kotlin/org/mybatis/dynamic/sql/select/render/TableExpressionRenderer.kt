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
package org.mybatis.dynamic.sql.select.render

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.TableExpressionVisitor
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.select.SubQuery
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import java.util.Objects

/**
 * 表表达式渲染器,实现 TableExpressionVisitor 访问者模式。
 */
class TableExpressionRenderer private constructor(builder: Builder) : TableExpressionVisitor<FragmentAndParameters> {
    private val renderingContext: RenderingContext

    init {
        renderingContext = Objects.requireNonNull(builder.renderingContext!!)
    }

    override fun visit(table: SqlTable): FragmentAndParameters {
        return FragmentAndParameters.fromFragment(renderingContext.aliasedTableName(table))
    }

    override fun visit(subQuery: SubQuery): FragmentAndParameters {
        val suffix = subQuery.alias().map { a: String -> ") $a" } //$NON-NLS-1$
            .orElse(")") //$NON-NLS-1$

        return SubQueryRenderer.withSelectModel(subQuery.selectModel())
            .withRenderingContext(renderingContext)
            .withPrefix("(") //$NON-NLS-1$
            .withSuffix(suffix)
            .build()
            .render()
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        var renderingContext: RenderingContext? = null

        fun withRenderingContext(renderingContext: RenderingContext): Builder {
            this.renderingContext = renderingContext
            return this
        }

        fun build(): TableExpressionRenderer {
            return TableExpressionRenderer(this)
        }
    }
}
