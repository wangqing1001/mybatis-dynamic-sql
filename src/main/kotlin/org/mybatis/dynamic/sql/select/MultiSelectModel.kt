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
package org.mybatis.dynamic.sql.select

import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.order.OrderByModel
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.paging.PagingModel
import org.mybatis.dynamic.sql.select.render.MultiSelectRenderer
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.util.Validator
import java.util.Objects
import java.util.stream.Stream

/**
 * 多 select 语句模型。包含初始 select 与多个 union 查询。
 */
class MultiSelectModel @JvmOverloads constructor(
    private val initialSelect: SelectModel,
    private val unionQueries: List<UnionQuery>,
    statementConfiguration: StatementConfiguration,
    orderByModel: OrderByModel? = null,
    pagingModel: PagingModel? = null,
) : AbstractSelectModel(orderByModel,pagingModel,statementConfiguration) {

    init {
        Validator.assertNotEmpty(unionQueries, "ERROR.35") //$NON-NLS-1$
    }

    fun initialSelect(): SelectModel {
        return initialSelect
    }

    fun unionQueries(): List<UnionQuery> {
        return unionQueries
    }

    fun render(renderingStrategy: RenderingStrategy): SelectStatementProvider {
        return MultiSelectRenderer(this,renderingStrategy).render()
    }


}
