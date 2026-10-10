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

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.select.SearchedCaseRenderer
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import java.util.Optional

/**
 * 搜索型 case 表达式模型。
 */
class SearchedCaseModel @JvmOverloads constructor(
    private val whenConditions: List<SearchedCaseWhenCondition>,
    private val elseValue: BasicColumn? = null,
    private val alias: String? = null,
    private val descendingPhrase: String = ""
) : BasicColumn, SortSpecification {

    fun whenConditions(): Collection<SearchedCaseWhenCondition> {
        return whenConditions
    }

    fun elseValue(): Optional<BasicColumn> {
        return Optional.ofNullable(elseValue)
    }

    override fun alias(): String? {
        return alias
    }

    override fun `as`(alias: String): SearchedCaseModel {
        return SearchedCaseModel(whenConditions, elseValue,alias,descendingPhrase)
    }

    override fun descending(): SearchedCaseModel {
        return SearchedCaseModel(whenConditions, elseValue,alias," DESC")
    }

    override fun renderForOrderBy(renderingContext: RenderingContext): FragmentAndParameters {
        return render(renderingContext).mapFragment { f: String -> f + descendingPhrase }
    }

    override fun render(renderingContext: RenderingContext): FragmentAndParameters {
        return SearchedCaseRenderer(this, renderingContext).render()
    }

}
