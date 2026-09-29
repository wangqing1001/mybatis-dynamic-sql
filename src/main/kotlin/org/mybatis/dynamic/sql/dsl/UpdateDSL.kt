/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.dsl

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.update.UpdateModel

/**
 * update 语句 DSL。
 */
class UpdateDSL private constructor(table: SqlTable, tableAlias: String?) :
    AbstractUpdateDSL<UpdateModel, UpdateDSL>(table, tableAlias) {

    /**
     * 警告!调用此方法可能生成更新表中所有行的 update 语句。
     *
     * @return update 模型
     */
    override fun build(): UpdateModel {
        return buildUpdateModel()
    }

    override fun getThis(): UpdateDSL {
        return this
    }

    companion object {
        @JvmStatic
        fun update(table: SqlTable, tableAlias: String): UpdateDSL {
            return UpdateDSL(table, tableAlias)
        }

        @JvmStatic
        fun update(table: SqlTable): UpdateDSL {
            return UpdateDSL(table, null)
        }
    }
}
