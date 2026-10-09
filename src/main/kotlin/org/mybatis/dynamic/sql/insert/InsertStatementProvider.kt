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
package org.mybatis.dynamic.sql.insert

/**
 * insert 语句提供者接口。
 */
interface InsertStatementProvider<T> {


    /**
     * 返回与此 insert 语句关联的行。
     *
     * @return 与此 insert 语句关联的行
     */
    val row: T

    /**
     * 返回格式化后的 insert 语句。
     *
     * @return 格式化后的 insert 语句
     */
    val insertStatement: String
}
