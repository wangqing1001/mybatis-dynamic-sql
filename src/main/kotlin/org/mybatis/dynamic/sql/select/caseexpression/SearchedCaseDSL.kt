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
package org.mybatis.dynamic.sql.select.caseexpression

import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion
import org.mybatis.dynamic.sql.CriteriaGroup
import org.mybatis.dynamic.sql.RenderableCondition
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.dsl.BooleanOperations
import java.util.Arrays

/**
 * 搜索型 case 表达式 DSL。
 */
class SearchedCaseDSL private constructor() : ElseDSL<SearchedCaseDSL.SearchedCaseEnder> {
    private val whenConditions: MutableList<SearchedCaseWhenCondition> = ArrayList()
    private var elseValue: BasicColumn? = null

    fun <T> `when`(
        column: BindableColumn<T>,
        condition: RenderableCondition<T>,
        vararg subCriteria: AndOrCriteriaGroup
    ): WhenDSL {
        return `when`(column, condition, Arrays.asList(*subCriteria))
    }

    fun <T> `when`(
        column: BindableColumn<T>,
        condition: RenderableCondition<T>,
        subCriteria: List<AndOrCriteriaGroup>
    ): WhenDSL {
        val sqlCriterion = ColumnAndConditionCriterion.withColumn(column)
            .withCondition(condition)
            .withSubCriteria(subCriteria)
            .build()

        return initialize(sqlCriterion)
    }

    fun `when`(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): WhenDSL {
        return `when`(initialCriterion, listOf(*subCriteria))
    }

    fun `when`(initialCriterion: SqlCriterion, subCriteria: List<AndOrCriteriaGroup>): WhenDSL {
        val sqlCriterion = CriteriaGroup.Builder()
            .withInitialCriterion(initialCriterion)
            .withSubCriteria(subCriteria)
            .build()

        return initialize(sqlCriterion)
    }

    private fun initialize(sqlCriterion: SqlCriterion): WhenDSL {
        return WhenDSL(sqlCriterion)
    }

    @Suppress("FunctionName")
    override fun else_(column: BasicColumn): SearchedCaseEnder {
        elseValue = column
        return SearchedCaseEnder()
    }

    fun end(): SearchedCaseModel {
        return SearchedCaseModel.Builder()
            .withElseValue(elseValue)
            .withWhenConditions(whenConditions)
            .build()
    }

    inner class WhenDSL(sqlCriterion: SqlCriterion) :
        BooleanOperations<WhenDSL>, ThenDSL<SearchedCaseDSL> {
        protected val initialCriterion: SqlCriterion
        protected val subCriteria: MutableList<AndOrCriteriaGroup> = ArrayList()

        init {
            initialCriterion = sqlCriterion
        }

        override fun then(column: BasicColumn): SearchedCaseDSL {
            whenConditions.add(SearchedCaseWhenCondition.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .withThenValue(column)
                .build())
            return this@SearchedCaseDSL
        }

        override fun addSubCriterion(subCriterion: AndOrCriteriaGroup): WhenDSL {
            subCriteria.add(subCriterion)
            return this
        }
    }

    inner class SearchedCaseEnder {
        fun end(): SearchedCaseModel {
            return this@SearchedCaseDSL.end()
        }
    }

    companion object {
        @JvmStatic
        fun searchedCase(): SearchedCaseDSL {
            return SearchedCaseDSL()
        }
    }
}
