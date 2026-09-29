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

import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionModel
import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionRenderer
import org.mybatis.dynamic.sql.exception.NonRenderingWhereClauseException
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import java.util.Optional

/**
 * where 子句渲染器。
 */
class WhereRenderer private constructor(builder: Builder) : AbstractBooleanExpressionRenderer("where", builder) { //$NON-NLS-1$

    override fun render(): Optional<FragmentAndParameters> {
        val whereClause = super.render()

        return if (whereClause.isPresent || renderingContext.isNonRenderingClauseAllowed()) {
            whereClause
        } else {
            throw NonRenderingWhereClauseException()
        }
    }

    class Builder(whereModel: AbstractBooleanExpressionModel) : AbstractBuilder<Builder>(whereModel) {


        override fun self(): Builder {
            return this
        }

        fun build(): WhereRenderer {
            return WhereRenderer(this)
        }
    }

    companion object {
        @JvmStatic
        fun withWhereModel(whereModel: AbstractBooleanExpressionModel): Builder {
            return Builder(whereModel)
        }
    }
}
