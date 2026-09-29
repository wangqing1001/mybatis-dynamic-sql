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
package org.mybatis.dynamic.sql.select.join

import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.util.Validator
import java.util.ArrayList
import java.util.Objects
import java.util.stream.Stream

/**
 * 连接模型,包含一组连接规格。
 */
class JoinModel private constructor(joinSpecifications: List<JoinSpecification>) {

    private val joinSpecifications: MutableList<JoinSpecification> = ArrayList()

    init {
        Objects.requireNonNull(joinSpecifications)
        Validator.assertNotEmpty(joinSpecifications, "ERROR.15") //$NON-NLS-1$
        this.joinSpecifications.addAll(joinSpecifications)
    }

    fun joinSpecifications(): Stream<JoinSpecification> {
        return joinSpecifications.stream()
    }

    fun containsSubQueries(): Boolean {
        return joinSpecifications.stream()
            .map { joinSpecification: JoinSpecification -> joinSpecification.table() }
            .anyMatch { tableExpression: TableExpression -> tableExpression.isSubQuery }
    }

    companion object {

        @JvmStatic
        fun of(joinSpecifications: List<JoinSpecification>): JoinModel {
            return JoinModel(joinSpecifications)
        }
    }
}
