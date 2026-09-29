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

import org.mybatis.dynamic.sql.common.AbstractBooleanExpressionRenderer
import org.mybatis.dynamic.sql.select.HavingModel

/**
 * having 子句渲染器。
 */
class HavingRenderer private constructor(builder: Builder) :
    AbstractBooleanExpressionRenderer("having", builder) { //$NON-NLS-1$

    companion object {
        @JvmStatic
        fun withHavingModel(havingModel: HavingModel): Builder {
            return Builder(havingModel)
        }
    }

    class Builder(havingModel: HavingModel) : AbstractBuilder<Builder>(havingModel) {
        override fun self(): Builder {
            return this
        }

        fun build(): HavingRenderer {
            return HavingRenderer(this)
        }
    }





}
