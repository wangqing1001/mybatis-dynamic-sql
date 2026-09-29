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

import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.RenderableCondition
import org.mybatis.dynamic.sql.render.RenderedParameterInfo
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.select.caseexpression.BasicWhenCondition
import org.mybatis.dynamic.sql.select.caseexpression.ConditionBasedWhenCondition
import org.mybatis.dynamic.sql.select.caseexpression.SimpleCaseWhenConditionVisitor
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.FragmentCollector
import org.mybatis.dynamic.sql.util.Validator
import java.util.Objects
import java.util.stream.Collectors

/**
 * 简单 case when 条件渲染器。
 */
class SimpleCaseWhenConditionRenderer<T>(
    renderingContext: RenderingContext,
    column: BindableColumn<T>
) : SimpleCaseWhenConditionVisitor<T, FragmentAndParameters> {
    private val renderingContext: RenderingContext
    private val column: BindableColumn<T>

    init {
        this.renderingContext = Objects.requireNonNull(renderingContext)
        this.column = Objects.requireNonNull(column)
    }

    override fun visit(whenCondition: ConditionBasedWhenCondition<T>): FragmentAndParameters {
        val fragmentCollector = whenCondition.conditions()
            .filter { condition: RenderableCondition<T> -> shouldRender(condition) }
            .map { condition: RenderableCondition<T> -> renderCondition(condition) }
            .collect(FragmentCollector.collect())

        Validator.assertFalse(fragmentCollector.isEmpty(), "ERROR.39") //$NON-NLS-1$

        return fragmentCollector.toFragmentAndParameters(Collectors.joining(", ")) //$NON-NLS-1$
    }

    override fun visit(whenCondition: BasicWhenCondition<T>): FragmentAndParameters {
        return whenCondition.conditions().map { value: T -> renderBasicValue(value) }
            .collect(FragmentCollector.collect())
            .toFragmentAndParameters(Collectors.joining(", ")) //$NON-NLS-1$
    }

    private fun shouldRender(condition: RenderableCondition<T>): Boolean {
        return condition.shouldRender(renderingContext)
    }

    private fun renderCondition(condition: RenderableCondition<T>): FragmentAndParameters {
        return condition.renderCondition(renderingContext, column)
    }

    private fun renderBasicValue(value: T): FragmentAndParameters {
        val rpi = renderingContext.calculateParameterInfo(column)
        return FragmentAndParameters.withFragment(rpi.renderedPlaceHolder)
            .withParameter(rpi.parameterMapKey, value)
            .build()
    }
}
