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
import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion
import org.mybatis.dynamic.sql.CriteriaGroup
import org.mybatis.dynamic.sql.RenderableCondition
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.select.having.HavingApplier

/**
 * 支持 having 子句的 DSL 接口。
 */
interface HavingOperations<F : BooleanOperations<F>> {

    fun <T> having(column: BindableColumn<T>,condition: RenderableCondition<T>,vararg subCriteria: AndOrCriteriaGroup): F {
        return having(column, condition, listOf(*subCriteria))
    }

    fun <T> having(column: BindableColumn<T>,condition: RenderableCondition<T>,subCriteria: List<AndOrCriteriaGroup>): F {
        return having(ColumnAndConditionCriterion(column,condition,subCriteria))
    }

    fun having(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): F {
        return having(initialCriterion, listOf(*subCriteria))
    }

    fun having(initialCriterion: SqlCriterion, subCriteria: List<AndOrCriteriaGroup>): F {
        return having(CriteriaGroup(initialCriterion,subCriteria))
    }

    fun having(initialCriterion: SqlCriterion): F

    fun applyHaving(havingApplier: HavingApplier): F
}
