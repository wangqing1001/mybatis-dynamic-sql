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
import org.mybatis.dynamic.sql.ExistsCriterion
import org.mybatis.dynamic.sql.ExistsPredicate
import org.mybatis.dynamic.sql.NullCriterion
import org.mybatis.dynamic.sql.RenderableCondition
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.where.WhereApplier
import java.util.Arrays

/**
 * 支持 where 子句的 DSL 基类——除了 Insert 之外的所有 DSL 都支持 where。
 * 本类的目的是提供一套所有语句都可以使用的通用 where 方法。
 *
 * @param F 针对特定 SQL 语句定制的 Where DSL 实现
 */
interface WhereOperations<F : BooleanOperations<F>> {

    fun <T: Any> where(column: BindableColumn<T>,condition: RenderableCondition<T>,vararg subCriteria: AndOrCriteriaGroup): F {
        return where(column, condition, listOf(*subCriteria))
    }

    fun <T: Any> where(column: BindableColumn<T>, condition: RenderableCondition<T>, subCriteria: List<AndOrCriteriaGroup>): F {
        val sqlCriterion = ColumnAndConditionCriterion.withColumn(column)
            .withCondition(condition)
            .withSubCriteria(subCriteria)
            .build()

        return where(sqlCriterion)
    }

    fun where(existsPredicate: ExistsPredicate, vararg subCriteria: AndOrCriteriaGroup): F {
        return where(existsPredicate, Arrays.asList(*subCriteria))
    }

    fun where(existsPredicate: ExistsPredicate, subCriteria: List<AndOrCriteriaGroup>): F {
        val sqlCriterion = ExistsCriterion.Builder()
            .withExistsPredicate(existsPredicate)
            .withSubCriteria(subCriteria)
            .build()

        return where(sqlCriterion)
    }

    fun where(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): F {
        return where(initialCriterion, Arrays.asList(*subCriteria))
    }

    fun where(initialCriterion: SqlCriterion, subCriteria: List<AndOrCriteriaGroup>): F {
        val sqlCriterion = CriteriaGroup.Builder()
            .withInitialCriterion(initialCriterion)
            .withSubCriteria(subCriteria)
            .build()

        return where(sqlCriterion)
    }

    fun where(subCriteria: List<AndOrCriteriaGroup>): F {
        val sqlCriterion = CriteriaGroup.Builder()
            .withInitialCriterion(NullCriterion())
            .withSubCriteria(subCriteria)
            .build()

        return where(sqlCriterion)
    }

    fun where(): F

    fun where(initialCriterion: SqlCriterion): F

    fun applyWhere(whereApplier: WhereApplier): F
}
