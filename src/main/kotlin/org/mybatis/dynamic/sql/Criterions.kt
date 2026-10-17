@file:JvmName("SqlBuilder")
@file:JvmMultifileClass
package org.mybatis.dynamic.sql

import org.mybatis.dynamic.sql.dsl.HavingDSL
import org.mybatis.dynamic.sql.dsl.WhereDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.kotlin.GroupingCriteriaCollector
import org.mybatis.dynamic.sql.util.kotlin.GroupingCriteriaReceiver

fun where(): WhereDSL {
    return WhereDSL()
}

fun <T> where(column: BindableColumn<T>, condition: RenderableCondition<T>,vararg subCriteria: AndOrCriteriaGroup): WhereDSL {
    val initialCriterion = ColumnAndConditionCriterion(column,condition)
    return where(initialCriterion, *subCriteria)
}

fun where(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): WhereDSL {
    return WhereDSL(initialCriterion, listOf(*subCriteria))
}

fun where(existsPredicate: ExistsPredicate, vararg subCriteria: AndOrCriteriaGroup): WhereDSL {
    val existsCriterion = ExistsCriterion(existsPredicate)
    return where(existsCriterion, *subCriteria)
}

fun <T> having(column: BindableColumn<T>, condition: RenderableCondition<T>,vararg subCriteria: AndOrCriteriaGroup): HavingDSL {
    val initialCriterion = ColumnAndConditionCriterion(column,condition)
    return having(initialCriterion, *subCriteria)
}

fun having(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): HavingDSL {
    return HavingDSL(initialCriterion, listOf(*subCriteria))
}

fun <T> group(column: BindableColumn<T>, condition: RenderableCondition<T>, vararg subCriteria: AndOrCriteriaGroup): CriteriaGroup {
    return group(column, condition, listOf(*subCriteria))
}

fun <T> group(column: BindableColumn<T>, condition: RenderableCondition<T>,subCriteria: List<AndOrCriteriaGroup> ): CriteriaGroup {
    val initialCriterion = ColumnAndConditionCriterion(column,condition)
    return CriteriaGroup(initialCriterion,subCriteria)
}

fun group(existsPredicate: ExistsPredicate, vararg subCriteria: AndOrCriteriaGroup): CriteriaGroup {
    return group(existsPredicate, listOf(*subCriteria))
}

fun group(existsPredicate: ExistsPredicate, subCriteria: List<AndOrCriteriaGroup>): CriteriaGroup {
    val initialCriterion = ExistsCriterion(existsPredicate)
    return CriteriaGroup(initialCriterion,subCriteria)
}

fun group(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): CriteriaGroup {
    return group(initialCriterion, listOf(*subCriteria))
}

fun group(initialCriterion: SqlCriterion, subCriteria: List<AndOrCriteriaGroup>): CriteriaGroup {
    return CriteriaGroup(initialCriterion,subCriteria)
}

fun group(subCriteria: List<AndOrCriteriaGroup>): CriteriaGroup {
    return CriteriaGroup(NullCriterion(),subCriteria)
}

fun <T> not(column: BindableColumn<T>, condition: RenderableCondition<T>,vararg subCriteria: AndOrCriteriaGroup): NotCriterion {
    return not(column, condition, listOf(*subCriteria))
}

fun <T> not(column: BindableColumn<T>, condition: RenderableCondition<T>,subCriteria: List<AndOrCriteriaGroup>): NotCriterion {
    val initialCriterion = ColumnAndConditionCriterion(column,condition)
    return NotCriterion(initialCriterion,subCriteria)
}

fun not(existsPredicate: ExistsPredicate, vararg subCriteria: AndOrCriteriaGroup): NotCriterion {
    return not(existsPredicate, listOf(*subCriteria))
}

fun not(existsPredicate: ExistsPredicate, subCriteria: List<AndOrCriteriaGroup>): NotCriterion {
    val initialCriterion = ExistsCriterion(existsPredicate)
    return NotCriterion(initialCriterion,subCriteria)
}

fun not(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): NotCriterion {
    return not(initialCriterion, listOf(*subCriteria))
}

fun not(initialCriterion: SqlCriterion, subCriteria: List<AndOrCriteriaGroup>): NotCriterion {
    return NotCriterion(initialCriterion,subCriteria)
}

fun not(subCriteria: List<AndOrCriteriaGroup>): NotCriterion {
    return NotCriterion(NullCriterion(),subCriteria)
}

fun <T> or(column: BindableColumn<T>, condition: RenderableCondition<T>,vararg subCriteria: AndOrCriteriaGroup): AndOrCriteriaGroup {
    val initialCriterion = ColumnAndConditionCriterion(column,condition)
    return AndOrCriteriaGroup("or",initialCriterion,listOf(*subCriteria))
}

fun or(existsPredicate: ExistsPredicate, vararg subCriteria: AndOrCriteriaGroup): AndOrCriteriaGroup {
    return AndOrCriteriaGroup("or",ExistsCriterion(existsPredicate),listOf(*subCriteria))
}

fun or(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): AndOrCriteriaGroup {
    return AndOrCriteriaGroup("or",initialCriterion,listOf(*subCriteria))
}

fun or(subCriteria: List<AndOrCriteriaGroup>): AndOrCriteriaGroup {
    return AndOrCriteriaGroup("or",NullCriterion(),subCriteria)
}

fun or(receiver: GroupingCriteriaReceiver): AndOrCriteriaGroup{
    return with(GroupingCriteriaCollector().apply(receiver)) {
        AndOrCriteriaGroup("or",initialCriterion,subCriteria)
    }
}

fun <T> and(column: BindableColumn<T>, condition: RenderableCondition<T>,vararg subCriteria: AndOrCriteriaGroup): AndOrCriteriaGroup {
    val initialCriterion = ColumnAndConditionCriterion(column,condition)
    return AndOrCriteriaGroup("and",initialCriterion,listOf(*subCriteria))
}

fun and(existsPredicate: ExistsPredicate, vararg subCriteria: AndOrCriteriaGroup): AndOrCriteriaGroup {
    return AndOrCriteriaGroup("and",ExistsCriterion(existsPredicate),listOf(*subCriteria))
}

fun and(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): AndOrCriteriaGroup {
    return AndOrCriteriaGroup("and",initialCriterion,listOf(*subCriteria))
}

fun and(subCriteria: List<AndOrCriteriaGroup>): AndOrCriteriaGroup {
    return AndOrCriteriaGroup("and",NullCriterion(),subCriteria)
}

@JvmSynthetic
fun and(receiver: GroupingCriteriaReceiver): AndOrCriteriaGroup{
    return with(GroupingCriteriaCollector().apply(receiver)) {
        AndOrCriteriaGroup("and",initialCriterion,subCriteria)
    }
}

fun <T> on(joinColumn: BindableColumn<T>,joinCondition: RenderableCondition<T>): ColumnAndConditionCriterion<T> {
    return ColumnAndConditionCriterion(joinColumn,joinCondition)
}

fun exists(selectModelBuilder: Buildable<SelectModel>): ExistsPredicate {
    return ExistsPredicate("exists", selectModelBuilder)
}

fun notExists(selectModelBuilder: Buildable<SelectModel>): ExistsPredicate {
    return ExistsPredicate("not exists", selectModelBuilder)
}