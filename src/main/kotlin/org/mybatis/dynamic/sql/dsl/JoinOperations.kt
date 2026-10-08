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
import org.mybatis.dynamic.sql.RenderableCondition
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.SubQuery
import org.mybatis.dynamic.sql.select.join.JoinType
import org.mybatis.dynamic.sql.util.Buildable
import java.util.Arrays
import java.util.function.Function

/**
 * join 操作接口,为 DSL 提供各种 join 方法支持。
 */
interface JoinOperations<F : BooleanOperations<F>> {

    fun join(joinType: JoinType, joinTable: TableExpression, initialCriterion: SqlCriterion): F

    fun join(joinType: JoinType, joinTable: SqlTable, tableAlias: String, initialCriterion: SqlCriterion): F

    fun join(joinTable: SqlTable): JoinOnGatherer<F> {
        return JoinOnGatherer { ic: SqlCriterion -> join(JoinType.INNER, joinTable, ic) }
    }

    fun join(joinTable: SqlTable, tableAlias: String): JoinOnGatherer<F> {
        return JoinOnGatherer { ic: SqlCriterion -> join(JoinType.INNER, joinTable, tableAlias, ic) }
    }

    fun join(joinTable: Buildable<SelectModel>, tableAlias: String?): JoinOnGatherer<F> {
        return JoinOnGatherer { ic: SqlCriterion -> join(JoinType.INNER, buildSubQuery(joinTable, tableAlias), ic) }
    }

    fun join(
        joinTable: SqlTable,
        onJoinCriterion: SqlCriterion,
        vararg andJoinCriteria: AndOrCriteriaGroup
    ): F {
        return join(joinTable, onJoinCriterion, Arrays.asList(*andJoinCriteria))
    }

    fun join(
        joinTable: SqlTable,
        tableAlias: String,
        onJoinCriterion: SqlCriterion,
        vararg andJoinCriteria: AndOrCriteriaGroup
    ): F {
        return join(joinTable, tableAlias, onJoinCriterion, Arrays.asList(*andJoinCriteria))
    }

    fun join(
        joinTable: SqlTable,
        onJoinCriterion: SqlCriterion,
        andJoinCriteria: List<AndOrCriteriaGroup>
    ): F {
        return join(joinTable).on(onJoinCriterion).and(andJoinCriteria)
    }

    fun join(
        joinTable: SqlTable,
        tableAlias: String,
        onJoinCriterion: SqlCriterion,
        andJoinCriteria: List<AndOrCriteriaGroup>
    ): F {
        return join(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria)
    }

    fun join(
        subQuery: Buildable<SelectModel>,
        tableAlias: String?,
        onJoinCriterion: SqlCriterion,
        andJoinCriteria: List<AndOrCriteriaGroup>
    ): F {
        return join(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria)
    }

    fun leftJoin(joinTable: SqlTable): JoinOnGatherer<F> {
        return JoinOnGatherer { ic: SqlCriterion -> join(JoinType.LEFT, joinTable, ic) }
    }

    fun leftJoin(joinTable: SqlTable, tableAlias: String): JoinOnGatherer<F> {
        return JoinOnGatherer { ic: SqlCriterion -> join(JoinType.LEFT, joinTable, tableAlias, ic) }
    }

    fun leftJoin(joinTable: Buildable<SelectModel>, tableAlias: String?): JoinOnGatherer<F> {
        return JoinOnGatherer { ic: SqlCriterion -> join(JoinType.LEFT, buildSubQuery(joinTable, tableAlias), ic) }
    }

    fun leftJoin(
        joinTable: SqlTable,
        onJoinCriterion: SqlCriterion,
        vararg andJoinCriteria: AndOrCriteriaGroup
    ): F {
        return leftJoin(joinTable, onJoinCriterion, listOf(*andJoinCriteria))
    }

    fun leftJoin(
        joinTable: SqlTable,
        tableAlias: String,
        onJoinCriterion: SqlCriterion,
        vararg andJoinCriteria: AndOrCriteriaGroup
    ): F {
        return leftJoin(joinTable, tableAlias, onJoinCriterion, listOf(*andJoinCriteria))
    }

    fun leftJoin(
        joinTable: SqlTable,
        onJoinCriterion: SqlCriterion,
        andJoinCriteria: List<AndOrCriteriaGroup>
    ): F {
        return leftJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria)
    }

    fun leftJoin(
        joinTable: SqlTable,
        tableAlias: String,
        onJoinCriterion: SqlCriterion,
        andJoinCriteria: List<AndOrCriteriaGroup>
    ): F {
        return leftJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria)
    }

    fun leftJoin(
        subQuery: Buildable<SelectModel>,
        tableAlias: String?,
        onJoinCriterion: SqlCriterion,
        andJoinCriteria: List<AndOrCriteriaGroup>
    ): F {
        return leftJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria)
    }

    fun rightJoin(joinTable: SqlTable): JoinOnGatherer<F> {
        return JoinOnGatherer { ic: SqlCriterion -> join(JoinType.RIGHT, joinTable, ic) }
    }

    fun rightJoin(joinTable: SqlTable, tableAlias: String): JoinOnGatherer<F> {
        return JoinOnGatherer { ic: SqlCriterion -> join(JoinType.RIGHT, joinTable, tableAlias, ic) }
    }

    fun rightJoin(joinTable: Buildable<SelectModel>, tableAlias: String?): JoinOnGatherer<F> {
        return JoinOnGatherer { ic: SqlCriterion -> join(JoinType.RIGHT, buildSubQuery(joinTable, tableAlias), ic) }
    }

    fun rightJoin(
        joinTable: SqlTable,
        onJoinCriterion: SqlCriterion,
        vararg andJoinCriteria: AndOrCriteriaGroup
    ): F {
        return rightJoin(joinTable, onJoinCriterion, listOf(*andJoinCriteria))
    }

    fun rightJoin(
        joinTable: SqlTable,
        tableAlias: String,
        onJoinCriterion: SqlCriterion,
        vararg andJoinCriteria: AndOrCriteriaGroup
    ): F {
        return rightJoin(joinTable, tableAlias, onJoinCriterion, listOf(*andJoinCriteria))
    }

    fun rightJoin(
        joinTable: SqlTable,
        onJoinCriterion: SqlCriterion,
        andJoinCriteria: List<AndOrCriteriaGroup>
    ): F {
        return rightJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria)
    }

    fun rightJoin(
        joinTable: SqlTable,
        tableAlias: String,
        onJoinCriterion: SqlCriterion,
        andJoinCriteria: List<AndOrCriteriaGroup>
    ): F {
        return rightJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria)
    }

    fun rightJoin(
        subQuery: Buildable<SelectModel>,
        tableAlias: String?,
        onJoinCriterion: SqlCriterion,
        andJoinCriteria: List<AndOrCriteriaGroup>
    ): F {
        return rightJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria)
    }

    fun fullJoin(joinTable: SqlTable): JoinOnGatherer<F> {
        return JoinOnGatherer { ic: SqlCriterion -> join(JoinType.FULL, joinTable, ic) }
    }

    fun fullJoin(joinTable: SqlTable, tableAlias: String): JoinOnGatherer<F> {
        return JoinOnGatherer { ic: SqlCriterion -> join(JoinType.FULL, joinTable, tableAlias, ic) }
    }

    fun fullJoin(joinTable: Buildable<SelectModel>, tableAlias: String?): JoinOnGatherer<F> {
        return JoinOnGatherer { ic: SqlCriterion -> join(JoinType.FULL, buildSubQuery(joinTable, tableAlias), ic) }
    }

    fun fullJoin(
        joinTable: SqlTable,
        onJoinCriterion: SqlCriterion,
        vararg andJoinCriteria: AndOrCriteriaGroup
    ): F {
        return fullJoin(joinTable, onJoinCriterion, Arrays.asList(*andJoinCriteria))
    }

    fun fullJoin(
        joinTable: SqlTable,
        tableAlias: String,
        onJoinCriterion: SqlCriterion,
        vararg andJoinCriteria: AndOrCriteriaGroup
    ): F {
        return fullJoin(joinTable, tableAlias, onJoinCriterion, Arrays.asList(*andJoinCriteria))
    }

    fun fullJoin(
        joinTable: SqlTable,
        onJoinCriterion: SqlCriterion,
        andJoinCriteria: List<AndOrCriteriaGroup>
    ): F {
        return fullJoin(joinTable).on(onJoinCriterion).and(andJoinCriteria)
    }

    fun fullJoin(
        joinTable: SqlTable,
        tableAlias: String,
        onJoinCriterion: SqlCriterion,
        andJoinCriteria: List<AndOrCriteriaGroup>
    ): F {
        return fullJoin(joinTable, tableAlias).on(onJoinCriterion).and(andJoinCriteria)
    }

    fun fullJoin(
        subQuery: Buildable<SelectModel>,
        tableAlias: String?,
        onJoinCriterion: SqlCriterion,
        andJoinCriteria: List<AndOrCriteriaGroup>
    ): F {
        return fullJoin(subQuery, tableAlias).on(onJoinCriterion).and(andJoinCriteria)
    }
}

private fun buildSubQuery(selectModel: Buildable<SelectModel>, alias: String?): SubQuery {
    return SubQuery(selectModel.build(),alias)
}

/**
 * join on 条件收集器。
 */
class JoinOnGatherer<F : BooleanOperations<*>> internal constructor(
    private val builder: Function<SqlCriterion, F>
) {

    fun <T> on(joinColumn: BindableColumn<T>, joinCondition: RenderableCondition<T>): F {
        return on(ColumnAndConditionCriterion(joinColumn,joinCondition))
    }

    fun <T> on(joinColumn: BindableColumn<T>,onJoinCondition: RenderableCondition<T>,vararg subCriteria: AndOrCriteriaGroup): F {
        return on(ColumnAndConditionCriterion(joinColumn,onJoinCondition,listOf(*subCriteria)))
    }

    fun on(initialCriterion: SqlCriterion): F {
        return builder.apply(initialCriterion)
    }
}
