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
package org.mybatis.dynamic.sql.delete

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.order.OrderByModel
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.render.TableAliasCalculator
import org.mybatis.dynamic.sql.select.paging.LimitModel
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.toFragmentCollector
import org.mybatis.dynamic.sql.where.WhereModel
import java.util.function.Function

/**
 * delete 模型。
 */
class DeleteModel @JvmOverloads constructor(
    private val table: SqlTable,
    private val statementConfiguration: StatementConfiguration,
    private val tableAlias: String? = null,
    private val whereModel: WhereModel? = null,
    private val limitModel: LimitModel? = null,
    private val orderByModel: OrderByModel? = null,
) {

    fun table(): SqlTable {
        return table
    }

    fun tableAlias(): String? {
        return tableAlias
    }

    fun whereModel(): WhereModel? {
        return whereModel
    }

    fun limitModel(): LimitModel? {
        return limitModel
    }

    fun orderByModel(): OrderByModel? {
        return orderByModel
    }

    fun statementConfiguration(): StatementConfiguration {
        return statementConfiguration
    }

    fun <R> map(adapterFunction: Function<DeleteModel, R>): R {
        return adapterFunction.apply(this)
    }

    fun render(renderingStrategy: RenderingStrategy): DeleteStatementProvider {
        val tableAliasCalculator = tableAlias?.let {
            ExplicitTableAliasCalculator(mapOf(table to it))
        }?:TableAliasCalculator.empty()
        val renderingContext = RenderingContext(renderingStrategy,statementConfiguration,tableAliasCalculator)
        val table = renderingContext.aliasedTableName(table)
        val list = mutableListOf(FragmentAndParameters("delete from $table"))
        whereModel?.render(renderingContext)?.let { list.add(it) }
        orderByModel?.render(renderingContext)?.let { list.add(it) }
        limitModel?.render(renderingContext)?.let { list.add(it) }
        val fragmentCollector = list.toFragmentCollector()
        val deleteStatement = fragmentCollector.collectFragments(" ")
        val parameters = fragmentCollector.parameters()
        return DefaultDeleteStatementProvider(deleteStatement,parameters)
    }

}
