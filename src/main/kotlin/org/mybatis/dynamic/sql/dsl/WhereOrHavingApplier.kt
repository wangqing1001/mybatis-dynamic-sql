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
import java.util.function.Consumer

/**
 * where 或 having 应用的抽象基类。
 *
 * @param T 实现类型
 */
abstract class WhereOrHavingApplier<T : WhereOrHavingApplier<T>> {
    private val initialCriterion: SqlCriterion
    private val subCriteria: MutableList<AndOrCriteriaGroup> = mutableListOf()
    private val after: Consumer<BooleanOperations<*>>

    constructor(initialCriterion: SqlCriterion, subCriteria: List<AndOrCriteriaGroup>) {
        this.initialCriterion = initialCriterion
        this.subCriteria.addAll(subCriteria)
        after = Consumer { }
    }

    constructor(
        initialCriterion: SqlCriterion,
        subCriteria: List<AndOrCriteriaGroup>,
        after: Consumer<BooleanOperations<*>>
    ) {
        this.initialCriterion = initialCriterion
        this.subCriteria.addAll(subCriteria)
        this.after = after
    }

    fun initialCriterion(): SqlCriterion {
        return initialCriterion
    }

    fun subCriteria(): List<AndOrCriteriaGroup> {
        val scc = SubCriteriaCollector()
        after.accept(scc)
        val answer: MutableList<AndOrCriteriaGroup> = ArrayList()
        answer.addAll(subCriteria)
        answer.addAll(scc.subCriteria())
        return answer
    }

    /**
     * 返回一个组合 applier,先执行本操作再执行 after 操作。
     *
     * @param after 在本操作之后执行的操作
     *
     * @return 先执行本操作再执行 after 操作的组合 applier
     */
    fun andThen(after: Consumer<BooleanOperations<*>>): T {
        val newConsumer = this.after.andThen(after)
        return buildNew(initialCriterion, subCriteria, newConsumer)
    }

    protected abstract fun buildNew(
        initialCriterion: SqlCriterion,
        subCriteria: List<AndOrCriteriaGroup>,
        after: Consumer<BooleanOperations<*>>
    ): T

    private class SubCriteriaCollector : BooleanOperations<SubCriteriaCollector> {
        private val subCriteria: MutableList<AndOrCriteriaGroup> = ArrayList()

        override fun addSubCriterion(subCriterion: AndOrCriteriaGroup): SubCriteriaCollector {
            subCriteria.add(subCriterion)
            return this
        }

        fun subCriteria(): List<AndOrCriteriaGroup> {
            return subCriteria
        }
    }
}
