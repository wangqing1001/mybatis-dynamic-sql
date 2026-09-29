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

/**
 * This class represents a criteria group with either an AND or an OR connector.
 * This class is intentionally NOT derived from SqlCriterion because we only want it to be
 * available where an AND or an OR condition is appropriate.
 *
 * @author Jeff Butler
 *
 * @since 1.4.0
 */
class AndOrCriteriaGroup private constructor(builder: Builder) {

    private val connector: String
    private val initialCriterion: SqlCriterion
    private val subCriteria: MutableList<AndOrCriteriaGroup>

    init {
        connector = Objects.requireNonNull(builder.connector)
        initialCriterion = Objects.requireNonNull(builder.initialCriterion)
        subCriteria = builder.subCriteria
    }

    fun connector(): String {
        return connector
    }

    fun initialCriterion(): SqlCriterion {
        return initialCriterion
    }

    fun subCriteria(): MutableList<AndOrCriteriaGroup> {
        return Collections.unmodifiableList(subCriteria)
    }

    class Builder {
        lateinit var connector: String
        lateinit var initialCriterion: SqlCriterion
        val subCriteria: MutableList<AndOrCriteriaGroup> = mutableListOf()

        fun withConnector(connector: String): Builder {
            this.connector = connector
            return this
        }

        fun withInitialCriterion(initialCriterion: SqlCriterion): Builder {
            this.initialCriterion = initialCriterion
            return this
        }

        fun withSubCriteria(subCriteria: List<AndOrCriteriaGroup>): Builder {
            this.subCriteria.addAll(subCriteria)
            return this
        }

        fun build(): AndOrCriteriaGroup {
            return AndOrCriteriaGroup(this)
        }

    }
}
