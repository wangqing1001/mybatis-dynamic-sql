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
import org.mybatis.dynamic.sql.util.Validator

/**
 * order by 子句模型。
 */
class OrderByModel private constructor(columns: Collection<SortSpecification>) {
    private val columns: MutableList<SortSpecification> = mutableListOf()

    init {
        Validator.assertNotEmpty(columns, "ERROR.12") //$NON-NLS-1$
        this.columns.addAll(columns)
    }

    fun columns(): Collection<SortSpecification> {
        return columns
    }

    companion object {
        @JvmStatic
        fun of(columns: Collection<SortSpecification>): OrderByModel {
            return OrderByModel(columns)
        }
    }
}
