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


/**
 * 字段与值的收集器,负责汇总 insert 语句的列、值短语与参数。
 */
class FieldAndValueCollector(val fieldsAndValues: List<FieldAndValueAndParameters>) {

    fun isEmpty(): Boolean {
        return fieldsAndValues.isEmpty()
    }

    fun columnsPhrase(): String {
        return fieldsAndValues.joinToString(", ", "(", ")") { it.fieldName() }
    }

    fun valuesPhrase(): String {
        return fieldsAndValues.joinToString(", ", "values (", ")") { it.valuePhrase() }
    }

    fun multiRowInsertValuesPhrase(rowCount: Int): String {
        return IntRange(0, rowCount-1).joinToString(", ", "values ", "") {
            toSingleRowOfValues(it)
        }
    }

    private fun toSingleRowOfValues(row: Int): String {
        return fieldsAndValues.joinToString(", ", "(", ")") {
            String.format(it.valuePhrase(), row)
        }
    }

    fun parameters(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        for(fieldsAndValue in fieldsAndValues) {
            map.putAll(fieldsAndValue.parameters())
        }
        return map
    }

}
