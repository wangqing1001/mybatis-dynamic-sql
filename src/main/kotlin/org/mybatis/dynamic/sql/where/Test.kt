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
package org.mybatis.dynamic.sql.where

import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion
import org.mybatis.dynamic.sql.CriteriaGroup
import org.mybatis.dynamic.sql.ExistsCriterion
import org.mybatis.dynamic.sql.NotCriterion
import org.mybatis.dynamic.sql.NullCriterion
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.SqlCriterionVisitor
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.StringUtilities
import java.util.function.Function

/**
 * 将 [SqlCriterion] 渲染为 [RenderedCriterion]。
 * 渲染过程比较复杂,因为所有条件可能渲染也可能不渲染。例如,"isEqualWhenPresent"
 * 在值为 null 时不渲染。另外,SqlCriterion 可能包含也可能不包含子条件。
 *
 * <p>渲染是一个递归过程。渲染器会递归进入每个子条件 - 子条件也可能包含更深的子条件 -
 * 直到所有可能的子条件都被渲染为单个片段。例如,最终片段可能看起来像:
 *
 * <pre>
 *     col1 = ? and (col2 = ? or (col3 = ? and col4 = ?))
 * </pre>
 *
 * <p>如果所有条件和子条件都无法渲染,最终结果也可能为空。
 */
class Test(private val renderingContext: RenderingContext) : SqlCriterionVisitor<Pair<String?, FragmentAndParameters>?> {

    override fun <T> visit(criterion: ColumnAndConditionCriterion<T>): Pair<String?, FragmentAndParameters>? {
        val initialCriterion = renderColumnAndCondition(criterion)
        val renderedSubCriteria = renderSubCriteria(criterion.subCriteria())
        return initialCriterion ?.let {
            calculateRenderedCriterion(it, renderedSubCriteria, ::calculateFragment)
        } ?: calculateRenderedCriterion(renderedSubCriteria, ::calculateFragment)
    }

    override fun visit(criterion: ExistsCriterion): Pair<String?, FragmentAndParameters> {
        val existsPredicate = criterion.existsPredicate()
        val selectModel = existsPredicate.selectModelBuilder().build()
        val prefix = "${existsPredicate.operator()} ("
        val initialCriterion = selectModel.render(renderingContext,prefix,")")
        val renderedSubCriteria = renderSubCriteria(criterion.subCriteria())
        return calculateRenderedCriterion(initialCriterion, renderedSubCriteria, ::calculateFragment)
    }

    override fun visit(criterion: CriteriaGroup): Pair<String?, FragmentAndParameters>? {
        return render(criterion.initialCriterion(), criterion.subCriteria(), ::calculateFragment)
    }

    override fun visit(criterion: NotCriterion): Pair<String?, FragmentAndParameters>? {
        return render(criterion.initialCriterion(), criterion.subCriteria(), ::calculateNotFragment)
    }

    override fun visit(criterion: NullCriterion): Pair<String, FragmentAndParameters>? {
        return null
    }

    fun render(criterion: SqlCriterion,subCriteria: List<AndOrCriteriaGroup>,function: Function<List<FragmentAndParameters>,FragmentAndParameters>): Pair<String?, FragmentAndParameters>? {
        val initial = criterion.accept(this)?.second
        val subList = renderSubCriteria(subCriteria)
        return initial?.let {
            calculateRenderedCriterion(it, subList, function)
        } ?: calculateRenderedCriterion(subList, function)
    }

    private fun <T> renderColumnAndCondition(criterion: ColumnAndConditionCriterion<T>): FragmentAndParameters? {
        if (criterion.condition().shouldRender(renderingContext)) {
            return renderCondition(criterion)
        }
        criterion.condition().renderingSkipped()
        return null
    }

    private fun renderSubCriteria(subCriteria: List<AndOrCriteriaGroup>): List<Pair<String?, FragmentAndParameters>> {
        return subCriteria.mapNotNull { renderAndOrCriteriaGroup(it) }
    }

    private fun renderAndOrCriteriaGroup(criterion: AndOrCriteriaGroup): Pair<String?, FragmentAndParameters>? {
        val initialCriterion = criterion.initialCriterion()
        val subCriteria = criterion.subCriteria()
        return render(initialCriterion, subCriteria,{ calculateFragment(it) })
    }

    private fun <T > renderCondition(criterion: ColumnAndConditionCriterion<T>): FragmentAndParameters {
        return ColumnAndConditionRenderer(criterion.column(),criterion.condition(),renderingContext).render()
    }

    private fun calculateRenderedCriterion(initial: FragmentAndParameters,subList: List<Pair<String?, FragmentAndParameters>>,function: Function<List<FragmentAndParameters>,FragmentAndParameters>): Pair<String?, FragmentAndParameters> {
        val list = collectSqlFragments(initial, subList)

        return null to function.apply(list)
    }

    private fun calculateRenderedCriterion(
        renderedSubCriteria: List<Pair<String?, FragmentAndParameters>>,
        function: Function<List<FragmentAndParameters>, FragmentAndParameters>
    ): Pair<String?, FragmentAndParameters>? {
        return collectSqlFragments(renderedSubCriteria)?.let {
            null to function.apply(it)
        }
    }

    private fun collectSqlFragments(init: FragmentAndParameters,subList: List<Pair<String?, FragmentAndParameters>>): List<FragmentAndParameters> {
        val list = mutableListOf(init)
        list.addAll(subList.map(::merge))
        return list
    }

    private fun collectSqlFragments(subList: List<Pair<String?, FragmentAndParameters>>): List<FragmentAndParameters>? {
        if (subList.isEmpty()) {
            return null
        }
        val list = mutableListOf(subList[0].second)
        val otherList = subList.slice(1 until subList.size).map(::merge)
        list.addAll(otherList)
        return list
    }

    private fun calculateFragment(list: List<FragmentAndParameters>): FragmentAndParameters {
        if(list.size>1) {
            return list.reduce { acc, parameters -> acc + " " + parameters  }.mapFragment{"($it)"}
        }
        return list.firstOrNull()?:FragmentAndParameters("")
    }

    private fun calculateNotFragment(list: List<FragmentAndParameters>): FragmentAndParameters {
        if(list.size>1) {
            return list.reduce { acc, parameters -> acc + " " + parameters  }.mapFragment{"not ($it)"}
        }
        return list.firstOrNull()?.mapFragment{"not $it"}?:FragmentAndParameters("")
    }

    private fun merge(pair: Pair<String?, FragmentAndParameters>): FragmentAndParameters {
        val connector = pair.first?:return pair.second
        return pair.second.mapFragment{ connector + StringUtilities.spaceBefore(it) }
    }
}
