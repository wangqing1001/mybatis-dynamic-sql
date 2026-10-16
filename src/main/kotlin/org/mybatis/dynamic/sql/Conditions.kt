@file:JvmName("SqlBuilder")
@file:JvmMultifileClass
package org.mybatis.dynamic.sql

import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.kotlin.KotlinSubQueryBuilder
import org.mybatis.dynamic.sql.where.condition.*

fun <T: Any> isNull() = IsNull<T>()

fun <T: Any> isNotNull() = IsNotNull<T>()

fun <T: Any> isEqualTo(value: T) = IsEqualTo.of(value)

fun <T: Any> isEqualTo(selectModelBuilder: Buildable<SelectModel>) = IsEqualToWithSubselect.of<T>(selectModelBuilder)

@JvmSynthetic
fun <T: Any> isEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit): IsEqualToWithSubselect<T>{
    return isEqualTo(KotlinSubQueryBuilder().apply(subQuery))
}

fun <T: Any> isEqualTo(column: BasicColumn) = IsEqualToColumn.of<T>(column)

fun <T: Any> isEqualToWhenPresent(value: T?) = IsEqualToWhenPresent.of(value)

fun <T: Any> isNotEqualTo(value: T) = IsNotEqualTo.of(value)

fun <T: Any> isNotEqualTo(selectModelBuilder: Buildable<SelectModel>) = IsNotEqualToWithSubselect.of<T>(selectModelBuilder)

@JvmSynthetic
fun <T: Any> isNotEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit): IsNotEqualToWithSubselect<T>{
    return isNotEqualTo(KotlinSubQueryBuilder().apply(subQuery))
}

fun <T: Any> isNotEqualTo(column: BasicColumn) = IsNotEqualToColumn.of<T>(column)

fun <T: Any> isNotEqualToWhenPresent(value: T?) = IsNotEqualToWhenPresent.of(value)

fun <T: Any> isGreaterThan(value: T) = IsGreaterThan.of(value)

fun <T: Any> isGreaterThan(selectModelBuilder: Buildable<SelectModel>) = IsGreaterThanWithSubselect.of<T>(selectModelBuilder)

@JvmSynthetic
fun <T: Any> isGreaterThan(subQuery: KotlinSubQueryBuilder.() -> Unit): IsGreaterThanWithSubselect<T>{
    return isGreaterThan(KotlinSubQueryBuilder().apply(subQuery))
}

fun <T: Any> isGreaterThan(column: BasicColumn) = IsGreaterThanColumn.of<T>(column)

fun <T: Any> isGreaterThanWhenPresent(value: T?) = IsGreaterThanWhenPresent.of(value)

fun <T: Any> isGreaterThanOrEqualTo(value: T) = IsGreaterThanOrEqualTo.of(value)

fun <T: Any> isGreaterThanOrEqualTo(selectModelBuilder: Buildable<SelectModel>) = IsGreaterThanOrEqualToWithSubselect.of<T>(selectModelBuilder)

@JvmSynthetic
fun <T: Any> isGreaterThanOrEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit): IsGreaterThanOrEqualToWithSubselect<T>{
    return isGreaterThanOrEqualTo(KotlinSubQueryBuilder().apply(subQuery))
}

fun <T: Any> isGreaterThanOrEqualTo(column: BasicColumn) = IsGreaterThanOrEqualToColumn.of<T>(column)

fun <T: Any> isGreaterThanOrEqualToWhenPresent(value: T?) = IsGreaterThanOrEqualToWhenPresent.of(value)

fun <T> isLessThan(value: T):IsLessThan<T> = IsLessThan.of(value)

fun <T: Any> isLessThan(selectModelBuilder: Buildable<SelectModel>) = IsLessThanWithSubselect.of<T>(selectModelBuilder)

@JvmSynthetic
fun <T: Any> isLessThan(subQuery: KotlinSubQueryBuilder.() -> Unit): IsLessThanWithSubselect<T>{
    return isLessThan(KotlinSubQueryBuilder().apply(subQuery))
}

fun <T: Any> isLessThan(column: BasicColumn) = IsLessThanColumn.of<T>(column)

fun <T: Any> isLessThanWhenPresent(value: T?) = IsLessThanWhenPresent.of(value)

fun <T: Any> isLessThanOrEqualTo(value: T) = IsLessThanOrEqualTo.of(value)

fun <T: Any> isLessThanOrEqualTo(selectModelBuilder: Buildable<SelectModel>) = IsLessThanOrEqualToWithSubselect.of<T>(selectModelBuilder)

@JvmSynthetic
fun <T: Any> isLessThanOrEqualTo(subQuery: KotlinSubQueryBuilder.() -> Unit): IsLessThanOrEqualToWithSubselect<T>{
    return isLessThanOrEqualTo(KotlinSubQueryBuilder().apply(subQuery))
}

fun <T: Any> isLessThanOrEqualTo(column: BasicColumn) = IsLessThanOrEqualToColumn.of<T>(column)

fun <T: Any> isLessThanOrEqualToWhenPresent(value: T?) = IsLessThanOrEqualToWhenPresent.of(value)

fun <T: Any> isIn(vararg values: T) = isIn(listOf(*values))

fun <T: Any> isIn(values: Collection<T>) = IsIn.of(values)

fun <T: Any> isIn(selectModelBuilder: Buildable<SelectModel>) = IsInWithSubselect.of<T>(selectModelBuilder)

@JvmSynthetic
fun <T: Any> isIn(subQuery: KotlinSubQueryBuilder.() -> Unit): IsInWithSubselect<T>{
    return isIn(KotlinSubQueryBuilder().apply(subQuery))
}

fun <T: Any> isInWhenPresent(vararg values: T?) = isInWhenPresent(listOf(*values))

fun <T: Any> isInWhenPresent(values: Collection<T?>?) = IsInWhenPresent.of(values)

fun <T: Any> isNotIn(vararg values: T) = isNotIn(listOf(*values))

fun <T: Any> isNotIn(values: Collection<T>) = IsNotIn.of(values)

fun <T: Any> isNotIn(selectModelBuilder: Buildable<SelectModel>) = IsNotInWithSubselect.of<T>(selectModelBuilder)

@JvmSynthetic
fun <T: Any> isNotIn(subQuery: KotlinSubQueryBuilder.() -> Unit): IsNotInWithSubselect<T>{
    return isNotIn(KotlinSubQueryBuilder().apply(subQuery))
}

fun <T: Any> isNotInWhenPresent(vararg values: T?) = isNotInWhenPresent(listOf(*values))

fun <T: Any> isNotInWhenPresent(values: Collection<T?>?) = IsNotInWhenPresent.of(values)

fun <T: Any> isBetween(value1: T) = BetweenBuilder(value1)

fun <T: Any> isBetweenWhenPresent(value1: T?) = BetweenWhenPresentBuilder(value1)

fun <T: Any> isNotBetween(value1: T) = NotBetweenBuilder(value1)

fun <T: Any> isNotBetweenWhenPresent(value1: T?) =  NotBetweenWhenPresentBuilder(value1)

fun <T: Any> isLike(value: T) = IsLike.of(value)

fun <T: Any> isLikeWhenPresent(value: T?) = IsLikeWhenPresent.of(value)

fun <T: Any> isNotLike(value: T) = IsNotLike.of(value)

fun <T: Any> isNotLikeWhenPresent(value: T?) = IsNotLikeWhenPresent.of(value)

fun isTrue(): IsEqualTo<Boolean> = isEqualTo(true)

fun isFalse(): IsEqualTo<Boolean> = isEqualTo(false)

fun isLikeCaseInsensitive(value: String) = IsLikeCaseInsensitive.of(value)

fun isLikeCaseInsensitiveWhenPresent(value: String?) = IsLikeCaseInsensitiveWhenPresent.of(value)

fun isNotLikeCaseInsensitive(value: String) = IsNotLikeCaseInsensitive.of(value)

fun isNotLikeCaseInsensitiveWhenPresent(value: String?) = IsNotLikeCaseInsensitiveWhenPresent.of(value)

fun isInCaseInsensitive(vararg values: String) = isInCaseInsensitive(listOf(*values))

fun isInCaseInsensitive(values: Collection<String>) = IsInCaseInsensitive.of(values)

fun isInCaseInsensitiveWhenPresent(vararg values: String?) = isInCaseInsensitiveWhenPresent(listOf(*values))

fun isInCaseInsensitiveWhenPresent(values: Collection<String?>?) = IsInCaseInsensitiveWhenPresent.of(values)

fun isNotInCaseInsensitive(vararg values: String) = isNotInCaseInsensitive(listOf(*values))

fun isNotInCaseInsensitive(values: Collection<String>) = IsNotInCaseInsensitive.of(values)

fun isNotInCaseInsensitiveWhenPresent(vararg values: String?) = isNotInCaseInsensitiveWhenPresent(listOf(*values))

fun isNotInCaseInsensitiveWhenPresent(values: Collection<String?>?) = IsNotInCaseInsensitiveWhenPresent.of(values)

class BetweenBuilder<T : Any>(private val value1: T) {
    fun and(value2: T): IsBetween<T> = IsBetween.isBetween(value1).and(value2)
}

class BetweenWhenPresentBuilder<T : Any>(private val value1: T?) {

    fun and(value2: T?): IsBetweenWhenPresent<T> {
        return IsBetweenWhenPresent.isBetweenWhenPresent(value1).and(value2)
    }

}

class NotBetweenBuilder<T : Any>(private val value1: T) {

    fun and(value2: T): IsNotBetween<T> = IsNotBetween.isNotBetween(value1).and(value2)

}

class NotBetweenWhenPresentBuilder<T : Any>(private val value1: T?) {

    fun and(value2: T?): IsNotBetweenWhenPresent<T> {
        return IsNotBetweenWhenPresent.isNotBetweenWhenPresent(value1).and(value2)
    }

}