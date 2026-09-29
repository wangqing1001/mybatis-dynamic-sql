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
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.select.join.JoinSpecification
import org.mybatis.dynamic.sql.select.join.JoinType
import java.util.ArrayList
import java.util.Objects

/**
 * join 支持的抽象基类。
 *
 * @param D 持有本 join 的 DSL 类型
 * @param F 本 join 实现自身的类型
 */
abstract class AbstractJoinSupport<D : JoinOperations<F>, F : AbstractJoinSupport<D, F>> :
    JoinOperations<F>, BooleanOperations<F> {

    private val joinType: JoinType
    private val joinTable: TableExpression
    private val initialCriterion: SqlCriterion
    private val subCriteria: MutableList<AndOrCriteriaGroup> = ArrayList()

    protected constructor(joinType: JoinType, joinTable: TableExpression, initialCriterion: SqlCriterion) {
        this.joinType = joinType
        this.joinTable = joinTable
        this.initialCriterion = initialCriterion
    }

    override fun addSubCriterion(subCriterion: AndOrCriteriaGroup): F {
        subCriteria.add(subCriterion)
        return getThis()
    }

    protected abstract fun getThis(): F

    abstract fun endJoin(): D

    // 注意:Kotlin 中 protected 成员仅子类可访问(Java 中同包也可访问),
    // 此处 buildJoinModel() 需要跨类调用,故改为 internal。
    internal fun toJoinSpecification(): JoinSpecification {
        return JoinSpecification.withJoinTable(Objects.requireNonNull(joinTable))
            .withJoinType(Objects.requireNonNull(joinType))
            .withInitialCriterion(initialCriterion)
            .withSubCriteria(subCriteria)
            .build()
    }
}
