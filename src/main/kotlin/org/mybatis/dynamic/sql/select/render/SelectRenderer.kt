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
package org.mybatis.dynamic.sql.select.render

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import java.util.Objects

/**
 * select 语句渲染器。
 */
class SelectRenderer private constructor(builder: Builder) {
    private val selectModel: SelectModel
    private val renderingStrategy: RenderingStrategy

    init {
        selectModel = Objects.requireNonNull(builder.selectModel!!)
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy!!)
    }

    fun render(): SelectStatementProvider {
        val renderingContext = RenderingContext.withRenderingStrategy(renderingStrategy)
            .withStatementConfiguration(selectModel.statementConfiguration())
            .build()

        val fragmentAndParameters = SubQueryRenderer.withSelectModel(selectModel)
            .withRenderingContext(renderingContext)
            .build()
            .render()

        return DefaultSelectStatementProvider.withSelectStatement(fragmentAndParameters.fragment())
            .withParameters(fragmentAndParameters.parameters())
            .build()
    }

    companion object {
        @JvmStatic
        fun withSelectModel(selectModel: SelectModel): Builder {
            return Builder().withSelectModel(selectModel)
        }
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        var selectModel: SelectModel? = null
        var renderingStrategy: RenderingStrategy? = null

        fun withRenderingStrategy(renderingStrategy: RenderingStrategy): Builder {
            this.renderingStrategy = renderingStrategy
            return this
        }

        fun withSelectModel(selectModel: SelectModel): Builder {
            this.selectModel = selectModel
            return this
        }

        fun build(): SelectRenderer {
            return SelectRenderer(this)
        }
    }
}
