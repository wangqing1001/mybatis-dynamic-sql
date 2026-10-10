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
package org.mybatis.dynamic.sql

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.FragmentCollector
import org.mybatis.dynamic.sql.util.StringUtilities
import org.mybatis.dynamic.sql.where.render.CriterionRenderer

abstract class AbstractBooleanExpressionModel protected constructor(
    private val prefix: String,
    private val initialCriterion: SqlCriterion,
    private val subCriteria: List<AndOrCriteriaGroup> = mutableListOf(),
) {

    fun initialCriterion(): SqlCriterion {
        return initialCriterion
    }

    fun subCriteria(): List<AndOrCriteriaGroup> {
        return subCriteria
    }

    open fun render(renderingContext: RenderingContext): FragmentAndParameters? {
        val criterionRenderer = CriterionRenderer(renderingContext)
        return criterionRenderer.render(initialCriterion,subCriteria,::calculateClause)?.fragmentAndParameters()
    }


    private fun calculateClause(collector: FragmentCollector): String {
        if (collector.hasMultipleFragments()) {
            return collector.collectFragments(" ", StringUtilities.spaceAfter(prefix))
        }
        return collector.firstFragment()?.let { stripEnclosingParenthesesIfPresent(it) }
            ?.let { StringUtilities.spaceAfter(prefix) + it } ?: ""
    }

    private fun stripEnclosingParenthesesIfPresent(fragment: String): String {
        return if (fragment.startsWith("(") && fragment.endsWith(")")) { //$NON-NLS-1$ //$NON-NLS-2$
            fragment.substring(1, fragment.length - 1)
        } else {
            fragment
        }
    }

}
