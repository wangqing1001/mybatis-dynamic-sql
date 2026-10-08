package org.mybatis.dynamic.sql.dsl

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.configuration.StatementConfiguration
import org.mybatis.dynamic.sql.insert.GeneralInsertModel
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.ConstantMapping
import org.mybatis.dynamic.sql.util.NullMapping
import org.mybatis.dynamic.sql.util.StringConstantMapping
import org.mybatis.dynamic.sql.util.ValueMapping
import org.mybatis.dynamic.sql.util.ValueOrNullMapping
import org.mybatis.dynamic.sql.util.ValueWhenPresentMapping
import java.util.ArrayList
import java.util.Objects
import java.util.function.Supplier

/**
 * 通用 insert DSL,支持 set 子句的各种映射方式。
 */
class GeneralInsertDSL private constructor(builder: Builder) : Buildable<GeneralInsertModel> {
    private val columnMappings: MutableList<AbstractColumnMapping>
    private val table: SqlTable

    init {
        table = Objects.requireNonNull(builder.table)
        columnMappings = builder.columnMappings
    }

    fun <T> set(column: SqlColumn<T>): SetClauseFinisher<T> {
        return SetClauseFinisher(column)
    }

    override fun build(): GeneralInsertModel {
        return GeneralInsertModel(table, StatementConfiguration(), columnMappings)
    }

    companion object {
        @JvmStatic
        fun insertInto(table: SqlTable): GeneralInsertDSL {
            return Builder().withTable(table).build()
        }
    }

    inner class SetClauseFinisher<T>(private val column: SqlColumn<T>) {

        fun toNull(): GeneralInsertDSL {
            columnMappings.add(NullMapping.of(column))
            return this@GeneralInsertDSL
        }

        fun toConstant(constant: String): GeneralInsertDSL {
            columnMappings.add(ConstantMapping.of(column, constant))
            return this@GeneralInsertDSL
        }

        fun toStringConstant(constant: String): GeneralInsertDSL {
            columnMappings.add(StringConstantMapping.of(column, constant))
            return this@GeneralInsertDSL
        }

        fun toValue(value: T): GeneralInsertDSL {
            return toValue({ value })
        }

        fun toValue(valueSupplier: Supplier<T>): GeneralInsertDSL {
            columnMappings.add(ValueMapping.of(column, valueSupplier))
            return this@GeneralInsertDSL
        }

        fun toValueOrNull(value: T?): GeneralInsertDSL {
            return toValueOrNull({ value })
        }

        fun toValueOrNull(valueSupplier: Supplier<T?>): GeneralInsertDSL {
            columnMappings.add(ValueOrNullMapping.of(column, valueSupplier))
            return this@GeneralInsertDSL
        }

        fun toValueWhenPresent(value: T?): GeneralInsertDSL {
            return toValueWhenPresent({ value })
        }

        fun toValueWhenPresent(valueSupplier: Supplier<T?>): GeneralInsertDSL {
            columnMappings.add(ValueWhenPresentMapping(column, valueSupplier))
            return this@GeneralInsertDSL
        }
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        val columnMappings: MutableList<AbstractColumnMapping> = ArrayList()
        lateinit var table: SqlTable

        fun withTable(table: SqlTable): Builder {
            this.table = table
            return this
        }

        fun withColumnMappings(columnMappings: Collection<AbstractColumnMapping>): Builder {
            this.columnMappings.addAll(columnMappings)
            return this
        }

        fun build(): GeneralInsertDSL {
            return GeneralInsertDSL(this)
        }
    }
}