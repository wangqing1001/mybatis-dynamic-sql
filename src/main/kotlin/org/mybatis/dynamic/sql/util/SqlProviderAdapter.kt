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
package org.mybatis.dynamic.sql.util

import org.mybatis.dynamic.sql.delete.DeleteStatementProvider
import org.mybatis.dynamic.sql.insert.GeneralInsertStatementProvider
import org.mybatis.dynamic.sql.insert.select.InsertSelectStatementProvider
import org.mybatis.dynamic.sql.insert.InsertStatementProvider
import org.mybatis.dynamic.sql.insert.batch.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.select.SelectStatementProvider
import org.mybatis.dynamic.sql.update.UpdateStatementProvider

/**
 * 用于 MyBatis SQL provider 注解的适配器。
 *
 * @author Jeff Butler
 */
class SqlProviderAdapter {

    fun delete(deleteStatement: DeleteStatementProvider): String {
        return deleteStatement.deleteStatement
    }

    fun generalInsert(insertStatement: GeneralInsertStatementProvider): String {
        return insertStatement.insertStatement
    }

    fun insert(insertStatement: InsertStatementProvider<*>): String {
        return insertStatement.insertStatement
    }

    fun insertMultiple(insertStatement: MultiRowInsertStatementProvider<*>): String {
        return insertStatement.insertStatement
    }

    /**
     * 此适配器方法用于 MyBatis 的 &#064;InsertProvider 注解,当执行 insert 语句时预期有生成值时。
     * 使用此适配器方法的典型方法签名如下:
     *
     * <pre>
     * public interface FooMapper {
     *     &#064;InsertProvider(type=SqlProviderAdapter.class, method="insertMultipleWithGeneratedKeys")
     *     &#064;Options(useGeneratedKeys=true, keyProperty="records.id")
     *     int insertMultiple(String insertStatement, &#064;Param("records") List&lt;Foo&gt; records)
     * }
     * </pre>
     *
     * @param parameterMap 当 insert 方法中有多个参数时,MyBatis 自动创建参数映射。
     *
     * @return 参数映射中包含的 SQL 语句。假定它是参数映射中唯一一个类型为 String 的条目。
     */
    fun insertMultipleWithGeneratedKeys(parameterMap: Map<String, Any>): String {
        val entries = parameterMap.entries.stream()
            .filter { e: Map.Entry<String, Any> -> e.key.startsWith("param") } //$NON-NLS-1$
            .map { e: Map.Entry<String, Any> -> e.value }
            .filter { value: Any -> value is String }
            .map { value: Any -> value as String }
            .toList()

        return if (entries.size == 1) {
            entries[0]
        } else {
            throw IllegalArgumentException(Messages.getString("ERROR.30")) //$NON-NLS-1$
        }
    }

    fun insertSelect(insertStatement: InsertSelectStatementProvider): String {
        return insertStatement.insertStatement
    }

    fun select(selectStatement: SelectStatementProvider): String {
        return selectStatement.selectStatement
    }

    fun update(updateStatement: UpdateStatementProvider): String {
        return updateStatement.updateStatement
    }
}
