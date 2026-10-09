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
package org.mybatis.dynamic.sql.insert

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.ConstantMapping
import org.mybatis.dynamic.sql.util.FieldAndValueAndParameters
import org.mybatis.dynamic.sql.util.GeneralInsertMappingVisitor
import org.mybatis.dynamic.sql.util.NullMapping
import org.mybatis.dynamic.sql.util.StringConstantMapping
import org.mybatis.dynamic.sql.util.StringUtilities
import org.mybatis.dynamic.sql.util.ValueMapping
import org.mybatis.dynamic.sql.util.ValueOrNullMapping
import org.mybatis.dynamic.sql.util.ValueWhenPresentMapping
import java.util.Objects

/**
 * 通用 insert 值短语访问器。
 */
class GeneralInsertValuePhraseVisitor(renderingContext: RenderingContext) :
    GeneralInsertMappingVisitor<FieldAndValueAndParameters?>() {

    private val renderingContext: RenderingContext = Objects.requireNonNull(renderingContext)

    override fun visit(mapping: NullMapping): FieldAndValueAndParameters {
        return buildNullFragment(mapping)
    }

    override fun visit(mapping: ConstantMapping): FieldAndValueAndParameters {
        return FieldAndValueAndParameters(mapping.columnName(), mapping.constant())
    }

    override fun visit(mapping: StringConstantMapping): FieldAndValueAndParameters {
        val valuePhrase = StringUtilities.formatConstantForSQL(mapping.constant())
        return FieldAndValueAndParameters(mapping.columnName(), valuePhrase)
    }

    override fun <T> visit(mapping: ValueMapping<T>): FieldAndValueAndParameters {
        return buildValueFragment(mapping, mapping.value())
    }

    override fun <T> visit(mapping: ValueOrNullMapping<T>): FieldAndValueAndParameters? {
        return mapping.value().map { v: Any -> buildValueFragment(mapping, v) }
            .orElseGet { buildNullFragment(mapping) }
    }

    override fun <T> visit(mapping: ValueWhenPresentMapping<T>): FieldAndValueAndParameters? {
        val value = mapping.value() ?:return null
        return buildValueFragment(mapping, value)
    }

    private fun buildValueFragment(mapping: AbstractColumnMapping, value: Any?): FieldAndValueAndParameters {
        return buildFragment(mapping, value)
    }

    private fun buildNullFragment(mapping: AbstractColumnMapping): FieldAndValueAndParameters {
        return FieldAndValueAndParameters(mapping.columnName(), "null")
    }

    private fun buildFragment(mapping: AbstractColumnMapping, value: Any?): FieldAndValueAndParameters {
        val parameterInfo = renderingContext.calculateParameterInfo(mapping.column())
        val parameters = mapOf(parameterInfo.parameterMapKey to value)
        return FieldAndValueAndParameters(mapping.columnName(), parameterInfo.renderedPlaceHolder, parameters)
    }
}
