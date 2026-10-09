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
package org.mybatis.dynamic.sql.select

import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.TableExpressionVisitor

/**
 * 子查询表表达式。包装一个 SelectModel 并可选指定别名。
 */
class SubQuery @JvmOverloads constructor(
    private val selectModel: SelectModel,
    private val alias: String? = null
) : TableExpression {

    fun selectModel(): SelectModel {
        return selectModel
    }

    fun alias(): String? {
        return alias
    }

    override val isSubQuery: Boolean = true

    override fun <R> accept(visitor: TableExpressionVisitor<R>): R {
        return visitor.visit(this)
    }

}
