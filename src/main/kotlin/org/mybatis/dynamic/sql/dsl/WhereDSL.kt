/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.dsl

import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.NullCriterion
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.where.WhereApplier

/**
 * where 子句 DSL,用于构建独立的 where 子句。
 */
class WhereDSL : BooleanOperations<WhereDSL> {
    private val initialCriterion: SqlCriterion
    private val subCriteria: MutableList<AndOrCriteriaGroup> = mutableListOf()

    constructor() {
        initialCriterion = NullCriterion()
    }

    constructor(initialCriterion: SqlCriterion, subCriteria: List<AndOrCriteriaGroup>) {
        this.initialCriterion = initialCriterion
        this.subCriteria.addAll(subCriteria)
    }

    override fun addSubCriterion(subCriterion: AndOrCriteriaGroup): WhereDSL {
        subCriteria.add(subCriterion)
        return this
    }

    fun toWhereApplier(): WhereApplier {
        return WhereApplier(initialCriterion, subCriteria)
    }
}
