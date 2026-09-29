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
import org.mybatis.dynamic.sql.ExistsPredicate
import org.mybatis.dynamic.sql.NotCriterion
import org.mybatis.dynamic.sql.NullCriterion
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.SqlCriterionVisitor
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.select.render.SubQueryRenderer
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.FragmentCollector
import java.util.Objects
import java.util.Optional
import java.util.function.Function
import java.util.stream.Collectors

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
class CriterionRenderer(private val renderingContext: RenderingContext) : SqlCriterionVisitor<Optional<RenderedCriterion>> {


    override fun <T> visit(criterion: ColumnAndConditionCriterion<T>): Optional<RenderedCriterion> {
        val initialCriterion = renderColumnAndCondition(criterion)
        val renderedSubCriteria = renderSubCriteria(criterion.subCriteria())

        return initialCriterion.map { fp: FragmentAndParameters ->
            calculateRenderedCriterion(fp, renderedSubCriteria, ::calculateFragment)
        }.orElseGet { calculateRenderedCriterion(renderedSubCriteria, ::calculateFragment) }
    }

    override fun visit(criterion: ExistsCriterion): Optional<RenderedCriterion> {
        val initialCriterion = renderExists(criterion)
        val renderedSubCriteria = renderSubCriteria(criterion.subCriteria())

        return calculateRenderedCriterion(initialCriterion, renderedSubCriteria, ::calculateFragment)
    }

    override fun visit(criterion: CriteriaGroup): Optional<RenderedCriterion> {
        return renderCriteriaGroup(criterion, ::calculateFragment)
    }

    override fun visit(criterion: NotCriterion): Optional<RenderedCriterion> {
        return renderCriteriaGroup(criterion, ::calculateNotFragment)
    }

    override fun visit(criterion: NullCriterion): Optional<RenderedCriterion> {
        return Optional.empty()
    }

    private fun renderCriteriaGroup(
        criterion: CriteriaGroup,
        fragmentCalculator: Function<FragmentCollector, String>
    ): Optional<RenderedCriterion> {
        return render(criterion.initialCriterion(), criterion.subCriteria(), fragmentCalculator)
    }

    fun render(
        initialCriterion: SqlCriterion,
        subCriteria: List<AndOrCriteriaGroup?>,
        fragmentCalculator: Function<FragmentCollector, String>
    ): Optional<RenderedCriterion> {
        val fragmentAndParameters = initialCriterion.accept(this)
            .map { it.fragmentAndParameters() }
        val renderedSubCriteria = renderSubCriteria(subCriteria)

        return fragmentAndParameters.map { fp: FragmentAndParameters ->
            calculateRenderedCriterion(fp, renderedSubCriteria, fragmentCalculator)
        }.orElseGet { calculateRenderedCriterion(renderedSubCriteria, fragmentCalculator) }
    }

    private fun <T > renderColumnAndCondition(criterion: ColumnAndConditionCriterion<T>): Optional<FragmentAndParameters> {
        return if (criterion.condition().shouldRender(renderingContext)) {
            Optional.of(renderCondition(criterion))
        } else {
            criterion.condition().renderingSkipped()
            Optional.empty()
        }
    }

    private fun renderExists(criterion: ExistsCriterion): FragmentAndParameters {
        val existsPredicate = criterion.existsPredicate()
        return SubQueryRenderer.withSelectModel(existsPredicate.selectModelBuilder().build())
            .withRenderingContext(renderingContext)
            .withPrefix(existsPredicate.operator() + " (") //$NON-NLS-1$
            .withSuffix(")") //$NON-NLS-1$
            .build()
            .render()
    }

    private fun renderSubCriteria(subCriteria: List<AndOrCriteriaGroup?>): List<RenderedCriterion> {
        return subCriteria.filterNotNull().stream()
            .map { renderAndOrCriteriaGroup(it) }
            .flatMap { it.stream() }
            .toList()
    }

    private fun renderAndOrCriteriaGroup(criterion: AndOrCriteriaGroup): Optional<RenderedCriterion> {
        return render(criterion.initialCriterion(), criterion.subCriteria(), ::calculateFragment)
            .map { rc: RenderedCriterion -> rc.withConnector(criterion.connector()) }
    }

    private fun calculateRenderedCriterion(
        initialCriterion: FragmentAndParameters,
        renderedSubCriteria: List<RenderedCriterion>,
        fragmentCalculator: Function<FragmentCollector, String>
    ): Optional<RenderedCriterion> {
        return Optional.of(
            calculateRenderedCriterion(
                collectSqlFragments(initialCriterion, renderedSubCriteria),
                fragmentCalculator
            )
        )
    }

    private fun calculateRenderedCriterion(
        fragmentCollector: FragmentCollector,
        fragmentCalculator: Function<FragmentCollector, String>
    ): RenderedCriterion {
        val fragmentAndParameters = FragmentAndParameters
            .withFragment(fragmentCalculator.apply(fragmentCollector))
            .withParameters(fragmentCollector.parameters())
            .build()

        return RenderedCriterion.Builder()
            .withFragmentAndParameters(fragmentAndParameters)
            .build()
    }

    private fun calculateRenderedCriterion(
        renderedSubCriteria: List<RenderedCriterion>,
        fragmentCalculator: Function<FragmentCollector, String>
    ): Optional<RenderedCriterion> {
        return collectSqlFragments(renderedSubCriteria)
            .map { fc: FragmentCollector -> calculateRenderedCriterion(fc, fragmentCalculator) }
    }

    private fun <T > renderCondition(criterion: ColumnAndConditionCriterion<T>): FragmentAndParameters {
        return ColumnAndConditionRenderer.Builder<T>()
            .withColumn(criterion.column())
            .withCondition(criterion.condition())
            .withRenderingContext(renderingContext)
            .build()
            .render()
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
    private fun collectSqlFragments(
        initialCondition: FragmentAndParameters,
        renderedSubCriteria: List<RenderedCriterion>
    ): FragmentCollector {
        return renderedSubCriteria.stream()
            .map { it.fragmentAndParametersWithConnector() }
            .collect(FragmentCollector.collect(initialCondition))
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
    private fun collectSqlFragments(renderedSubCriteria: List<RenderedCriterion>): Optional<FragmentCollector> {
        if (renderedSubCriteria.isEmpty()) {
            return Optional.empty()
        }

        val firstCondition = renderedSubCriteria[0].fragmentAndParameters()

        val fc = renderedSubCriteria.stream()
            .skip(1)
            .map { it.fragmentAndParametersWithConnector() }
            .collect(FragmentCollector.collect(firstCondition))

        return Optional.of(fc)
    }

    private fun calculateFragment(collector: FragmentCollector): String {
        return if (collector.hasMultipleFragments()) {
            collector.collectFragments(
                Collectors.joining(" ", "(", ")") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            )
        } else {
            collector.firstFragment().orElse("") //$NON-NLS-1$
        }
    }

    private fun calculateNotFragment(collector: FragmentCollector): String {
        return if (collector.hasMultipleFragments()) {
            collector.collectFragments(
                Collectors.joining(" ", "not (", ")") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            )
        } else {
            collector.firstFragment().map { s: String -> "not " + s }.orElse("") //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
