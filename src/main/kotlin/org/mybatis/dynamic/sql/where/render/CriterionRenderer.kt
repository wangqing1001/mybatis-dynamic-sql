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
package org.mybatis.dynamic.sql.where.render

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
import org.mybatis.dynamic.sql.util.FragmentCollector
import org.mybatis.dynamic.sql.util.toFragmentCollector
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
class CriterionRenderer(private val renderingContext: RenderingContext) : SqlCriterionVisitor<RenderedCriterion?> {

    override fun <T> visit(criterion: ColumnAndConditionCriterion<T>): RenderedCriterion? {
        val initialCriterion = renderColumnAndCondition(criterion)
        val renderedSubCriteria = renderSubCriteria(criterion.subCriteria())
        return initialCriterion ?.let {
            calculateRenderedCriterion(it, renderedSubCriteria, ::calculateFragment)
        } ?: calculateRenderedCriterion(renderedSubCriteria, ::calculateFragment)
    }

    override fun visit(criterion: ExistsCriterion): RenderedCriterion {
        val initialCriterion = renderExists(criterion)
        val renderedSubCriteria = renderSubCriteria(criterion.subCriteria())
        return calculateRenderedCriterion(initialCriterion, renderedSubCriteria, ::calculateFragment)
    }

    override fun visit(criterion: CriteriaGroup): RenderedCriterion? {
        return renderCriteriaGroup(criterion, ::calculateFragment)
    }

    override fun visit(criterion: NotCriterion): RenderedCriterion? {
        return renderCriteriaGroup(criterion, ::calculateNotFragment)
    }

    override fun visit(criterion: NullCriterion): RenderedCriterion? {
        return null
    }

    private fun renderCriteriaGroup(
        criterion: CriteriaGroup,
        fragmentCalculator: Function<FragmentCollector, String>
    ): RenderedCriterion? {
        return render(criterion.initialCriterion(), criterion.subCriteria(), fragmentCalculator)
    }

    fun render(initialCriterion: SqlCriterion,subCriteria: List<AndOrCriteriaGroup>,calculator: Function<FragmentCollector, String>): RenderedCriterion? {
        val fragmentAndParameters = initialCriterion.accept(this)?.fragmentAndParameters()
        val renderedSubCriteria = renderSubCriteria(subCriteria)
        return fragmentAndParameters?. let {
            calculateRenderedCriterion(it, renderedSubCriteria, calculator)
        } ?: calculateRenderedCriterion(renderedSubCriteria, calculator)
    }

    private fun <T > renderColumnAndCondition(criterion: ColumnAndConditionCriterion<T>): FragmentAndParameters? {
        if (criterion.condition().shouldRender(renderingContext)) {
            return renderCondition(criterion)
        }
        criterion.condition().renderingSkipped()
        return null
    }

    private fun renderExists(criterion: ExistsCriterion): FragmentAndParameters {
        val existsPredicate = criterion.existsPredicate()
        val selectModel = existsPredicate.selectModelBuilder().build()
        val prefix = "${existsPredicate.operator()} ("
        return selectModel.render(renderingContext,prefix,")")
    }

    private fun renderSubCriteria(subCriteria: List<AndOrCriteriaGroup>): List<RenderedCriterion> {
        return subCriteria.mapNotNull { renderAndOrCriteriaGroup(it) }
    }

    private fun renderAndOrCriteriaGroup(criterion: AndOrCriteriaGroup): RenderedCriterion? {
        return render(criterion.initialCriterion(), criterion.subCriteria(), ::calculateFragment)?.withConnector(criterion.connector())
    }

    private fun calculateRenderedCriterion(
        initialCriterion: FragmentAndParameters,
        renderedSubCriteria: List<RenderedCriterion>,
        calculator: Function<FragmentCollector, String>
    ): RenderedCriterion {
        val fragmentCollector = collectSqlFragments(initialCriterion, renderedSubCriteria)
        return calculateRenderedCriterion(fragmentCollector,calculator )
    }

    private fun calculateRenderedCriterion(fragmentCollector: FragmentCollector,calculator: Function<FragmentCollector, String>): RenderedCriterion {
        val fragment = calculator.apply(fragmentCollector)
        val fragmentAndParameters = FragmentAndParameters(fragment,fragmentCollector.parameters())
        return RenderedCriterion(null,fragmentAndParameters)
    }

    private fun calculateRenderedCriterion(
        renderedSubCriteria: List<RenderedCriterion>,
        calculator: Function<FragmentCollector, String>
    ): RenderedCriterion? {

        return collectSqlFragments(renderedSubCriteria)?.let { calculateRenderedCriterion(it,calculator) }
    }

    private fun <T > renderCondition(criterion: ColumnAndConditionCriterion<T>): FragmentAndParameters {
        return ColumnAndConditionRenderer(criterion.column(),criterion.condition(),renderingContext).render()
    }

    /**
     * 此方法封装了从初始条件和渲染后的子条件列表构建片段集合的逻辑。
     * 此重载已知存在初始条件,并且可能还有子条件。收集器将按顺序包含初始条件和任何渲染后的子条件。
     *
     * @param initialCondition 不可为 null。如果没有初始条件,请使用另一个重载
     * @param renderedSubCriteria 之前渲染的子条件列表。子条件都将带有连接符(AND 或 OR)
     * @return 片段收集器,其片段表示最终计算出的片段和参数列表。
     *     片段收集器可用于计算单个组合片段 - 无论是作为 where 子句,还是在递归调用中作为有效的渲染子条件
     */
    private fun collectSqlFragments(initialCondition: FragmentAndParameters,renderedSubCriteria: List<RenderedCriterion>): FragmentCollector {
        return renderedSubCriteria.map { it.fragmentAndParametersWithConnector() }
            .toFragmentCollector(initialCondition)
    }

    /**
     * 此方法封装了从渲染后的子条件列表构建片段集合的逻辑。
     * 在此重载中,我们将子条件列表的第一个元素作为初始条件。
     * 收集器将按顺序包含渲染后的子条件。但是,第一个渲染子条件的连接符将被移除,
     * 以避免生成无效的 where 子句,如 "where and a < 3"。
     *
     * @param renderedSubCriteria 之前渲染的子条件列表。子条件都将带有连接符(AND 或 OR)
     * @return 片段收集器,其片段表示最终计算出的片段和参数列表。
     *     片段收集器可用于计算单个组合片段 - 无论是作为 where 子句,还是在递归调用中作为有效的渲染子条件
     */
    private fun collectSqlFragments(renderedSubCriteria: List<RenderedCriterion>): FragmentCollector? {
        if (renderedSubCriteria.isEmpty()) {
            return null
        }
        val firstCondition = renderedSubCriteria[0].fragmentAndParameters()
        return renderedSubCriteria.slice(1 until renderedSubCriteria.size)
            .map { it.fragmentAndParametersWithConnector() }.toFragmentCollector(firstCondition)
    }

    private fun calculateFragment(collector: FragmentCollector): String {
        return if (collector.hasMultipleFragments()) {
            collector.collectFragments(" ", "(", ")" )
        } else {
            collector.firstFragment() ?: ""
        }
    }

    private fun calculateNotFragment(collector: FragmentCollector): String {
        return if (collector.hasMultipleFragments()) {
            collector.collectFragments(" ", "not (", ")")
        } else {
            collector.firstFragment()?.let { "not $it" } ?: ""
        }
    }
}
