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
package org.mybatis.dynamic.sql.select

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.util.Validator
import java.util.Objects
import java.util.stream.Stream

/**
 * group by 子句模型。
 */
class GroupByModel private constructor(columns: Collection<BasicColumn>) {
    private val columns: MutableList<BasicColumn> = ArrayList()

    init {
        Objects.requireNonNull(columns)
        Validator.assertNotEmpty(columns, "ERROR.11") //$NON-NLS-1$
        this.columns.addAll(columns)
    }

    fun columns(): Stream<BasicColumn> {
        return columns.stream()
    }

    companion object {
        @JvmStatic
        fun of(columns: Collection<BasicColumn>): GroupByModel {
            return GroupByModel(columns)
        }
    }
}
