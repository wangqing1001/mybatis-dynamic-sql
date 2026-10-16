@file:JvmName("SqlBuilder")
@file:JvmMultifileClass
package org.mybatis.dynamic.sql

import org.mybatis.dynamic.sql.delete.DeleteModel
import org.mybatis.dynamic.sql.dsl.BatchInsertDSL
import org.mybatis.dynamic.sql.dsl.CountDSL
import org.mybatis.dynamic.sql.dsl.DeleteDSL
import org.mybatis.dynamic.sql.dsl.GeneralInsertDSL
import org.mybatis.dynamic.sql.dsl.InsertDSL
import org.mybatis.dynamic.sql.dsl.InsertSelectDSL
import org.mybatis.dynamic.sql.dsl.MultiRowInsertDSL
import org.mybatis.dynamic.sql.dsl.SelectDSL
import org.mybatis.dynamic.sql.dsl.UpdateDSL
import org.mybatis.dynamic.sql.insert.GeneralInsertModel
import org.mybatis.dynamic.sql.insert.InsertModel
import org.mybatis.dynamic.sql.insert.batch.BatchInsertModel
import org.mybatis.dynamic.sql.insert.batch.MultiRowInsertModel
import org.mybatis.dynamic.sql.insert.select.InsertSelectModel
import org.mybatis.dynamic.sql.select.MultiSelectDSL
import org.mybatis.dynamic.sql.select.MultiSelectModel
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.aggregate.Count
import org.mybatis.dynamic.sql.select.aggregate.CountAll
import org.mybatis.dynamic.sql.select.aggregate.CountDistinct
import org.mybatis.dynamic.sql.update.UpdateModel
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.GeneralInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.InsertSelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.KotlinBatchInsertBuilder
import org.mybatis.dynamic.sql.util.kotlin.KotlinBatchInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.KotlinCountBuilder
import org.mybatis.dynamic.sql.util.kotlin.KotlinDeleteBuilder
import org.mybatis.dynamic.sql.util.kotlin.KotlinGeneralInsertBuilder
import org.mybatis.dynamic.sql.util.kotlin.KotlinInsertBuilder
import org.mybatis.dynamic.sql.util.kotlin.KotlinInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.KotlinInsertSelectSubQueryBuilder
import org.mybatis.dynamic.sql.util.kotlin.KotlinMultiRowInsertBuilder
import org.mybatis.dynamic.sql.util.kotlin.KotlinMultiRowInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.KotlinMultiSelectBuilder
import org.mybatis.dynamic.sql.util.kotlin.KotlinSelectBuilder
import org.mybatis.dynamic.sql.util.kotlin.KotlinUpdateBuilder
import org.mybatis.dynamic.sql.util.kotlin.MultiSelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter

fun count(): CountAll {
    return CountAll()
}

fun count(column: BasicColumn): Count {
    return Count(column)
}

fun countDistinct(column: BasicColumn): CountDistinct {
    return CountDistinct.of(column)
}

@JvmOverloads
fun countDistinctFrom(column: BasicColumn,table: SqlTable,tableAlias: String? = null): CountDSL {
    if(tableAlias==null){
        return CountDSL(countDistinct(column)).from(table)
    }
    return CountDSL(countDistinct(column)).from(table,tableAlias)
}

@JvmOverloads
fun countFrom(column: BasicColumn,table: SqlTable,tableAlias: String? = null): CountDSL {
    if(tableAlias==null){
        return CountDSL(count(column)).from(table)
    }
    return CountDSL(count(column)).from(table,tableAlias)
}

@JvmOverloads
fun countFrom(table: SqlTable, tableAlias: String?=null): CountDSL {
    if(tableAlias==null){
        return CountDSL(count()).from(table)
    }
    return CountDSL(count()).from(table, tableAlias)
}

@JvmSynthetic
fun countDistinctFrom(column: BasicColumn,table: SqlTable, completer: CountCompleter): SelectModel{
    return KotlinCountBuilder(CountDSL(countDistinct(column)).from(table)).apply(completer).build()
}

@JvmSynthetic
fun countFrom(column: BasicColumn,table: SqlTable, completer: CountCompleter): SelectModel{
    return KotlinCountBuilder(CountDSL(count(column)).from(table)).apply(completer).build()
}

@JvmSynthetic
fun countFrom(table: SqlTable, completer: CountCompleter): SelectModel{
    return KotlinCountBuilder(CountDSL(count()).from(table)).apply(completer).build()
}

fun select(vararg selectList: BasicColumn): SelectDSL {
    return select(listOf(*selectList))
}

fun select(selectList: Collection<BasicColumn>): SelectDSL {
    return SelectDSL.select(selectList)
}


@JvmSynthetic
fun select(vararg columns: BasicColumn, completer: SelectCompleter): SelectModel{
    return select(listOf(*columns), completer)
}

@JvmSynthetic
fun select(columns: Collection<BasicColumn>, completer: SelectCompleter): SelectModel{
    return KotlinSelectBuilder(SelectDSL.select(columns)).apply(completer).build()
}

fun selectDistinct(vararg selectList: BasicColumn): SelectDSL {
    return selectDistinct(listOf(*selectList))
}

fun selectDistinct(selectList: Collection<BasicColumn>): SelectDSL {
    return SelectDSL.selectDistinct(selectList)
}

@JvmSynthetic
fun selectDistinct(vararg columns: BasicColumn, completer: SelectCompleter): SelectModel{
    return selectDistinct(listOf(*columns), completer)
}

@JvmSynthetic
fun selectDistinct(columns: List<BasicColumn>, completer: SelectCompleter): SelectModel{
    return KotlinSelectBuilder(SelectDSL.selectDistinct(columns)).apply(completer).build()
}

fun multiSelect(selectModelBuilder: Buildable<SelectModel>): MultiSelectDSL {
    return MultiSelectDSL(selectModelBuilder)
}

@JvmSynthetic
fun multiSelect(completer: MultiSelectCompleter): MultiSelectModel {
    return KotlinMultiSelectBuilder().apply(completer).build()
}

fun <T: Any> insert(row: T): InsertDSL.IntoGatherer<T> {
    return InsertDSL.insert(row)
}

@JvmSynthetic
fun <T: Any> insert(table: SqlTable,row: T,completer: KotlinInsertCompleter<T>): InsertModel<T>{
    return KotlinInsertBuilder(table,row).apply(completer).build()
}

fun insertInto(table: SqlTable): GeneralInsertDSL {
    return GeneralInsertDSL.insertInto(table)
}

@JvmSynthetic
fun insertInto(table: SqlTable, completer: GeneralInsertCompleter): GeneralInsertModel{
    return KotlinGeneralInsertBuilder(table).apply(completer).build()
}

fun <T: Any> insertBatch(vararg records: T): BatchInsertDSL.IntoGatherer<T> {
    return BatchInsertDSL.insert(listOf(*records))
}

fun <T: Any> insertBatch(records: Collection<T>): BatchInsertDSL.IntoGatherer<T> {
    return BatchInsertDSL.insert(records)
}

@JvmSynthetic
fun <T: Any> insertBatch(table: SqlTable,rows: Collection<T>, completer: KotlinBatchInsertCompleter<T>): BatchInsertModel<T>{
    return KotlinBatchInsertBuilder(table,rows).apply(completer).build()
}

fun <T: Any> insertMultiple(vararg records: T): MultiRowInsertDSL.IntoGatherer<T> {
    return MultiRowInsertDSL.insert(listOf(*records))
}

fun <T: Any> insertMultiple(records: Collection<T>): MultiRowInsertDSL.IntoGatherer<T> {
    return MultiRowInsertDSL.insert(records)
}

@JvmSynthetic
fun <T : Any> insertMultiple(table: SqlTable,rows: Collection<T>, completer: KotlinMultiRowInsertCompleter<T>): MultiRowInsertModel<T>{
    return KotlinMultiRowInsertBuilder(table,rows).apply(completer).build()
}

fun insertSelect(table: SqlTable): InsertSelectDSL.InsertColumnGatherer{
    return InsertSelectDSL.insertInto(table)
}

@JvmSynthetic
fun insertSelect(table: SqlTable,completer: InsertSelectCompleter): InsertSelectModel{
    return KotlinInsertSelectSubQueryBuilder(table).apply(completer).build()
}

fun update(table: SqlTable): UpdateDSL {
    return UpdateDSL.update(table)
}

fun deleteFrom(table: SqlTable): DeleteDSL {
    return DeleteDSL.deleteFrom(table)
}

fun deleteFrom(table: SqlTable, tableAlias: String): DeleteDSL {
    return DeleteDSL.deleteFrom(table, tableAlias)
}

@JvmSynthetic
fun deleteFrom(table: SqlTable, completer: DeleteCompleter): DeleteModel{
    return KotlinDeleteBuilder(DeleteDSL.deleteFrom(table)).apply(completer).build()
}

@JvmSynthetic
fun deleteFrom(table: SqlTable, tableAlias: String, completer: DeleteCompleter): DeleteModel {
    return KotlinDeleteBuilder(DeleteDSL.deleteFrom(table, tableAlias)).apply(completer).build()
}

@JvmSynthetic
fun update(table: SqlTable, completer: UpdateCompleter): UpdateModel{
    return KotlinUpdateBuilder(UpdateDSL.update(table)).apply(completer).build()
}

fun update(table: SqlTable, tableAlias: String): UpdateDSL {
    return UpdateDSL.update(table, tableAlias)
}

@JvmSynthetic
fun update(table: SqlTable, tableAlias: String, completer: UpdateCompleter): UpdateModel{
    return KotlinUpdateBuilder(UpdateDSL.update(table, tableAlias)).apply(completer).build()
}





