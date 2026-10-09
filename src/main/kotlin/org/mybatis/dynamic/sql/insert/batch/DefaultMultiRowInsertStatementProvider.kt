package org.mybatis.dynamic.sql.insert.batch

/**
 * 默认多行 insert 语句提供者。
 */
class DefaultMultiRowInsertStatementProvider<T>(
    override val insertStatement: String,
    override val records: List<T>,
) : MultiRowInsertStatementProvider<T>