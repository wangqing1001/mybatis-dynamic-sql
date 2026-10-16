@file:JvmName("SqlBuilder")
@file:JvmMultifileClass
package org.mybatis.dynamic.sql

import org.mybatis.dynamic.sql.select.ColumnSortSpecification
import org.mybatis.dynamic.sql.select.SimpleSortSpecification

fun sortColumn(name: String) = SimpleSortSpecification(name)

fun sortColumn(tableAlias: String, column: SqlColumn<*>) = ColumnSortSpecification(tableAlias, column)