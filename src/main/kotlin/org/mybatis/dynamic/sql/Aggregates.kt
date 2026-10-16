@file:JvmName("SqlBuilder")
@file:JvmMultifileClass
package org.mybatis.dynamic.sql

import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.aggregate.Avg
import org.mybatis.dynamic.sql.select.aggregate.Max
import org.mybatis.dynamic.sql.select.aggregate.Min
import org.mybatis.dynamic.sql.select.aggregate.Sum
import org.mybatis.dynamic.sql.select.caseexpression.SearchedCaseDSL
import org.mybatis.dynamic.sql.select.caseexpression.SearchedCaseDSL.Companion.searchedCase
import org.mybatis.dynamic.sql.select.caseexpression.SearchedCaseModel
import org.mybatis.dynamic.sql.select.caseexpression.SimpleCaseDSL
import org.mybatis.dynamic.sql.select.caseexpression.SimpleCaseDSL.Companion.simpleCase
import org.mybatis.dynamic.sql.select.caseexpression.SimpleCaseModel
import org.mybatis.dynamic.sql.select.function.Add
import org.mybatis.dynamic.sql.select.function.Cast
import org.mybatis.dynamic.sql.select.function.Concat
import org.mybatis.dynamic.sql.select.function.Concatenate
import org.mybatis.dynamic.sql.select.function.Divide
import org.mybatis.dynamic.sql.select.function.Lower
import org.mybatis.dynamic.sql.select.function.Multiply
import org.mybatis.dynamic.sql.select.function.OperatorFunction
import org.mybatis.dynamic.sql.select.function.Substring
import org.mybatis.dynamic.sql.select.function.Subtract
import org.mybatis.dynamic.sql.select.function.Upper
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.kotlin.KotlinSubQueryBuilder
import org.mybatis.dynamic.sql.util.kotlin.elements.CastDSL
import org.mybatis.dynamic.sql.util.kotlin.elements.KSearchedCaseDSL
import org.mybatis.dynamic.sql.util.kotlin.elements.KSimpleCaseDSL
import org.mybatis.dynamic.sql.util.invalidIfNull

fun subQuery(subQuery: Buildable<SelectModel>) = SubQueryColumn.of(subQuery.build())

@JvmSynthetic
fun subQuery(subQuery: KotlinSubQueryBuilder.() -> Unit) = SubQueryColumn.of(KotlinSubQueryBuilder().apply(subQuery).build())

fun <T: Any> max(column: BindableColumn<T>) = Max.of(column)

fun <T: Any> min(column: BindableColumn<T>) = Min.of(column)

fun <T: Any> avg(column: BindableColumn<T>) = Avg.of(column)

fun <T: Any> sum(column: BindableColumn<T>) = Sum.of(column)

fun sum(column: BasicColumn) = Sum.of(column)

fun <T: Any> sum(column: BindableColumn<T>, condition: RenderableCondition<T>) = Sum.of(column, condition)

fun <T: Any> constant(constant: String) = Constant.of<T>(constant)

fun stringConstant(constant: String) = StringConstant.of(constant)

fun <T: Any> value(value: T) = BoundValue.of(value)

fun <T: Any> add(firstColumn: BindableColumn<T>,secondColumn: BasicColumn,vararg subsequentColumns: BasicColumn): Add<T>{
    return Add.of(firstColumn, secondColumn,listOf(*subsequentColumns))
}

fun <T: Any> divide(firstColumn: BindableColumn<T>,secondColumn: BasicColumn,vararg subsequentColumns: BasicColumn): Divide<T>{
    return Divide.of(firstColumn, secondColumn,listOf(*subsequentColumns))
}

fun <T: Any> multiply(firstColumn: BindableColumn<T>,secondColumn: BasicColumn,vararg subsequentColumns: BasicColumn): Multiply<T>{
    return Multiply.of(firstColumn, secondColumn,listOf(*subsequentColumns))
}

fun <T: Any> subtract(firstColumn: BindableColumn<T>,secondColumn: BasicColumn,vararg subsequentColumns: BasicColumn): Subtract<T>{
    return Subtract.of(firstColumn, secondColumn,listOf(*subsequentColumns))
}

fun <T : Any> concat(firstColumn: BindableColumn<T>,vararg subsequentColumns: BasicColumn): Concat<T>{
    return Concat.of(firstColumn, listOf(*subsequentColumns))
}

fun <T : Any> concatenate(firstColumn: BindableColumn<T>,secondColumn: BasicColumn,vararg subsequentColumns: BasicColumn): Concatenate<T>{
    return Concatenate(firstColumn, secondColumn, listOf(*subsequentColumns))
}

fun <T : Any> applyOperator(operator: String,firstColumn: BindableColumn<T>,secondColumn: BasicColumn,vararg subsequentColumns: BasicColumn): OperatorFunction<T>{
    return OperatorFunction(operator, firstColumn, secondColumn, listOf(*subsequentColumns))
}

fun <T : Any> lower(column: BindableColumn<T>) = Lower.of(column)

fun <T : Any> substring(column: BindableColumn<T>,offset: Int,length: Int) = Substring.of(column, offset, length)

fun <T : Any> upper(column: BindableColumn<T>) = Upper.of(column)

fun cast(value: String): CastFinisher {
    return cast(stringConstant(value))
}

fun cast(value: Double): CastFinisher {
    return cast(constant<Any>(value.toString()))
}

fun cast(column: BasicColumn): CastFinisher {
    return CastFinisher(column)
}

@JvmSynthetic
fun cast(receiver: CastDSL.() -> Unit): Cast{
    return invalidIfNull(CastDSL().apply(receiver).cast, "ERROR.43")
}

// case expressions
fun <T> case_(column: BindableColumn<T>): SimpleCaseDSL<T> {
    return simpleCase(column)
}

fun case_(): SearchedCaseDSL {
    return searchedCase()
}

@JvmSynthetic
fun case(dslCompleter: KSearchedCaseDSL.() -> Unit): SearchedCaseModel{
    return KSearchedCaseDSL().apply(dslCompleter).run {
        SearchedCaseModel(whenConditions,elseValue)
    }
}

@JvmSynthetic
fun <T : Any> case(column: BindableColumn<T>, dslCompleter: KSimpleCaseDSL<T>.() -> Unit) : SimpleCaseModel<T>{
    return KSimpleCaseDSL<T>().apply(dslCompleter).run {
        SimpleCaseModel(column,whenConditions,elseValue)
    }
}


class CastFinisher(private val column: BasicColumn) {

    fun `as`(targetType: String): Cast {
        return Cast(column,targetType)
    }

}