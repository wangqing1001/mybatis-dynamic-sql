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

import org.apache.ibatis.annotations.DeleteProvider
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter

/**
 * 通用 MyBatis delete 映射器。
 *
 * 该映射器可以直接注入 MyBatis 配置,也可以被现有映射器扩展。
 */
interface CommonDeleteMapper {
    /**
     * 执行一个 delete 语句。
     *
     * @param deleteStatement delete 语句
     * @return 受影响的行数
     */
    @DeleteProvider(type = SqlProviderAdapter::class, method = "delete")
    fun delete(deleteStatement: DeleteStatementProvider): Int
}
