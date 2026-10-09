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
package org.mybatis.dynamic.sql.update

import org.mybatis.dynamic.sql.order.OrderByRenderer
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.render.TableAliasCalculator
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.FragmentCollector
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.util.toFragmentCollector

/**
 * update 渲染器。
 */
class UpdateRenderer(
    private val updateModel: UpdateModel,
    renderingStrategy: RenderingStrategy
) {

    private val renderingContext: RenderingContext
    private val visitor: SetPhraseVisitor

    init {
        val tableAliasCalculator = updateModel.tableAlias()?.let {
            ExplicitTableAliasCalculator(mapOf(updateModel.table() to it))
        } ?:TableAliasCalculator.empty()
        val statementConfiguration = updateModel.statementConfiguration()
        renderingContext = RenderingContext(renderingStrategy,statementConfiguration,tableAliasCalculator)
        visitor = SetPhraseVisitor(renderingContext)
    }

    fun render(): UpdateStatementProvider {
        val list = mutableListOf<FragmentAndParameters>()
        list.add(calculateUpdateStatementStart())
        list.add(calculateSetPhrase())
        val whereClause = calculateWhereClause()
        if(whereClause !=null){
            list.add(whereClause)
        }
        val orderByClause = calculateOrderByClause()
        if(orderByClause !=null){
            list.add(orderByClause)
        }
        val limitClause = calculateLimitClause()
        if(limitClause !=null){
            list.add(limitClause)
        }
        return toUpdateStatementProvider(list.toFragmentCollector())
    }

    private fun toUpdateStatementProvider(fragmentCollector: FragmentCollector): UpdateStatementProvider {
        val updateStatement = fragmentCollector.collectFragments(" ")
        return DefaultUpdateStatementProvider(updateStatement,fragmentCollector.parameters())
    }

    private fun calculateUpdateStatementStart(): FragmentAndParameters {
        val aliasedTableName = renderingContext.aliasedTableName(updateModel.table())
        return FragmentAndParameters("update $aliasedTableName")
    }

    private fun calculateSetPhrase(): FragmentAndParameters {
        val fragmentCollector = updateModel.columnMappings()
            .mapNotNull { it.accept(visitor) }.toFragmentCollector()
        Validator.assertFalse(fragmentCollector.isEmpty(), "ERROR.18")
        return fragmentCollector.toFragmentAndParameters(", ", "set ", "")
    }

    private fun calculateWhereClause(): FragmentAndParameters? {
        return updateModel.whereModel()?.render(renderingContext)
    }

    private fun calculateLimitClause(): FragmentAndParameters? {
        val limit = updateModel.limit() ?: return null
        val parameterInfo = renderingContext.calculateLimitParameterInfo()
        val fragment = "limit ${parameterInfo.renderedPlaceHolder}"
        return FragmentAndParameters(fragment,mapOf(parameterInfo.parameterMapKey to limit))
    }

    private fun calculateOrderByClause():FragmentAndParameters? {
        val orderByModel = updateModel.orderByModel()?:return null
        return OrderByRenderer(orderByModel,renderingContext).render()
    }

}
