package org.mybatis.dynamic.sql.filter

import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.CriteriaGroup
import org.mybatis.dynamic.sql.NullCriterion
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.TableExpression
import org.mybatis.dynamic.sql.where.WhereApplier


/**
 * [WhereFilterContext] 的默认实现。
 *
 * <p>该实现是可变累加器,但生命周期仅限一次过滤器链执行;每次执行都会创建新实例,
 * 因此不会像持有游标的实现那样出现「第二次调用静默失效」的问题。
 */
internal class DefaultWhereFilterContext(
    override val table: TableExpression
) : WhereFilterContext {

    private var initialCriterion: SqlCriterion = NullCriterion()
    private val subCriteria: MutableList<AndOrCriteriaGroup> = mutableListOf()

    override fun and(criterion: SqlCriterion): WhereFilterContext = addCriterion("and", criterion)

    override fun or(criterion: SqlCriterion): WhereFilterContext = addCriterion("or", criterion)

    override fun applyWhere(applier: WhereApplier): WhereFilterContext =
        addCriterion("and", CriteriaGroup(applier.initialCriterion(), applier.subCriteria()))

    override fun toWhereApplier(): WhereApplier = WhereApplier(initialCriterion, subCriteria)

    private fun addCriterion(connector: String, criterion: SqlCriterion): WhereFilterContext {
        if (isFirstCriterion()) {
            initialCriterion = criterion
        } else {
            subCriteria.add(AndOrCriteriaGroup(connector, criterion))
        }
        return this
    }

    private fun isFirstCriterion(): Boolean =
        initialCriterion is NullCriterion && subCriteria.isEmpty()
}