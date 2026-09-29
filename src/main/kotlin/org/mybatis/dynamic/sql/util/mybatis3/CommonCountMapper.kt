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

import org.apache.ibatis.annotations.SelectProvider
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter

/**
 * 通用 MyBatis count 映射器。count 语句是总是返回 long 的 select 语句。
 *
 * 该映射器可以直接注入 MyBatis 配置,也可以被现有映射器扩展。
 */
interface CommonCountMapper {
    /**
     * 执行一个返回 long 的 select 语句(通常是 select(count(*)) 语句)。
     * 该映射器假定语句返回单行且单列,并且可以作为 long 获取。
     *
     * @param selectStatement select 语句
     * @return long 值
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun count(selectStatement: SelectStatementProvider): Long
}
