/*
 *    Copyright 2016-2025 the original author or authors.
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
package org.mybatis.dynamic.sql.util.springbatch

import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.SelectStatementProvider
import java.util.HashMap

/**
 * Spring Batch 工具类。
 */
object SpringBatchUtility {
    internal const val PARAMETER_KEY = "mybatis3_dsql_query" //$NON-NLS-1$

    /**
     * 用于与 MyBatisPagingItemReader 一起使用的查询中的常量。
     * 此值在运行时不会用于查询,因为 MyBatis Spring 集成将为 _skiprows 提供一个值。
     *
     * 此值可以用作查询中 "offset" 方法的参数,以明确实际运行时值将由 MyBatis Spring 集成提供。
     *
     * 详见 <a href="https://mybatis.org/spring/batch.html">https://mybatis.org/spring/batch.html</a>。
     */
    const val MYBATIS_SPRING_BATCH_SKIPROWS: Long = -437L

    /**
     * 用于与 MyBatisPagingItemReader 一起使用的查询中的常量。
     * 此值在运行时不会用于查询,因为 MyBatis Spring 集成将为 _pagesize 提供一个值。
     *
     * 此值可以用作查询中 "limit" 或 "fetchFirst" 方法的参数,以明确实际运行时值将由 MyBatis Spring 集成提供。
     *
     * 详见 <a href="https://mybatis.org/spring/batch.html">https://mybatis.org/spring/batch.html</a>。
     */
    const val MYBATIS_SPRING_BATCH_PAGESIZE: Long = -439L

    @JvmField
    val SPRING_BATCH_PAGING_ITEM_READER_RENDERING_STRATEGY: RenderingStrategy =
        SpringBatchPagingItemReaderRenderingStrategy()

    @JvmStatic
    fun toParameterValues(selectStatement: SelectStatementProvider): Map<String, Any?> {
        val parameterValues = HashMap<String, Any?>()
        parameterValues[PARAMETER_KEY] = selectStatement.selectStatement
        parameterValues[RenderingStrategy.DEFAULT_PARAMETER_PREFIX] = selectStatement.parameters
        return parameterValues
    }
}
