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

import org.mybatis.dynamic.sql.exception.InvalidSqlException
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.select.join.JoinModel
import org.mybatis.dynamic.sql.select.join.JoinSpecification
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.FragmentCollector
import org.mybatis.dynamic.sql.util.Messages
import java.util.Objects
import java.util.stream.Collectors

/**
 * join 渲染器。
 */
class JoinRenderer private constructor(builder: Builder) {
    private val joinModel: JoinModel
    private val tableExpressionRenderer: TableExpressionRenderer
    private val renderingContext: RenderingContext

    init {
        joinModel = Objects.requireNonNull(builder.joinModel!!)
        tableExpressionRenderer = Objects.requireNonNull(builder.tableExpressionRenderer!!)
        renderingContext = Objects.requireNonNull(builder.renderingContext!!)
    }

    fun render(): FragmentAndParameters {
        return joinModel.joinSpecifications()
            .map { joinSpecification: JoinSpecification -> renderJoinSpecification(joinSpecification) }
            .collect(FragmentCollector.collect())
            .toFragmentAndParameters(Collectors.joining(" ")) //$NON-NLS-1$
    }

    private fun renderJoinSpecification(joinSpecification: JoinSpecification): FragmentAndParameters {
        val fc = FragmentCollector()
        fc.add(FragmentAndParameters.fromFragment(joinSpecification.joinType().type()))
        fc.add(joinSpecification.table().accept(tableExpressionRenderer))
        fc.add(
            JoinSpecificationRenderer
                .withJoinSpecification(joinSpecification)
                .withRenderingContext(renderingContext)
                .build()
                .render()
                .orElseThrow { InvalidSqlException(Messages.getString("ERROR.46")) } //$NON-NLS-1$
        )

        return fc.toFragmentAndParameters(Collectors.joining(" ")) //$NON-NLS-1$
    }

    companion object {
        @JvmStatic
        fun withJoinModel(joinModel: JoinModel): Builder {
            return Builder().withJoinModel(joinModel)
        }
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        var joinModel: JoinModel? = null
        var tableExpressionRenderer: TableExpressionRenderer? = null
        var renderingContext: RenderingContext? = null

        fun withJoinModel(joinModel: JoinModel): Builder {
            this.joinModel = joinModel
            return this
        }

        fun withTableExpressionRenderer(tableExpressionRenderer: TableExpressionRenderer): Builder {
            this.tableExpressionRenderer = tableExpressionRenderer
            return this
        }

        fun withRenderingContext(renderingContext: RenderingContext): Builder {
            this.renderingContext = renderingContext
            return this
        }

        fun build(): JoinRenderer {
            return JoinRenderer(this)
        }
    }
}
