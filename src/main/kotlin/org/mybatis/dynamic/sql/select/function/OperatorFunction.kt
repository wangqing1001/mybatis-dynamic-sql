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
package org.mybatis.dynamic.sql.select.function

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.toFragmentCollector
import java.util.ArrayList
import java.util.Arrays
import java.util.Objects
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * 运算符函数,如加、减、乘、除等。
 */
open class OperatorFunction<T>(
    private val operator: String,
    firstColumn: BasicColumn,
    protected val secondColumn: BasicColumn,
    protected val subsequentColumns: List<BasicColumn>
) : AbstractUniTypeFunction<T, OperatorFunction<T>>(firstColumn) {

    override fun copy(): OperatorFunction<T> {
        return OperatorFunction(operator, column, secondColumn, subsequentColumns)
    }

    override fun render(renderingContext: RenderingContext): FragmentAndParameters {
        val paddedOperator = " $operator "
        listOf(column, secondColumn)
        return listOf(listOf(column, secondColumn), subsequentColumns).flatten().map { it.render(renderingContext) }
            .toFragmentCollector().toFragmentAndParameters(paddedOperator, "(", ")")
    }

}
