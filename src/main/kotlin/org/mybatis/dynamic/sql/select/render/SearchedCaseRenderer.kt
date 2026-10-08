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

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.exception.InvalidSqlException
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.select.caseexpression.SearchedCaseModel
import org.mybatis.dynamic.sql.select.caseexpression.SearchedCaseWhenCondition
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.Messages
import org.mybatis.dynamic.sql.util.toFragmentCollector
import java.util.Objects
import java.util.Optional


/**
 * 搜索式 case 表达式渲染器。
 */
class SearchedCaseRenderer(
    searchedCaseModel: SearchedCaseModel,
    renderingContext: RenderingContext
) {
    private val searchedCaseModel: SearchedCaseModel
    private val renderingContext: RenderingContext

    init {
        this.searchedCaseModel = Objects.requireNonNull(searchedCaseModel)
        this.renderingContext = Objects.requireNonNull(renderingContext)
    }

    fun render(): FragmentAndParameters {
        val list = mutableListOf(renderCase(),renderWhenConditions())
        renderElse().ifPresent { list.add(it) }
        list.add(renderEnd())
        return list.toFragmentCollector().toFragmentAndParameters(" ") //$NON-NLS-1$
    }

    private fun renderCase(): FragmentAndParameters {
        return FragmentAndParameters("case") //$NON-NLS-1$
    }

    private fun renderWhenConditions(): FragmentAndParameters {
        return searchedCaseModel.whenConditions().map {
            renderWhenCondition(it)
        }.toFragmentCollector().toFragmentAndParameters(" ")
    }

    private fun renderWhenCondition(whenCondition: SearchedCaseWhenCondition): FragmentAndParameters {
        return listOf(renderWhen(whenCondition), renderThen(whenCondition))
            .toFragmentCollector().toFragmentAndParameters(" ")
    }

    private fun renderWhen(whenCondition: SearchedCaseWhenCondition): FragmentAndParameters {
        return  SearchedCaseWhenConditionRenderer(whenCondition,renderingContext).render()
            ?: throw InvalidSqlException(Messages.getString("ERROR.39"))
    }

    private fun renderThen(whenCondition: SearchedCaseWhenCondition): FragmentAndParameters {
        return whenCondition.thenValue().render(renderingContext).mapFragment { f: String -> "then $f" }
    }

    private fun renderElse(): Optional<FragmentAndParameters> {
        return searchedCaseModel.elseValue().map { elseValue: BasicColumn -> renderElse(elseValue) }
    }

    private fun renderElse(elseValue: BasicColumn): FragmentAndParameters {
        return elseValue.render(renderingContext).mapFragment { f: String -> "else $f" } //$NON-NLS-1$
    }

    private fun renderEnd(): FragmentAndParameters {
        return FragmentAndParameters("end") //$NON-NLS-1$
    }
}
