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
package org.mybatis.dynamic.sql.update

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.select.SubQueryRenderer
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.ColumnToColumnMapping
import org.mybatis.dynamic.sql.util.ConstantMapping
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.NullMapping
import org.mybatis.dynamic.sql.util.SelectMapping
import org.mybatis.dynamic.sql.util.StringConstantMapping
import org.mybatis.dynamic.sql.util.StringUtilities
import org.mybatis.dynamic.sql.util.UpdateMappingVisitor
import org.mybatis.dynamic.sql.util.ValueMapping
import org.mybatis.dynamic.sql.util.ValueOrNullMapping
import org.mybatis.dynamic.sql.util.ValueWhenPresentMapping

/**
 * set 短语访问器,负责将列映射转换为 update 语句中的 set 短语。
 */
class SetPhraseVisitor(private val renderingContext: RenderingContext) :
    UpdateMappingVisitor<FragmentAndParameters?>() {

    override fun visit(mapping: NullMapping): FragmentAndParameters? {
        return buildNullFragment(mapping)
    }

    override fun visit(mapping: ConstantMapping): FragmentAndParameters {
        val columnName = renderingContext.aliasedColumnName(mapping.column())
        val constant = mapping.constant()
        return FragmentAndParameters("$columnName = $constant")
    }

    override fun visit(mapping: StringConstantMapping): FragmentAndParameters {
        val columnName = renderingContext.aliasedColumnName(mapping.column())
        val constant = StringUtilities.formatConstantForSQL(mapping.constant())
        return FragmentAndParameters("$columnName = $constant")
    }

    override fun <T> visit(mapping: ValueMapping<T>): FragmentAndParameters {
        return buildValueFragment(mapping, mapping.value())
    }

    override fun <T> visit(mapping: ValueOrNullMapping<T>): FragmentAndParameters? {
        return mapping.value()
            .map { v: Any -> buildValueFragment(mapping, v) }
            .orElseGet { buildNullFragment(mapping) }
    }

    override fun <T> visit(mapping: ValueWhenPresentMapping<T>): FragmentAndParameters? {
        val value = mapping.value()?:return null
        return buildValueFragment(mapping, value)
    }

    override fun visit(mapping: SelectMapping): FragmentAndParameters? {
        val columnName = renderingContext.aliasedColumnName(mapping.column())
        val prefix = "$columnName = ("
        return SubQueryRenderer(mapping.selectModel(),renderingContext,prefix,")").render()
    }

    override fun visit(mapping: ColumnToColumnMapping): FragmentAndParameters {
        val columnName = renderingContext.aliasedColumnName(mapping.column())
        return mapping.rightColumn().render(renderingContext).mapFragment { "$columnName = $it" }
    }

    private fun <T> buildValueFragment(mapping: AbstractColumnMapping, value: T?): FragmentAndParameters {
        val parameterInfo = renderingContext.calculateParameterInfo(mapping.column())
        val columnName = renderingContext.aliasedColumnName(mapping.column())
        val fragment = "$columnName = ${parameterInfo.renderedPlaceHolder}"
        return FragmentAndParameters(fragment,mapOf(parameterInfo.parameterMapKey to value))
    }

    private fun buildNullFragment(mapping: AbstractColumnMapping): FragmentAndParameters {
        val columnName = renderingContext.aliasedColumnName(mapping.column())
        return FragmentAndParameters("$columnName = null")
    }
}
