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
package org.mybatis.dynamic.sql.select.aggregate

import org.mybatis.dynamic.sql.BindableColumn

/**
 * Count 函数的实现与其他聚合函数不同。这主要是为了保持向后兼容。
 * Count 函数被配置为 Long 类型的 BindableColumn,因为假定 count 函数总是返回数字。
 */
abstract class AbstractCount  : BindableColumn<Long> {

    private val alias: String?

    protected constructor(){
        this.alias = null
    }

    protected constructor(alias: String?){
        this.alias = alias
    }

    override fun alias(): String? {
        return alias
    }
}
