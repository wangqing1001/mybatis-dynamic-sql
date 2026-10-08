/*
 *    Copyright 2016-2026 the original author or authors.
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

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.delete.DeleteStatementProvider
import org.mybatis.dynamic.sql.dsl.CountDSL
import org.mybatis.dynamic.sql.dsl.CountDSLCompleter
import org.mybatis.dynamic.sql.dsl.DeleteDSL
import org.mybatis.dynamic.sql.dsl.DeleteDSLCompleter
import org.mybatis.dynamic.sql.dsl.SelectDSL
import org.mybatis.dynamic.sql.dsl.SelectDSLCompleter
import org.mybatis.dynamic.sql.dsl.UpdateDSL
import org.mybatis.dynamic.sql.dsl.UpdateDSLCompleter
import org.mybatis.dynamic.sql.dsl.GeneralInsertDSL
import org.mybatis.dynamic.sql.dsl.InsertDSL
import org.mybatis.dynamic.sql.dsl.MultiRowInsertDSL
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.render.RenderingStrategies
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.update.UpdateStatementProvider
import java.util.function.Function
import java.util.function.ToIntBiFunction
import java.util.function.ToIntFunction
import java.util.function.ToLongFunction
import java.util.function.UnaryOperator

/**
 * 用于构建 MyBatis3 映射器的工具函数。
 */
object MyBatis3Utils {
    @JvmStatic
    fun count(
        mapper: ToLongFunction<SelectStatementProvider>,
        column: BasicColumn,
        table: SqlTable,
        completer: CountDSLCompleter
    ): Long {
        return mapper.applyAsLong(count(column, table, completer))
    }

    @JvmStatic
    fun count(column: BasicColumn, table: SqlTable, completer: CountDSLCompleter): SelectStatementProvider {
        return countFrom(CountDSL.count(column).from(table), completer)
    }

    @JvmStatic
    fun countDistinct(
        mapper: ToLongFunction<SelectStatementProvider>,
        column: BasicColumn,
        table: SqlTable,
        completer: CountDSLCompleter
    ): Long {
        return mapper.applyAsLong(countDistinct(column, table, completer))
    }

    @JvmStatic
    fun countDistinct(column: BasicColumn, table: SqlTable, completer: CountDSLCompleter): SelectStatementProvider {
        return countFrom(CountDSL.countDistinct(column).from(table), completer)
    }

    @JvmStatic
    fun countFrom(table: SqlTable, completer: CountDSLCompleter): SelectStatementProvider {
        return countFrom(CountDSL.countFrom(table), completer)
    }

    @JvmStatic
    fun countFrom(
        mapper: ToLongFunction<SelectStatementProvider>,
        table: SqlTable,
        completer: CountDSLCompleter
    ): Long {
        return mapper.applyAsLong(countFrom(table, completer))
    }

    @JvmStatic
    fun countFrom(start: CountDSL, completer: CountDSLCompleter): SelectStatementProvider {
        return completer.apply(start)
            .build()
            .render(RenderingStrategies.MYBATIS3)
    }

    @JvmStatic
    fun countFrom(
        mapper: ToLongFunction<SelectStatementProvider>,
        start: CountDSL,
        completer: CountDSLCompleter
    ): Long {
        return mapper.applyAsLong(countFrom(start, completer))
    }

    @JvmStatic
    fun deleteFrom(table: SqlTable, completer: DeleteDSLCompleter): DeleteStatementProvider {
        return completer.apply(DeleteDSL.deleteFrom(table))
            .build()
            .render(RenderingStrategies.MYBATIS3)
    }

    @JvmStatic
    fun deleteFrom(
        mapper: ToIntFunction<DeleteStatementProvider>,
        table: SqlTable,
        completer: DeleteDSLCompleter
    ): Int {
        return mapper.applyAsInt(deleteFrom(table, completer))
    }

    @JvmStatic
    fun <R : Any> insert(row: R, table: SqlTable, completer: UnaryOperator<InsertDSL<R>>): InsertStatementProvider<R> {
        return completer.apply(SqlBuilder.insert(row).into(table))
            .build()
            .render(RenderingStrategies.MYBATIS3)
    }

    @JvmStatic
    fun <R : Any> insert(
        mapper: ToIntFunction<InsertStatementProvider<R>>,
        row: R,
        table: SqlTable,
        completer: UnaryOperator<InsertDSL<R>>
    ): Int {
        return mapper.applyAsInt(insert(row, table, completer))
    }

    @JvmStatic
    fun generalInsert(table: SqlTable, completer: UnaryOperator<GeneralInsertDSL>): GeneralInsertStatementProvider {
        return completer.apply(GeneralInsertDSL.insertInto(table))
            .build()
            .render(RenderingStrategies.MYBATIS3)
    }

    @JvmStatic
    fun generalInsert(
        mapper: ToIntFunction<GeneralInsertStatementProvider>,
        table: SqlTable,
        completer: UnaryOperator<GeneralInsertDSL>
    ): Int {
        return mapper.applyAsInt(generalInsert(table, completer))
    }

    @JvmStatic
    fun <R> insertMultiple(
        records: Collection<R>,
        table: SqlTable,
        completer: UnaryOperator<MultiRowInsertDSL<R>>
    ): MultiRowInsertStatementProvider<R> {
        return completer.apply(SqlBuilder.insertMultiple(records).into(table))
            .build()
            .render(RenderingStrategies.MYBATIS3)
    }

    @JvmStatic
    fun <R: Any> insertMultiple(
        mapper: ToIntFunction<MultiRowInsertStatementProvider<R>>,
        records: Collection<R>,
        table: SqlTable,
        completer: UnaryOperator<MultiRowInsertDSL<R>>
    ): Int {
        return mapper.applyAsInt(insertMultiple(records, table, completer))
    }

    @JvmStatic
    fun <R: Any> insertMultipleWithGeneratedKeys(
        mapper: ToIntBiFunction<String, List<R>>,
        records: Collection<R>,
        table: SqlTable,
        completer: UnaryOperator<MultiRowInsertDSL<R>>
    ): Int {
        val provider = insertMultiple(records, table, completer)
        return mapper.applyAsInt(provider.insertStatement, provider.records)
    }

    @JvmStatic
    fun select(
        selectList: Array<BasicColumn>,
        table: SqlTable,
        completer: SelectDSLCompleter
    ): SelectStatementProvider {
        return select(SelectDSL.select(*selectList).from(table), completer)
    }

    @JvmStatic
    fun select(start: SelectDSL, completer: SelectDSLCompleter): SelectStatementProvider {
        return completer.apply(start)
            .build()
            .render(RenderingStrategies.MYBATIS3)
    }

    @JvmStatic
    fun selectDistinct(
        selectList: Array<BasicColumn>,
        table: SqlTable,
        completer: SelectDSLCompleter
    ): SelectStatementProvider {
        return select(SelectDSL.selectDistinct(*selectList).from(table), completer)
    }

    @JvmStatic
    fun <R> selectDistinct(
        mapper: Function<SelectStatementProvider, List<R>>,
        selectList: Array<BasicColumn>,
        table: SqlTable,
        completer: SelectDSLCompleter
    ): List<R> {
        return mapper.apply(selectDistinct(selectList, table, completer))
    }

    @JvmStatic
    fun <R> selectList(
        mapper: Function<SelectStatementProvider, List<R>>,
        selectList: Array<BasicColumn>,
        table: SqlTable,
        completer: SelectDSLCompleter
    ): List<R> {
        return mapper.apply(select(selectList, table, completer))
    }

    @JvmStatic
    fun <R> selectList(
        mapper: Function<SelectStatementProvider, List<R>>,
        start: SelectDSL,
        completer: SelectDSLCompleter
    ): List<R> {
        return mapper.apply(select(start, completer))
    }

    @JvmStatic
    fun <R> selectOne(
        mapper: Function<SelectStatementProvider, R>,
        selectList: Array<BasicColumn>,
        table: SqlTable,
        completer: SelectDSLCompleter
    ): R {
        return mapper.apply(select(selectList, table, completer))
    }

    @JvmStatic
    fun <R> selectOne(
        mapper: Function<SelectStatementProvider, R>,
        start: SelectDSL,
        completer: SelectDSLCompleter
    ): R {
        return mapper.apply(select(start, completer))
    }

    @JvmStatic
    fun update(table: SqlTable, completer: UpdateDSLCompleter): UpdateStatementProvider {
        return completer.apply(UpdateDSL.update(table))
            .build()
            .render(RenderingStrategies.MYBATIS3)
    }

    @JvmStatic
    fun update(
        mapper: ToIntFunction<UpdateStatementProvider>,
        table: SqlTable,
        completer: UpdateDSLCompleter
    ): Int {
        return mapper.applyAsInt(update(table, completer))
    }
}
