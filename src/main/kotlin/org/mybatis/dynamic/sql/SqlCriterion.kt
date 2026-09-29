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
package org.mybatis.dynamic.sql

import java.util.*

abstract class SqlCriterion {

    private val subCriteria: MutableList<AndOrCriteriaGroup> = mutableListOf()

    protected constructor()

    protected constructor(builder: AbstractBuilder<*>) {
        subCriteria.addAll(builder.subCriteria)
    }


    abstract fun <R> accept(visitor: SqlCriterionVisitor<R>): R


    fun subCriteria(): MutableList<AndOrCriteriaGroup?> {
        return Collections.unmodifiableList<AndOrCriteriaGroup?>(subCriteria)
    }


    abstract class AbstractBuilder<T : AbstractBuilder<T>> {

        val subCriteria: MutableList<AndOrCriteriaGroup> = mutableListOf()

        fun withSubCriteria(subCriteria: List<AndOrCriteriaGroup>): T {
            this.subCriteria.addAll(subCriteria)
            return self()
        }

        protected abstract fun self():T
    }
}
