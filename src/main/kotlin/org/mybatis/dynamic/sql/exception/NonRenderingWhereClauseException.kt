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
package org.mybatis.dynamic.sql.exception

import org.mybatis.dynamic.sql.configuration.GlobalConfiguration
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.util.Messages

/**
 * 当语句中的 where 子句无法渲染时抛出此异常。
 * 当 where 子句中所有可选条件都无法渲染时会发生这种情况 - 例如,"in" 条件指定了空列表。
 *
 * <p>默认情况下,如果 where 子句无法渲染,框架会抛出此异常。无法渲染的 where 子句可能非常危险,
 * 因为它可能导致语句影响表中的所有行 - 例如,所有行都可能被删除。
 *
 * <p>如果打算允许 where 子句不渲染,请配置语句允许这样做,或更改全局配置。
 *
 * @see GlobalConfiguration
 * @see StatementConfiguration
 */
class NonRenderingWhereClauseException : DynamicSqlException {
    constructor() : super(Messages.getString("ERROR.2")) //$NON-NLS-1$

    companion object {
        @JvmField
        @kotlin.jvm.Transient
        val serialVersionUID: Long = 6619119078542625135L
    }
}
