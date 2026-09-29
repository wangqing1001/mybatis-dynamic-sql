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
 * This class represents a criteria group without an AND or an OR connector. This is useful
 * in situations where the initial SqlCriterion in a list should be further grouped
 * as in an expression like ((A &lt; 5 and B &gt; 6) or C = 3)
 *
 * @author Jeff Butler, inspired by @JoshuaJeme
 *
 * @since 1.4.0
 */
open class CriteriaGroup protected constructor(builder: AbstractGroupBuilder<*>) : SqlCriterion(builder) {

    private val initialCriterion: SqlCriterion

    init {
        initialCriterion = builder.initialCriterion
    }

    fun initialCriterion(): SqlCriterion {
        return initialCriterion
    }

    override fun <R> accept(visitor: SqlCriterionVisitor<R>): R {
        return visitor.visit(this)
    }

    abstract class AbstractGroupBuilder<T : AbstractGroupBuilder<T>> : AbstractBuilder<T>() {

        lateinit var initialCriterion: SqlCriterion

        fun withInitialCriterion(initialCriterion: SqlCriterion): T {
            this.initialCriterion = initialCriterion
            return self()
        }

    }

    class Builder : AbstractGroupBuilder<Builder>() {

        fun build(): CriteriaGroup {
            return CriteriaGroup(this)
        }

        override fun self(): Builder {
            return this
        }
    }
}
