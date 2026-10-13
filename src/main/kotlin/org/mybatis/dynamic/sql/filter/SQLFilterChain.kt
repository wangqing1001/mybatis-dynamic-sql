package com.zl.data.dsl.filter

import org.mybatis.dynamic.sql.dsl.CountDSLCompleter
import org.mybatis.dynamic.sql.dsl.DeleteDSLCompleter
import org.mybatis.dynamic.sql.dsl.SelectDSLCompleter
import org.mybatis.dynamic.sql.dsl.UpdateDSLCompleter
import org.mybatis.dynamic.sql.dsl.WhereDSL
import org.mybatis.dynamic.sql.filter.DefaultSQLFilterChain
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.GeneralInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter
import org.mybatis.dynamic.sql.where.WhereApplier


/**
 * DSL 过滤器链接口。
 *
 * 过滤器链是责任链模式中的“链路”抽象，负责把当前待处理的 SQL 构建器
 * （Completer）依次传递给链上的每一个 [SQLFilter] 进行处理。
 *
 * 该接口的默认实现 [DefaultDSLFilterChain] 内部维护一个游标位置，每次调用
 * [doXXX()] 时取出下一个过滤器并调用其 `doXXX(completer, this)` 方法；
 * 过滤器处理完毕后，通过把自身（`this`）再次传入，实现链式向后传递。
 *
 * 与 [SQLFilter] 相同，这里也提供 Java DSL 与 Kotlin DSL 两套重载方法。
 * 默认（未做任何处理时）实现均为“原样返回 completer”，即空过滤链不改变任何内容。
 *
 * 使用方式（通过伴生对象工厂方法创建）：
 * ```kotlin
 * val chain = DSLFilterChain.createFilterChain(filter1, filter2)
 * val filtered = chain.doFilter(completer)
 * ```
 */
interface SQLFilterChain {

    fun doWhere(completer: WhereDSL.()-> Unit):WhereDSL.()-> Unit{
        return completer
    }



    companion object {

        /**
         * 空过滤器链单例。
         *
         * 该实例不包含任何过滤器，对任何 completer 均原样返回，
         * 用于“无过滤需求”的场景，避免每次创建新对象。
         */
        private val NON_FILTER_CHAIN: SQLFilterChain = object : SQLFilterChain {}

        /**
         * 获取一个不做任何处理的空过滤器链（单例）。
         *
         * @return 空过滤器链实例
         */
        fun nonFilterChain(): SQLFilterChain {
            return NON_FILTER_CHAIN
        }

        /**
         * 根据给定的过滤器数组创建过滤器链。
         *
         * 当 [filters] 为空时返回空过滤器链（[nonFilterChain]），
         * 否则返回 [SQLFilterChain] 实例。
         *
         * @param filters 参与过滤的过滤器集合（可省略，表示无需过滤）
         * @return 组装好的过滤器链
         */
        fun createFilterChain(vararg filters: SQLFilter): SQLFilterChain {
            if (filters.isEmpty()) {
                return nonFilterChain()
            }
            return DefaultSQLFilterChain(filters)
        }

    }

}
