/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.common

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.FragmentCollector
import org.mybatis.dynamic.sql.util.StringUtilities
import org.mybatis.dynamic.sql.where.render.CriterionRenderer
import java.util.Optional
import java.util.stream.Collectors

/**
 * 布尔表达式渲染器抽象基类,用于 where / having 子句的渲染。
 */
abstract class AbstractBooleanExpressionRenderer protected constructor(private val prefix: String, builder: AbstractBuilder<*>) {

    protected val model: AbstractBooleanExpressionModel = builder.model
    private val criterionRenderer: CriterionRenderer
    protected val renderingContext: RenderingContext

    init {
        renderingContext = builder.renderingContext
        criterionRenderer = CriterionRenderer(renderingContext)
    }

    open fun render(): Optional<FragmentAndParameters> {
        return criterionRenderer.render(model.initialCriterion(), model.subCriteria(), ::calculateClause)
            .map { it.fragmentAndParameters() }
    }

    private fun calculateClause(collector: FragmentCollector): String {
        return if (collector.hasMultipleFragments()) {
            collector.collectFragments(
                Collectors.joining(" ", StringUtilities.spaceAfter(prefix), "")
            ) //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            collector.firstFragment()
                .map { stripEnclosingParenthesesIfPresent(it) }
                .map { addPrefix(it) }
                .orElse("")
        }
    }

    private fun stripEnclosingParenthesesIfPresent(fragment: String): String {
        // 当渲染出多个条件时,片段会有前后括号。因为只有一个片段,所以最终渲染的
        // 子句中不需要这些括号
        return if (fragment.startsWith("(") && fragment.endsWith(")")) { //$NON-NLS-1$ //$NON-NLS-2$
            fragment.substring(1, fragment.length - 1)
        } else {
            fragment
        }
    }

    private fun addPrefix(fragment: String): String {
        return StringUtilities.spaceAfter(prefix) + fragment
    }

    abstract class AbstractBuilder<B : AbstractBuilder<B>> protected constructor(
        val model: AbstractBooleanExpressionModel
    ) {

        lateinit var renderingContext: RenderingContext

        fun withRenderingContext(renderingContext: RenderingContext): B {
            this.renderingContext = renderingContext
            return self()
        }

        protected abstract fun self(): B
    }
}
