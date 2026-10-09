package org.mybatis.dynamic.sql.insert.batch

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.util.AbstractColumnMapping

/**
 * 多行 insert 模型的抽象基类,持有表、记录与列映射。
 */
abstract class AbstractMultiRowInsertModel<T> protected constructor(
    private val table: SqlTable,
    private val records: List<T>,
    private val columnMappings: List<AbstractColumnMapping> = emptyList()
) {

    fun columnMappings(): List<AbstractColumnMapping> {
        return columnMappings
    }

    fun records(): List<T> {
        return records
    }

    fun table(): SqlTable {
        return table
    }

    fun recordCount(): Int {
        return records.size
    }

    abstract class AbstractBuilder<T, S : AbstractBuilder<T, S>> {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        lateinit var table: SqlTable
        val records: MutableList<T> = mutableListOf()
        val columnMappings: MutableList<AbstractColumnMapping> = mutableListOf()

        fun withTable(table: SqlTable): S {
            this.table = table
            return getThis()
        }

        fun withRecords(records: Collection<T>): S {
            this.records.addAll(records)
            return getThis()
        }

        fun withColumnMappings(columnMappings: List<AbstractColumnMapping>): S {
            this.columnMappings.addAll(columnMappings)
            return getThis()
        }

        protected abstract fun getThis(): S
    }
}