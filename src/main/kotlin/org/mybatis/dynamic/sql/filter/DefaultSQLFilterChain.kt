package org.mybatis.dynamic.sql.filter


import com.zl.data.dsl.filter.SQLFilter
import com.zl.data.dsl.filter.SQLFilterChain
import org.mybatis.dynamic.sql.dsl.CountDSLCompleter
import org.mybatis.dynamic.sql.dsl.DeleteDSLCompleter
import org.mybatis.dynamic.sql.dsl.SelectDSLCompleter
import org.mybatis.dynamic.sql.dsl.UpdateDSLCompleter
import org.mybatis.dynamic.sql.dsl.WhereDSL
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.where.WhereApplier
import kotlin.Array
import kotlin.Int


/**
 * 默认的 DSL 过滤器链实现（责任链模式）。
 *
 * 内部维护一个游标 [pos]，每次调用 [doxxx()] 时：
 * 1. 若 [pos] 小于过滤器数量 [filters.size()]，取出 [pos] 位置的过滤器并递增游标，
 *    然后调用该过滤器的 `doFilter(completer, this)` 方法；
 * 2. 若 [pos] 已达到过滤器数量（所有过滤器均已执行完毕），直接原样返回 completer，
 *    表示链路处理结束。
 *
 * 过滤器通过把当前过滤器链（`this`）回传给下一个过滤器，实现链式向后传递。
 *
 * 注意：该类为有状态实现（游标 [pos] 会随调用递增），因此同一个链实例
 * 只能用于一次完整的过滤流程，不可复用。
 *
 * @param filters 参与过滤的过滤器数组，顺序即执行顺序
 */
class DefaultSQLFilterChain(val filters: Array<out SQLFilter>) : SQLFilterChain {

    private var pos: Int = 0

    override fun doWhere(completer: WhereDSL.() -> Unit): WhereDSL.() -> Unit {
        var completer1 = completer
        if (pos < filters.size) {
            val filter = filters[pos++]
            completer1 = filter.doWhere(completer1, this)
        }
        return completer1
    }




}
