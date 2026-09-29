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
package org.mybatis.dynamic.sql.delete.render

import org.mybatis.dynamic.sql.common.OrderByModel
import org.mybatis.dynamic.sql.common.OrderByRenderer
import org.mybatis.dynamic.sql.delete.DeleteModel
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator
import org.mybatis.dynamic.sql.render.RenderedParameterInfo
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.render.TableAliasCalculator
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.FragmentCollector
import org.mybatis.dynamic.sql.where.WhereModel
import java.util.Objects
import java.util.Optional
import java.util.stream.Collectors

/**
 * delete 渲染器。
 */
class DeleteRenderer private constructor(builder: Builder) {
    private val deleteModel: DeleteModel
    private val renderingContext: RenderingContext

    init {
        deleteModel = Objects.requireNonNull(builder.deleteModel)
        val tableAliasCalculator = builder.deleteModel.tableAlias()
            .map { a: String -> ExplicitTableAliasCalculator.of(deleteModel.table(), a) }
            .orElseGet { TableAliasCalculator.empty() }
        renderingContext = RenderingContext
            .withRenderingStrategy(Objects.requireNonNull(builder.renderingStrategy))
            .withTableAliasCalculator(tableAliasCalculator)
            .withStatementConfiguration(deleteModel.statementConfiguration())
            .build()
    }

    fun render(): DeleteStatementProvider {
        val fragmentCollector = FragmentCollector()

        fragmentCollector.add(calculateDeleteStatementStart())
        calculateWhereClause().ifPresent { fragment: FragmentAndParameters -> fragmentCollector.add(fragment) }
        calculateOrderByClause().ifPresent { fragment: FragmentAndParameters -> fragmentCollector.add(fragment) }
        calculateLimitClause().ifPresent { fragment: FragmentAndParameters -> fragmentCollector.add(fragment) }

        return toDeleteStatementProvider(fragmentCollector)
    }

    private fun toDeleteStatementProvider(fragmentCollector: FragmentCollector): DeleteStatementProvider {
        return DefaultDeleteStatementProvider
            .withDeleteStatement(fragmentCollector.collectFragments(Collectors.joining(" "))) //$NON-NLS-1$
            .withParameters(fragmentCollector.parameters())
            .build()
    }

    private fun calculateDeleteStatementStart(): FragmentAndParameters {
        val aliasedTableName = renderingContext.aliasedTableName(deleteModel.table())
        return FragmentAndParameters.fromFragment("delete from " + aliasedTableName) //$NON-NLS-1$
    }

    private fun calculateWhereClause(): Optional<FragmentAndParameters> {
        return deleteModel.whereModel().flatMap { whereModel: WhereModel -> renderWhereClause(whereModel) }
    }

    private fun renderWhereClause(whereModel: WhereModel): Optional<FragmentAndParameters> {
        return whereModel.render(renderingContext)
    }

    private fun calculateLimitClause(): Optional<FragmentAndParameters> {
        return deleteModel.limit().map { limit: Long -> renderLimitClause(limit) }
    }

    private fun renderLimitClause(limit: Long): FragmentAndParameters {
        val parameterInfo = renderingContext.calculateLimitParameterInfo()

        return FragmentAndParameters.withFragment("limit " + parameterInfo.renderedPlaceHolder) //$NON-NLS-1$
            .withParameter(parameterInfo.parameterMapKey, limit)
            .build()
    }

    private fun calculateOrderByClause(): Optional<FragmentAndParameters> {
        return deleteModel.orderByModel().map { orderByModel: OrderByModel -> renderOrderByClause(orderByModel) }
    }

    private fun renderOrderByClause(orderByModel: OrderByModel): FragmentAndParameters {
        return OrderByRenderer(renderingContext).render(orderByModel)
    }

    companion object {
        @JvmStatic
        fun withDeleteModel(deleteModel: DeleteModel): Builder {
            return Builder().withDeleteModel(deleteModel)
        }
    }

    class Builder {
        lateinit var deleteModel: DeleteModel
        lateinit var renderingStrategy: RenderingStrategy

        fun withDeleteModel(deleteModel: DeleteModel): Builder {
            this.deleteModel = deleteModel
            return this
        }

        fun withRenderingStrategy(renderingStrategy: RenderingStrategy): Builder {
            this.renderingStrategy = renderingStrategy
            return this
        }

        fun build(): DeleteRenderer {
            return DeleteRenderer(this)
        }
    }
}
