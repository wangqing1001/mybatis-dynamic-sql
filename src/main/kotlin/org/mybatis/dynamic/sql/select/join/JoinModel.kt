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
package org.mybatis.dynamic.sql.select.join

import org.mybatis.dynamic.sql.exception.InvalidSqlException
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.select.TableExpressionRenderer
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.Messages
import org.mybatis.dynamic.sql.util.assertNotEmpty
import org.mybatis.dynamic.sql.util.toFragmentCollector


class JoinModel(
    private val joinSpecifications: List<JoinSpecification> = emptyList()
) {

    init {
        assertNotEmpty(joinSpecifications, "ERROR.15") //$NON-NLS-1$
    }

    fun joinSpecifications(): List<JoinSpecification> {
        return joinSpecifications
    }

    fun containsSubQueries(): Boolean {
        return joinSpecifications.any { it.table().isSubQuery }
    }

    fun render(tableExpressionRenderer: TableExpressionRenderer,renderingContext: RenderingContext): FragmentAndParameters {
        return joinSpecifications.map {  renderJoinSpecification(it,tableExpressionRenderer,renderingContext) }
            .toFragmentCollector().toFragmentAndParameters(" ")
    }

    private fun renderJoinSpecification(joinSpecification: JoinSpecification,tableExpressionRenderer: TableExpressionRenderer,renderingContext: RenderingContext): FragmentAndParameters {
        val list = mutableListOf<FragmentAndParameters>()
        list.add(FragmentAndParameters(joinSpecification.joinType().type()))
        list.add(joinSpecification.table().accept(tableExpressionRenderer))
        val fragmentAndParameters = joinSpecification.render(renderingContext) ?: throw InvalidSqlException(Messages.getString("ERROR.46"))
        list.add(fragmentAndParameters)
        return list.toFragmentCollector().toFragmentAndParameters(" ")
    }

}
