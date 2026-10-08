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

import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.RenderableCondition
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.FragmentCollector
import org.mybatis.dynamic.sql.util.toFragmentCollector
import java.util.Objects
import java.util.stream.Collectors

/**
 * 列与条件渲染器。将左列和条件组合渲染为一个完整的条件片段。
 */
class ColumnAndConditionRenderer<T>(
    private val column: BindableColumn<T>,
    private val condition: RenderableCondition<T>,
    private val renderingContext: RenderingContext
) {

    fun render(): FragmentAndParameters {
        val list = mutableListOf<FragmentAndParameters>()
        list.add(condition.renderLeftColumn(renderingContext, column))
        list.add(condition.renderCondition(renderingContext, column))
        return list.toFragmentCollector().toFragmentAndParameters(" ")
    }
}
