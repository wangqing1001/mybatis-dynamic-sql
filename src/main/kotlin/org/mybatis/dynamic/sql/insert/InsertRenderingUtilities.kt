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
@file:JvmName("InsertRenderingUtilities")
package org.mybatis.dynamic.sql.insert

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.exception.InvalidSqlException
import org.mybatis.dynamic.sql.util.FieldAndValueCollector
import org.mybatis.dynamic.sql.util.Messages
import org.mybatis.dynamic.sql.util.spaceBefore

fun calculateInsertStatement(table: SqlTable, collector: FieldAndValueCollector): String {
    val statementStart = calculateInsertStatementStart(table)
    val columnsPhrase = collector.columnsPhrase()
    val valuesPhrase = collector.valuesPhrase()
    return statementStart + spaceBefore(columnsPhrase) + spaceBefore(valuesPhrase)
}

fun calculateInsertStatementStart(table: SqlTable): String {
    return "insert into " + table.tableName() //$NON-NLS-1$
}

fun getMappedPropertyName(column: SqlColumn<*>): String {
    return column.javaProperty()?: throw InvalidSqlException(Messages.getString("ERROR.50", column.name()))
}
