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

import org.apache.ibatis.annotations.InsertProvider
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter

/**
 * 用于执行各种非类型化 insert 语句(通用 insert 和 insert select)的通用映射器。
 * 该映射器适用于不期望生成键的 insert 语句。
 */
interface CommonGeneralInsertMapper {
    /**
     * 执行一个输入字段直接提供的 insert 语句。
     *
     * @param insertStatement insert 语句
     * @return 受影响的行数
     */
    @InsertProvider(type = SqlProviderAdapter::class, method = "generalInsert")
    fun generalInsert(insertStatement: GeneralInsertStatementProvider): Int

    /**
     * 执行一个输入字段由 select 语句提供的 insert 语句。
     *
     * @param insertSelectStatement insert 语句
     * @return 受影响的行数
     */
    @InsertProvider(type = SqlProviderAdapter::class, method = "insertSelect")
    fun insertSelect(insertSelectStatement: InsertSelectStatementProvider): Int
}
