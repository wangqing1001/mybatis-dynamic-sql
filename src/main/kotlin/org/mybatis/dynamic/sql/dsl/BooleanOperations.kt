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

import org.mybatis.dynamic.sql.*
import java.util.*

/**
 * 布尔运算接口,为 DSL 提供 and/or 操作支持。
 */
interface BooleanOperations<T : BooleanOperations<T>> {

    fun <S> and(
        column: BindableColumn<S>,
        condition: RenderableCondition<S>,
        vararg subCriteria: AndOrCriteriaGroup
    ): T {
        return and(column, condition, listOf(*subCriteria))
    }

    fun <S> and(
        column: BindableColumn<S>,
        condition: RenderableCondition<S>,
        subCriteria: List<AndOrCriteriaGroup>
    ): T {
        val initialCriterion = buildCriterion(column, condition)
        return addSubCriterion("and",initialCriterion , subCriteria) //$NON-NLS-1$
    }

    fun and(existsPredicate: ExistsPredicate, vararg subCriteria: AndOrCriteriaGroup): T {
        return and(existsPredicate, Arrays.asList(*subCriteria))
    }

    fun and(existsPredicate: ExistsPredicate, subCriteria: List<AndOrCriteriaGroup>): T {
        return addSubCriterion("and", buildCriterion(existsPredicate), subCriteria) //$NON-NLS-1$
    }

    fun and(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): T {
        return and(initialCriterion, Arrays.asList(*subCriteria))
    }

    fun and(initialCriterion: SqlCriterion, subCriteria: List<AndOrCriteriaGroup>): T {
        return addSubCriterion("and", buildCriterion(initialCriterion), subCriteria) //$NON-NLS-1$
    }

    fun and(criteria: List<AndOrCriteriaGroup>): T {
        return addSubCriterion("and", criteria) //$NON-NLS-1$
    }

    fun <S> or(
        column: BindableColumn<S>,
        condition: RenderableCondition<S>,
        vararg subCriteria: AndOrCriteriaGroup
    ): T {
        return or(column, condition, listOf(*subCriteria))
    }

    fun <S> or(
        column: BindableColumn<S>,
        condition: RenderableCondition<S>,
        subCriteria: List<AndOrCriteriaGroup>
    ): T {
        return addSubCriterion("or", buildCriterion(column, condition), subCriteria) //$NON-NLS-1$
    }

    fun or(existsPredicate: ExistsPredicate, vararg subCriteria: AndOrCriteriaGroup): T {
        return or(existsPredicate, listOf(*subCriteria))
    }

    fun or(existsPredicate: ExistsPredicate, subCriteria: List<AndOrCriteriaGroup>): T {
        return addSubCriterion("or", buildCriterion(existsPredicate), subCriteria) //$NON-NLS-1$
    }

    fun or(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): T {
        return or(initialCriterion, Arrays.asList(*subCriteria))
    }

    fun or(initialCriterion: SqlCriterion, subCriteria: List<AndOrCriteriaGroup>): T {
        return addSubCriterion("or", buildCriterion(initialCriterion), subCriteria) //$NON-NLS-1$
    }

    fun or(criteria: List<AndOrCriteriaGroup>): T {
        return addSubCriterion( "or", criteria) //$NON-NLS-1$
    }

    private fun <R> buildCriterion(column: BindableColumn<R>,condition: RenderableCondition<R>): SqlCriterion {
        return ColumnAndConditionCriterion(column,condition)
    }

    private fun buildCriterion(existsPredicate: ExistsPredicate): SqlCriterion {
        return ExistsCriterion(existsPredicate)
    }

    private fun buildCriterion(initialCriterion: SqlCriterion): SqlCriterion {
        return CriteriaGroup(initialCriterion)
    }

    private fun  addSubCriterion(connector: String,initialCriterion: SqlCriterion,subCriteria: List<AndOrCriteriaGroup>): T {
        return addSubCriterion(
            AndOrCriteriaGroup.Builder()
                .withInitialCriterion(initialCriterion)
                .withConnector(connector)
                .withSubCriteria(subCriteria)
                .build()
        )
    }

    private fun addSubCriterion(connector: String,criteria: List<AndOrCriteriaGroup>): T {
        return addSubCriterion(
            AndOrCriteriaGroup.Builder()
                .withConnector(connector)
                .withInitialCriterion(NullCriterion())
                .withSubCriteria(criteria)
                .build()
        )
    }

    fun addSubCriterion(subCriterion: AndOrCriteriaGroup): T
}
