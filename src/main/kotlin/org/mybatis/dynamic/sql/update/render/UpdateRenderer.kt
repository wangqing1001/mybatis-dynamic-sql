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
package org.mybatis.dynamic.sql.update.render

import org.mybatis.dynamic.sql.common.OrderByModel
import org.mybatis.dynamic.sql.common.OrderByRenderer
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator
import org.mybatis.dynamic.sql.render.RenderedParameterInfo
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.render.TableAliasCalculator
import org.mybatis.dynamic.sql.update.UpdateModel
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.FragmentCollector
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.where.WhereModel
import java.util.Objects
import java.util.Optional
import java.util.stream.Collectors

/**
 * update 渲染器。
 */
class UpdateRenderer private constructor(builder: Builder) {
    private val updateModel: UpdateModel
    private val renderingContext: RenderingContext
    private val visitor: SetPhraseVisitor

    init {
        updateModel = Objects.requireNonNull(builder.updateModel!!)
        val tableAliasCalculator = builder.updateModel!!.tableAlias()
            .map { a: String -> ExplicitTableAliasCalculator.of(updateModel.table(), a) }
            .orElseGet { TableAliasCalculator.empty() }
        renderingContext = RenderingContext
            .withRenderingStrategy(Objects.requireNonNull(builder.renderingStrategy!!))
            .withTableAliasCalculator(tableAliasCalculator)
            .withStatementConfiguration(updateModel.statementConfiguration())
            .build()
        visitor = SetPhraseVisitor(renderingContext)
    }

    fun render(): UpdateStatementProvider {
        val fragmentCollector = FragmentCollector()

        fragmentCollector.add(calculateUpdateStatementStart())
        fragmentCollector.add(calculateSetPhrase())
        calculateWhereClause().ifPresent { fragment: FragmentAndParameters -> fragmentCollector.add(fragment) }
        calculateOrderByClause().ifPresent { fragment: FragmentAndParameters -> fragmentCollector.add(fragment) }
        calculateLimitClause().ifPresent { fragment: FragmentAndParameters -> fragmentCollector.add(fragment) }

        return toUpdateStatementProvider(fragmentCollector)
    }

    private fun toUpdateStatementProvider(fragmentCollector: FragmentCollector): UpdateStatementProvider {
        return DefaultUpdateStatementProvider
            .withUpdateStatement(fragmentCollector.collectFragments(Collectors.joining(" "))) //$NON-NLS-1$
            .withParameters(fragmentCollector.parameters())
            .build()
    }

    private fun calculateUpdateStatementStart(): FragmentAndParameters {
        val aliasedTableName = renderingContext.aliasedTableName(updateModel.table())
        return FragmentAndParameters.fromFragment("update " + aliasedTableName) //$NON-NLS-1$
    }

    private fun calculateSetPhrase(): FragmentAndParameters {
        val fragmentCollector = updateModel.columnMappings()
            .map { m -> m.accept(visitor) }
            .flatMap { optional: Optional<FragmentAndParameters> -> optional.stream() }
            .collect(FragmentCollector.collect())

        Validator.assertFalse(fragmentCollector.isEmpty(), "ERROR.18") //$NON-NLS-1$

        return fragmentCollector.toFragmentAndParameters(
            Collectors.joining(", ", "set ", "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        )
    }

    private fun calculateWhereClause(): Optional<FragmentAndParameters> {
        return updateModel.whereModel().flatMap { whereModel: WhereModel -> renderWhereClause(whereModel) }
    }

    private fun renderWhereClause(whereModel: WhereModel): Optional<FragmentAndParameters> {
        return whereModel.render(renderingContext)
    }

    private fun calculateLimitClause(): Optional<FragmentAndParameters> {
        return updateModel.limit().map { limit: Long -> renderLimitClause(limit) }
    }

    private fun renderLimitClause(limit: Long): FragmentAndParameters {
        val parameterInfo = renderingContext.calculateLimitParameterInfo()

        return FragmentAndParameters.withFragment("limit " + parameterInfo.renderedPlaceHolder) //$NON-NLS-1$
            .withParameter(parameterInfo.parameterMapKey, limit)
            .build()
    }

    private fun calculateOrderByClause(): Optional<FragmentAndParameters> {
        return updateModel.orderByModel().map { orderByModel: OrderByModel -> renderOrderByClause(orderByModel) }
    }

    private fun renderOrderByClause(orderByModel: OrderByModel): FragmentAndParameters {
        return OrderByRenderer(renderingContext).render(orderByModel)
    }

    companion object {
        @JvmStatic
        fun withUpdateModel(updateModel: UpdateModel): Builder {
            return Builder().withUpdateModel(updateModel)
        }
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        var updateModel: UpdateModel? = null
        var renderingStrategy: RenderingStrategy? = null

        fun withUpdateModel(updateModel: UpdateModel): Builder {
            this.updateModel = updateModel
            return this
        }

        fun withRenderingStrategy(renderingStrategy: RenderingStrategy): Builder {
            this.renderingStrategy = renderingStrategy
            return this
        }

        fun build(): UpdateRenderer {
            return UpdateRenderer(this)
        }
    }
}
