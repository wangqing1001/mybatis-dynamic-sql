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
package org.mybatis.dynamic.sql.configuration

import org.mybatis.dynamic.sql.exception.NonRenderingWhereClauseException

/**
 * 此类可用于更改框架的某些行为。每个可配置的语句都包含此类的一个唯一实例,
 * 因此此处的更改只会影响单个语句。如果打算更改所有语句的行为,请使用 [GlobalConfiguration]。
 * 每个语句中此类的初始值都从 [GlobalConfiguration] 设置。
 *
 * <dl>
 *     <dt>nonRenderingWhereClauseAllowed</dt>
 *     <dd>如果为 false(默认值),当语句中指定了 where 子句但由于所有可选条件都不渲染
 *         而导致 where 子句无法渲染时,框架将抛出 [NonRenderingWhereClauseException]。
 *         例如,如果 "in" 条件指定了空值列表。如果 where 子句中没有指定条件,框架假定
 *         未打算使用 where 子句,不会抛出异常。
 *     </dd>
 * </dl>
 *
 * @see GlobalConfiguration
 */
class StatementConfiguration {
    private var nonRenderingWhereClauseAllowed =  GlobalContext.getConfiguration().nonRenderingWhereClauseAllowed()

    fun nonRenderingWhereClauseAllowed(): Boolean {
        return nonRenderingWhereClauseAllowed
    }

    fun nonRenderingWhereClauseAllowed(nonRenderingWhereClauseAllowed: Boolean): StatementConfiguration {
        this.nonRenderingWhereClauseAllowed = nonRenderingWhereClauseAllowed
        return this
    }
}
