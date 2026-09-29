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
import org.mybatis.dynamic.sql.delete.DeleteModel

/**
 * delete 语句 DSL。
 */
class DeleteDSL private constructor(table: SqlTable, tableAlias: String?) :
    AbstractDeleteDSL<DeleteModel, DeleteDSL>(table, tableAlias) {

    /**
     * 警告!调用此方法可能生成删除表中所有行的 delete 语句。
     *
     * @return 模型类
     */
    override fun build(): DeleteModel {
        return buildDeleteModel()
    }

    override fun getThis(): DeleteDSL {
        return this
    }

    companion object {
        @JvmStatic
        fun deleteFrom(table: SqlTable, tableAlias: String): DeleteDSL {
            return DeleteDSL(table, tableAlias)
        }

        @JvmStatic
        fun deleteFrom(table: SqlTable): DeleteDSL {
            return DeleteDSL(table, null)
        }
    }
}
