package examples.emptywhere

import com.zl.data.dsl.filter.SQLFilter
import com.zl.data.dsl.filter.SQLFilterChain
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.dsl.CountDSLCompleter
import org.mybatis.dynamic.sql.dsl.DeleteDSLCompleter
import org.mybatis.dynamic.sql.dsl.SelectDSL
import org.mybatis.dynamic.sql.dsl.SelectDSLCompleter
import org.mybatis.dynamic.sql.dsl.UpdateDSLCompleter
import org.mybatis.dynamic.sql.dsl.WhereDSL
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.GeneralInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.GroupingCriteriaReceiver
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter
import org.mybatis.dynamic.sql.util.kotlin.andThen
import org.mybatis.dynamic.sql.util.kotlin.elements.and
import org.mybatis.dynamic.sql.util.kotlin.elements.applyOperator
import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo
import org.mybatis.dynamic.sql.where.WhereApplier

/**
 * 逻辑删除过滤器。
 *
 * 该过滤器用于实现“软删除”场景：不对数据进行物理删除，而是在每次
 * count / select / update / delete 操作时自动追加 `deleted = false` 条件，
 * 从而保证业务数据只能查询/操作到“未删除”的记录。
 *
 * 工作方式：
 * - **count（Java DSL 风格）**：把 [deleted] 列追加到 where 条件中（`deleted = false`）；
 * - **count（Kotlin DSL 风格）**：通过 `where { and { deleted isEqualTo false } }` 追加条件；
 * - **select / update / delete / insert**：原样透传（默认实现），逻辑删除条件
 *   通常只作用于查询类操作，删除操作由上层调用方决定是否转换为“更新 deleted 字段”。
 *
 * 注意：该过滤器**不会**把 `delete` 语句改写为 `update` 语句，它只负责在
 * 查询/更新/删除语句上追加“未删除”过滤条件。
 *
 * @param deleted 逻辑删除标记列（类型通常为 Boolean，`false` 表示未删除）
 */
class LogicDeleteDSLFilter(val deleted: SqlColumn<Boolean>) : SQLFilter {


    override fun doWhere(completer: WhereDSL.() -> Unit,filterChain: SQLFilterChain): WhereDSL.() -> Unit {
        return filterChain.doWhere{
            addSubCriterion(and {deleted isEqualTo true})
            completer.invoke(this)
        }
    }



}
