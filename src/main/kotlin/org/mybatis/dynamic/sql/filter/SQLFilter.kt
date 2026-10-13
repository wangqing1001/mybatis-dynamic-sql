package com.zl.data.dsl.filter

import org.mybatis.dynamic.sql.dsl.CountDSLCompleter
import org.mybatis.dynamic.sql.dsl.SelectDSLCompleter
import org.mybatis.dynamic.sql.dsl.WhereDSL
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.where.WhereApplier
import kotlin.jvm.JvmName


/**
 * DSL 过滤器接口。
 *
 * 该接口基于“责任链模式（Chain of Responsibility）”对 MyBatis Dynamic SQL 的
 * 各类 SQL 构建器（Completer）进行拦截与增强，用于在生成 SQL 语句之前统一注入
 * 公共逻辑，例如：
 * - 逻辑删除条件的自动追加（[LogicDeleteDSLFilter]）
 * - 创建时间 / 更新时间字段的自动填充（[DateTimeDSLFilter]）
 * - 创建人 / 更新人字段的自动填充（[UserAuditDSLFilter]）
 *
 * 每个过滤器只需实现自己关心的操作类型，其余类型的 [doXXX()] 方法默认直接
 * 透传给链上的下一个过滤器（即默认实现为 `filterChain.doFilter(completer)`）。
 *
 * 接口中同时提供了两套重载方法：
 * - Java DSL 风格（[CountDSLCompleter]、[SelectDSLCompleter] 等），参数为具体类型；
 * - Kotlin DSL 风格（[CountCompleter]、[SelectCompleter] 等）。
 *
 * 注意：Kotlin DSL 风格的各 Completer 均为“函数类型别名”，在 JVM 上会统一擦除为
 * `Function1`，因此这些重载方法通过 [JvmName] 指定了不同的 JVM 方法名以避免字节码
 * 签名冲突（Kotlin 侧调用不受影响），并配合 [JvmSynthetic] 对 Java 侧隐藏。
 *
 * 两套风格可以共存，但需要注意：若同时注册了 Java 风格与 Kotlin 风格的过滤器，
 * 两者之间并不共享构建上下文，因此实际使用时通常只会使用其中一种风格。
 */
interface SQLFilter {

    fun doWhere(completer:WhereDSL.() -> Unit,filterChain: SQLFilterChain): WhereDSL.()-> Unit{
        return completer
    }

}
