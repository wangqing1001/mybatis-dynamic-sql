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
package org.mybatis.dynamic.sql.util.mybatis3

import org.apache.ibatis.annotations.Flush
import org.apache.ibatis.annotations.InsertProvider
import org.apache.ibatis.executor.BatchResult
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter

/**
 * 用于执行各种类型 insert 语句的通用映射器。
 * 该映射器适用于不期望生成键的 insert 语句。
 *
 * @param <T> 与该映射器关联的行类型
 */
interface CommonInsertMapper<T> : CommonGeneralInsertMapper {
    /**
     * 执行一个输入字段映射到 POJO 中值的 insert 语句。
     *
     * @param insertStatement insert 语句
     * @return 受影响的行数
     */
    @InsertProvider(type = SqlProviderAdapter::class, method = "insert")
    fun insert(insertStatement: InsertStatementProvider<T>): Int

    /**
     * 执行一个插入多行的 insert 语句。行值由 POJO 列表中的值映射提供。
     *
     * @param insertStatement insert 语句
     * @return 受影响的行数
     */
    @InsertProvider(type = SqlProviderAdapter::class, method = "insertMultiple")
    fun insertMultiple(insertStatement: MultiRowInsertStatementProvider<T>): Int

    /**
     * 刷新批量 insert 语句并返回当前批量的详细信息。
     * 当无法直接访问 [org.apache.ibatis.session.SqlSession] 时非常有用。
     *
     * @return 当前批量的详细信息,包括更新计数等
     */
    @Flush
    fun flush(): List<BatchResult>
}
