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
package org.mybatis.dynamic.sql.where.render

import org.mybatis.dynamic.sql.AbstractBooleanExpressionModel
import org.mybatis.dynamic.sql.AbstractBooleanExpressionRenderer
import org.mybatis.dynamic.sql.exception.NonRenderingWhereClauseException
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters

/**
 * where 子句渲染器。
 */
class WhereRenderer(
    model: AbstractBooleanExpressionModel,
    renderingContext: RenderingContext
) : AbstractBooleanExpressionRenderer("where", model,renderingContext) {

    override fun render(): FragmentAndParameters? {
        val whereClause = super.render()
        if (whereClause!=null || renderingContext.isNonRenderingClauseAllowed()) {
            return whereClause
        }
        throw NonRenderingWhereClauseException()
    }

}
