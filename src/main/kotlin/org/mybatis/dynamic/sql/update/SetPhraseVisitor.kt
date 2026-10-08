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
import org.mybatis.dynamic.sql.select.render.SubQueryRenderer
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
import java.util.Objects
import java.util.Optional

/**
 * set 短语访问器,负责将列映射转换为 update 语句中的 set 短语。
 */
class SetPhraseVisitor(renderingContext: RenderingContext) :
    UpdateMappingVisitor<Optional<FragmentAndParameters>>() {

    private val renderingContext: RenderingContext = Objects.requireNonNull(renderingContext)

    override fun visit(mapping: NullMapping): Optional<FragmentAndParameters> {
        return buildNullFragment(mapping)
    }

    override fun visit(mapping: ConstantMapping): Optional<FragmentAndParameters> {
        val fragment = renderingContext.aliasedColumnName(mapping.column())  + " = " + mapping.constant() //$NON-NLS-1$
        return Optional.of(FragmentAndParameters(fragment))
    }

    override fun visit(mapping: StringConstantMapping): Optional<FragmentAndParameters> {
        val fragment = renderingContext.aliasedColumnName(mapping.column()) + " = " + StringUtilities.formatConstantForSQL(mapping.constant())

        return Optional.of(FragmentAndParameters(fragment))
    }

    override fun <T> visit(mapping: ValueMapping<T>): Optional<FragmentAndParameters> {
        return buildValueFragment(mapping, mapping.value())
    }

    override fun <T> visit(mapping: ValueOrNullMapping<T>): Optional<FragmentAndParameters> {
        return mapping.value()
            .map { v: Any -> buildValueFragment(mapping, v) }
            .orElseGet { buildNullFragment(mapping) }
    }

    override fun <T> visit(mapping: ValueWhenPresentMapping<T>): Optional<FragmentAndParameters> {
        val value = mapping.value()?:return Optional.empty()
        return buildValueFragment(mapping, value)
    }

    override fun visit(mapping: SelectMapping): Optional<FragmentAndParameters> {
        val prefix = renderingContext.aliasedColumnName(mapping.column()) + " = (" //$NON-NLS-1$
        val fragmentAndParameters = SubQueryRenderer(mapping.selectModel(),renderingContext,prefix,")").render()
        return Optional.of(fragmentAndParameters)
    }

    override fun visit(mapping: ColumnToColumnMapping): Optional<FragmentAndParameters> {
        val fragmentAndParameters = mapping.rightColumn().render(renderingContext)
            .mapFragment { f: String -> renderingContext.aliasedColumnName(mapping.column()) + " = " + f } //$NON-NLS-1$
        return Optional.of(fragmentAndParameters)
    }

    private fun <T> buildValueFragment(mapping: AbstractColumnMapping, value: T?): Optional<FragmentAndParameters> {
        val parameterInfo = renderingContext.calculateParameterInfo(mapping.column())
        val setPhrase = renderingContext.aliasedColumnName(mapping.column()) + " = "  + parameterInfo.renderedPlaceHolder

        return Optional.of(FragmentAndParameters(setPhrase, mapOf(parameterInfo.parameterMapKey to value)))
    }

    private fun buildNullFragment(mapping: AbstractColumnMapping): Optional<FragmentAndParameters> {
        return Optional.of(
            FragmentAndParameters(renderingContext.aliasedColumnName(mapping.column()) + " = null") //$NON-NLS-1$
        )
    }
}
