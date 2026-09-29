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
package org.mybatis.dynamic.sql.insert.render

import java.util.ArrayList
import java.util.HashMap
import java.util.function.BiConsumer
import java.util.function.Supplier
import java.util.stream.Collector
import java.util.stream.Collectors
import java.util.stream.IntStream

/**
 * 字段与值的收集器,负责汇总 insert 语句的列、值短语与参数。
 */
class FieldAndValueCollector {
    val fieldsAndValues: MutableList<FieldAndValueAndParameters> = ArrayList()

    fun add(fieldAndValueAndParameters: FieldAndValueAndParameters) {
        fieldsAndValues.add(fieldAndValueAndParameters)
    }

    fun merge(other: FieldAndValueCollector): FieldAndValueCollector {
        fieldsAndValues.addAll(other.fieldsAndValues)
        return this
    }

    fun isEmpty(): Boolean {
        return fieldsAndValues.isEmpty()
    }

    fun columnsPhrase(): String {
        return fieldsAndValues.stream()
            .map { fv: FieldAndValueAndParameters -> fv.fieldName() }
            .collect(Collectors.joining(", ", "(", ")")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    fun valuesPhrase(): String {
        return fieldsAndValues.stream()
            .map { fv: FieldAndValueAndParameters -> fv.valuePhrase() }
            .collect(Collectors.joining(", ", "values (", ")")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    fun multiRowInsertValuesPhrase(rowCount: Int): String {
        return IntStream.range(0, rowCount)
            .mapToObj { row: Int -> toSingleRowOfValues(row) }
            .collect(Collectors.joining(", ", "values ", "")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private fun toSingleRowOfValues(row: Int): String {
        return fieldsAndValues.stream()
            .map { fv: FieldAndValueAndParameters -> fv.valuePhrase() }
            .map { s: String -> String.format(s, row) }
            .collect(Collectors.joining(", ", "(", ")")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    fun parameters(): Map<String, Any?> {
        return fieldsAndValues.stream()
            .map { fv: FieldAndValueAndParameters -> fv.parameters() }
            .collect(Supplier{mutableMapOf()}, BiConsumer{a,b-> a.putAll(b) }, BiConsumer{a,b-> a.putAll(b) })
    }

    companion object {
        @JvmStatic
        fun collect(): Collector<FieldAndValueAndParameters, FieldAndValueCollector, FieldAndValueCollector> {
            return Collector.of(
                { FieldAndValueCollector() },
                { c: FieldAndValueCollector, fv: FieldAndValueAndParameters -> c.add(fv) },
                { c1: FieldAndValueCollector, c2: FieldAndValueCollector -> c1.merge(c2) }
            )
        }
    }
}
