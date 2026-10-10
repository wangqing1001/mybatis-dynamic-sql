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
package org.mybatis.dynamic.sql.order

import org.mybatis.dynamic.sql.SortSpecification
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.Validator
import org.mybatis.dynamic.sql.util.toFragmentCollector

/**
 * order by 子句模型。
 */
class OrderByModel(private val columns: List<SortSpecification>) {

    init {
        Validator.assertNotEmpty(columns, "ERROR.12")
    }

    fun columns(): List<SortSpecification> {
        return columns
    }

    fun render(renderingContext: RenderingContext): FragmentAndParameters {
        return columns.map { it.renderForOrderBy(renderingContext) }.toFragmentCollector()
            .toFragmentAndParameters(", ", "order by ", "")
    }

}
