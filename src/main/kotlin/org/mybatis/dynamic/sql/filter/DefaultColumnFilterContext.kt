package org.mybatis.dynamic.sql.filter

import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.util.AbstractColumnMapping
import org.mybatis.dynamic.sql.util.ConstantMapping
import org.mybatis.dynamic.sql.util.NullMapping
import org.mybatis.dynamic.sql.util.StringConstantMapping
import org.mybatis.dynamic.sql.util.ValueMapping
import java.util.function.Supplier


/**
 * [ColumnFilterContext] 的默认实现。
 *
 * <p>该实现是可变累加器,但生命周期仅限一次过滤器执行;每次执行都会创建新实例,
 * 因此不会出现跨次 build 的状态残留。
 */
internal class DefaultColumnFilterContext(
    override val table: SqlTable
) : ColumnFilterContext {

    private val columnMappings: MutableList<AbstractColumnMapping> = mutableListOf()

    override fun setNull(column: SqlColumn<*>): ColumnFilterContext =
        add(NullMapping.of(column))

    override fun setConstant(column: SqlColumn<*>, constant: String): ColumnFilterContext =
        add(ConstantMapping.of(column, constant))

    override fun setStringConstant(column: SqlColumn<*>, value: String): ColumnFilterContext =
        add(StringConstantMapping.of(column, value))

    override fun <T> setValue(column: SqlColumn<T>, value: T): ColumnFilterContext =
        add(ValueMapping.of(column, {value}))

    override fun <T> setValue(column: SqlColumn<T>,value: Supplier<T>): ColumnFilterContext =
        add(ValueMapping.of(column, value))

    override fun toColumnMappings(): List<AbstractColumnMapping> = columnMappings.toList()

    private fun add(mapping: AbstractColumnMapping): ColumnFilterContext {
        columnMappings.add(mapping)
        return this
    }
}