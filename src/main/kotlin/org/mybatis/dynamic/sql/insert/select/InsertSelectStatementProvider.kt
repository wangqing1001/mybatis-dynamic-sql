package org.mybatis.dynamic.sql.insert.select

/**
 * insert-select 语句提供者接口。
 */
interface InsertSelectStatementProvider {

    val parameters: Map<String, Any?>

    val insertStatement: String

}