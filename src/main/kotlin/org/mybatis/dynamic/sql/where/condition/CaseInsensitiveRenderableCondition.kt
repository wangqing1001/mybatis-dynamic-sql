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
package org.mybatis.dynamic.sql.where.condition

import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.RenderableCondition
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters

/**
 * 不区分大小写的可渲染条件接口。
 * 渲染左列时,将列片段包装为 upper(...)。
 */
interface CaseInsensitiveRenderableCondition<T> : RenderableCondition<T> {

    override fun renderLeftColumn(
        renderingContext: RenderingContext,
        leftColumn: BindableColumn<T>
    ): FragmentAndParameters? {
        return super.renderLeftColumn(renderingContext, leftColumn)?.mapFragment { s: String -> "upper(" + s + ")" } //$NON-NLS-1$ //$NON-NLS-2$
    }
}
