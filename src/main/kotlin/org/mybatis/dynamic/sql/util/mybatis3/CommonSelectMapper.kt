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
package org.mybatis.dynamic.sql.util.mybatis3

import org.apache.ibatis.annotations.SelectProvider
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter
import java.math.BigDecimal
import java.util.Optional
import java.util.function.Function

/**
 * 通用 MyBatis select 映射器。允许执行 select 语句,而无需为每个语句编写自定义
 * [org.apache.ibatis.annotations.ResultMap]。
 *
 * 该映射器包含三类方法:
 * - selectOneMappedRow 和 selectManyMappedRows 方法允许使用任意数量列的 select 语句。
 *   MyBatis 将处理行并返回值的 Map 或 Map 的 List。
 * - selectOne 和 selectMany 方法也允许使用任意数量列的 select 语句。
 *   这些方法还允许指定一个函数,将行的 Map 转换为特定对象。
 * - 其他方法用于单列结果集。有许多数据类型的函数(Integer、Long、String 等)。
 *   还有一些函数返回单个值、Optional 值或值列表。
 *
 * 该映射器可以直接注入 MyBatis 配置,也可以被现有映射器扩展。
 */
interface CommonSelectMapper {
    /**
     * 将单行作为值的 Map 选择出来。该行可以有任意数量的列。
     * Map 键将是数据库返回的列名(如果在 select 语句中指定了别名,键将被别名化)。
     * Map 条目的数据类型由 JDBC 驱动程序确定。MyBatis 将调用 ResultSet.getObject() 来获取
     * ResultSet 中的值。请参阅 JDBC 驱动程序文档以了解特定数据库的类型映射。
     *
     * @param selectStatement select 语句
     * @return 包含行值的 Map
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectOneMappedRow(selectStatement: SelectStatementProvider): Map<String, Any>?

    /**
     * 选择一行值,然后将这些值转换为自定义类型。这与 Spring JDBC 模板处理结果集的方法类似。
     * 在这种情况下,MyBatis 将首先将行值提取到 Map 中,然后行映射器可以从 Map 中获取值并用它们
     * 构造自定义对象。
     *
     * 关于 MyBatis 如何构造值的 Map,请参见 [selectOneMappedRow]。
     *
     * @param selectStatement select 语句
     * @param rowMapper 将行的 Map 转换为所需数据类型的函数
     * @param <R> 转换对象的数据类型
     * @return 转换后的对象
     */
    fun <R> selectOne(
        selectStatement: SelectStatementProvider,
        rowMapper: Function<Map<String, Any>, R>
    ): R? {
        val result = selectOneMappedRow(selectStatement)
        return if (result == null) null else rowMapper.apply(result)
    }

    /**
     * 选择任意数量的行,并返回包含行值的 Map 的 List(每行一个 Map)。
     * 这些行可以有任意数量的列。
     * Map 键将是数据库返回的列名(如果在 select 语句中指定了别名,键将被别名化)。
     * Map 条目的数据类型由 JDBC 驱动程序确定。MyBatis 将调用 ResultSet.getObject() 来获取
     * ResultSet 中的值。请参阅 JDBC 驱动程序文档以了解特定数据库的类型映射。
     *
     * @param selectStatement select 语句
     * @return 包含行值的 Map 的 List
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectManyMappedRows(selectStatement: SelectStatementProvider): List<Map<String, Any>>

    /**
     * 选择任意数量的行,然后将这些值转换为自定义类型。这与 Spring JDBC 模板处理结果集的方法类似。
     * 在这种情况下,MyBatis 将首先将行值提取到 List of Map 中,然后行映射器可以从 Map 中获取值
     * 并为每一行构造自定义对象。
     *
     * @param selectStatement select 语句
     * @param rowMapper 将行的 Map 转换为所需数据类型的函数
     * @param <R> 转换对象的数据类型
     * @return 转换后的对象的 List
     */
    fun <R> selectMany(
        selectStatement: SelectStatementProvider,
        rowMapper: Function<Map<String, Any>, R>
    ): List<R> {
        return selectManyMappedRows(selectStatement).stream()
            .map(rowMapper)
            .toList()
    }

    /**
     * 从结果集中获取单个 [java.math.BigDecimal]。结果集必须只有一列且只有一行或零行。
     * 该列必须可以通过 ResultSet.getBigDecimal() 方法获取。
     *
     * @param selectStatement select 语句
     * @return 提取的值。如果返回零行或返回的列为 null,则可能为 null
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectOneBigDecimal(selectStatement: SelectStatementProvider): BigDecimal?

    /**
     * 从结果集中获取单个 [java.math.BigDecimal]。结果集必须只有一列且只有一行或零行。
     * 该列必须可以通过 ResultSet.getBigDecimal() 方法获取。
     *
     * @param selectStatement select 语句
     * @return 提取的值。如果返回零行或返回的列为 null,则 Optional 为空
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectOptionalBigDecimal(selectStatement: SelectStatementProvider): Optional<BigDecimal>

    /**
     * 从结果集中获取 [java.math.BigDecimal] 的 List。结果集必须只有一列,但可以有任意数量的行。
     * 该列必须可以通过 ResultSet.getBigDecimal() 方法获取。
     *
     * @param selectStatement select 语句
     * @return 提取值的列表。如果结果集中的某列为 null,则任何值都可能为 null
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectManyBigDecimals(selectStatement: SelectStatementProvider): List<BigDecimal>

    /**
     * 从结果集中获取单个 [java.lang.Double]。结果集必须只有一列且只有一行或零行。
     * 该列必须可以通过 ResultSet.getDouble() 方法获取。
     *
     * @param selectStatement select 语句
     * @return 提取的值。如果返回零行或返回的列为 null,则可能为 null
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectOneDouble(selectStatement: SelectStatementProvider): Double?

    /**
     * 从结果集中获取单个 [java.lang.Double]。结果集必须只有一列且只有一行或零行。
     * 该列必须可以通过 ResultSet.getDouble() 方法获取。
     *
     * @param selectStatement select 语句
     * @return 提取的值。如果返回零行或返回的列为 null,则 Optional 为空
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectOptionalDouble(selectStatement: SelectStatementProvider): Optional<Double>

    /**
     * 从结果集中获取 [java.lang.Double] 的 List。结果集必须只有一列,但可以有任意数量的行。
     * 该列必须可以通过 ResultSet.getDouble() 方法获取。
     *
     * @param selectStatement select 语句
     * @return 提取值的列表。如果结果集中的某列为 null,则任何值都可能为 null
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectManyDoubles(selectStatement: SelectStatementProvider): List<Double>

    /**
     * 从结果集中获取单个 [java.lang.Integer]。结果集必须只有一列且只有一行或零行。
     * 该列必须可以通过 ResultSet.getInt() 方法获取。
     *
     * @param selectStatement select 语句
     * @return 提取的值。如果返回零行或返回的列为 null,则可能为 null
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectOneInteger(selectStatement: SelectStatementProvider): Int?

    /**
     * 从结果集中获取单个 [java.lang.Integer]。结果集必须只有一列且只有一行或零行。
     * 该列必须可以通过 ResultSet.getInt() 方法获取。
     *
     * @param selectStatement select 语句
     * @return 提取的值。如果返回零行或返回的列为 null,则 Optional 为空
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectOptionalInteger(selectStatement: SelectStatementProvider): Optional<Int>

    /**
     * 从结果集中获取 [java.lang.Integer] 的 List。结果集必须只有一列,但可以有任意数量的行。
     * 该列必须可以通过 ResultSet.getInt() 方法获取。
     *
     * @param selectStatement select 语句
     * @return 提取值的列表。如果结果集中的某列为 null,则任何值都可能为 null
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectManyIntegers(selectStatement: SelectStatementProvider): List<Int>

    /**
     * 从结果集中获取单个 [java.lang.Long]。结果集必须只有一列且只有一行或零行。
     * 该列必须可以通过 ResultSet.getLong() 方法获取。
     *
     * @param selectStatement select 语句
     * @return 提取的值。如果返回零行或返回的列为 null,则可能为 null
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectOneLong(selectStatement: SelectStatementProvider): Long?

    /**
     * 从结果集中获取单个 [java.lang.Long]。结果集必须只有一列且只有一行或零行。
     * 该列必须可以通过 ResultSet.getLong() 方法获取。
     *
     * @param selectStatement select 语句
     * @return 提取的值。如果返回零行或返回的列为 null,则 Optional 为空
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectOptionalLong(selectStatement: SelectStatementProvider): Optional<Long>

    /**
     * 从结果集中获取 [java.lang.Long] 的 List。结果集必须只有一列,但可以有任意数量的行。
     * 该列必须可以通过 ResultSet.getLong() 方法获取。
     *
     * @param selectStatement select 语句
     * @return 提取值的列表。如果结果集中的某列为 null,则任何值都可能为 null
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectManyLongs(selectStatement: SelectStatementProvider): List<Long>

    /**
     * 从结果集中获取单个 [java.lang.String]。结果集必须只有一列且只有一行或零行。
     * 该列必须可以通过 ResultSet.getString() 方法获取。
     *
     * @param selectStatement select 语句
     * @return 提取的值。如果返回零行或返回的列为 null,则可能为 null
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectOneString(selectStatement: SelectStatementProvider): String?

    /**
     * 从结果集中获取单个 [java.lang.String]。结果集必须只有一列且只有一行或零行。
     * 该列必须可以通过 ResultSet.getString() 方法获取。
     *
     * @param selectStatement select 语句
     * @return 提取的值。如果返回零行或返回的列为 null,则 Optional 为空
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectOptionalString(selectStatement: SelectStatementProvider): Optional<String>

    /**
     * 从结果集中获取 [java.lang.String] 的 List。结果集必须只有一列,但可以有任意数量的行。
     * 该列必须可以通过 ResultSet.getString() 方法获取。
     *
     * @param selectStatement select 语句
     * @return 提取值的列表。如果结果集中的某列为 null,则任何值都可能为 null
     */
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    fun selectManyStrings(selectStatement: SelectStatementProvider): List<String>
}
