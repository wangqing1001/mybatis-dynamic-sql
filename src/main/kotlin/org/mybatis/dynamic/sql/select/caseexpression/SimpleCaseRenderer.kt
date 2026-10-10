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
package org.mybatis.dynamic.sql.select.caseexpression

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.toFragmentCollector


/**
 * 简单 case 表达式渲染器。
 */
class SimpleCaseRenderer<T>(
    private val simpleCaseModel: SimpleCaseModel<T>,
    private val renderingContext: RenderingContext
) {

    private val whenConditionRenderer = SimpleCaseWhenConditionRenderer(renderingContext,simpleCaseModel.column())

    fun render(): FragmentAndParameters {
        val list = mutableListOf(renderCase(),renderWhenConditions())
        val elseClause = renderElse()
        if(elseClause != null) {
            list.add(elseClause)
        }
        list.add(renderEnd())
        return list.toFragmentCollector().toFragmentAndParameters(" ") //$NON-NLS-1$
    }

    private fun renderCase(): FragmentAndParameters {
        val alias = simpleCaseModel.column().alias()
        val fragmentAndParameters = if (alias != null) {
            FragmentAndParameters(alias)
        } else {
            simpleCaseModel.column().render(renderingContext)
        }
        return fragmentAndParameters.mapFragment { f: String -> "case $f" }
    }

    private fun renderWhenConditions(): FragmentAndParameters {
        return simpleCaseModel.whenConditions().map {
            renderWhenCondition(it)
        }.toFragmentCollector().toFragmentAndParameters(" ")
    }

    private fun renderWhenCondition(whenCondition: SimpleCaseWhenCondition<T>): FragmentAndParameters {
        return listOf(
            renderWhen(),
            renderConditions(whenCondition),
            renderThen(whenCondition)
        ).toFragmentCollector()
            .toFragmentAndParameters(" ")
    }

    private fun renderWhen(): FragmentAndParameters {
        return FragmentAndParameters("when")
    }

    private fun renderConditions(whenCondition: SimpleCaseWhenCondition<T>): FragmentAndParameters {
        return whenCondition.accept(whenConditionRenderer)
    }

    private fun renderThen(whenCondition: SimpleCaseWhenCondition<T>): FragmentAndParameters {
        return whenCondition.thenValue().render(renderingContext)
            .mapFragment { f: String -> "then $f" } //$NON-NLS-1$
    }

    private fun renderElse(): FragmentAndParameters? {
        return simpleCaseModel.elseValue()?.render(renderingContext)?.mapFragment {  "else $it" } //$
    }



    private fun renderEnd(): FragmentAndParameters {
        return FragmentAndParameters("end") //$NON-NLS-1$
    }
}
