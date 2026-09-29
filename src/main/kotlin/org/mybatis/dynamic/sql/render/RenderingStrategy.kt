/*
 *    Copyright 2016-2025 the original author or authors.
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
package org.mybatis.dynamic.sql.render

import org.mybatis.dynamic.sql.BindableColumn
import java.util.concurrent.atomic.AtomicInteger

/**
 * 渲染策略用于生成平台特定的绑定。
 *
 * <p>渲染策略在语句生成的渲染阶段使用。
 * 所有生成的 SQL 语句都包含生成的语句本身,以及一个应在执行时绑定到语句的参数映射。
 * 例如,为 MyBatis 渲染时,生成的 select 语句可能如下所示:
 *
 * <p><code>select foo from bar where id = #{parameters.p1,jdbcType=INTEGER}</code>
 *
 * <p>在这种情况下,绑定是 <code>#{parameters.p1,jdbcType=INTEGER}</code>。MyBatis 知道如何解释此绑定 -
 * 它将在传递给语句的参数对象的 <code>parameters.p1</code> 属性中查找一个值,并在执行语句时将其绑定为预编译语句参数。
 */
abstract class RenderingStrategy {
    /**
     * 生成一个可用于将参数值放入参数映射中的唯一键。
     *
     * @param sequence 用于计算唯一值的序列
     * @return 用于将参数值放入参数映射中的键
     */
    open fun formatParameterMapKey(sequence: AtomicInteger): String {
        return "p" + sequence.getAndIncrement() //$NON-NLS-1$
    }

    /**
     * 返回一个用作 fetch first 查询参数的参数映射键。
     *
     * <p>默认情况下,此参数与其他参数相同。此方法是支持 MyBatis Spring Batch 的钩子。
     *
     * @param sequence 用于计算唯一值的序列
     * @return 用于将参数值放入参数映射中的键
     */
    open fun formatParameterMapKeyForFetchFirstRows(sequence: AtomicInteger): String {
        return formatParameterMapKey(sequence)
    }

    /**
     * 返回一个用作 limit 查询参数的参数映射键。
     *
     * <p>默认情况下,此参数与其他参数相同。此方法是支持 MyBatis Spring Batch 的钩子。
     *
     * @param sequence 用于计算唯一值的序列
     * @return 用于将参数值放入参数映射中的键
     */
    open fun formatParameterMapKeyForLimit(sequence: AtomicInteger): String {
        return formatParameterMapKey(sequence)
    }

    /**
     * 返回一个用作查询 offset 参数的参数映射键。
     *
     * <p>默认情况下,此参数与其他参数相同。此方法是支持 MyBatis Spring Batch 的钩子。
     *
     * @param sequence 用于计算唯一值的序列
     * @return 用于将参数值放入参数映射中的键
     */
    open fun formatParameterMapKeyForOffset(sequence: AtomicInteger): String {
        return formatParameterMapKey(sequence)
    }

    /**
     * 此方法为参数生成绑定到生成的 SQL 语句中占位符的绑定。
     *
     * <p>当参数与已知的目标列之间存在映射时,此绑定是合适的。
     * 在 MyBatis 中,绑定可以根据列指定类型信息。绑定特定于目标框架。
     *
     * <p>对于 MyBatis,绑定如下所示: "#{prefix.parameterName,jdbcType=xxx,typeHandler=xxx,javaType=xxx}"
     *
     * <p>对于 Spring,绑定如下所示: ":parameterName"
     *
     * @param column 用于在 MyBatis 绑定中生成类型详细信息的列定义。Spring 忽略。
     * @param prefix 用于在 SQL provider 对象中定位参数的参数前缀。通常是
     *     [DEFAULT_PARAMETER_PREFIX]。Spring 忽略。
     * @param parameterName 参数名称。通常通过调用
     *     [formatParameterMapKey] 生成
     * @return 生成的绑定
     */
    abstract fun getFormattedJdbcPlaceholder(column: BindableColumn<*>, prefix: String, parameterName: String): String

    /**
     * 此方法为参数生成绑定到生成的 SQL 语句中占位符的绑定。
     *
     * <p>当参数绑定到不是已知列的占位符(如 limit 或 offset 参数)时,此绑定是合适的。
     * 绑定特定于目标框架。
     *
     * <p>对于 MyBatis,绑定如下所示: "#{prefix.parameterName}"
     *
     * <p>对于 Spring,绑定如下所示: ":parameterName"
     *
     * @param prefix 用于在 SQL provider 对象中定位参数的参数前缀。通常是
     *     [DEFAULT_PARAMETER_PREFIX]。Spring 忽略。
     * @param parameterName 参数名称。通常通过调用
     *     [formatParameterMapKey] 生成
     * @return 生成的绑定
     */
    abstract fun getFormattedJdbcPlaceholder(prefix: String, parameterName: String): String

    /**
     * 此方法为参数生成绑定到生成的 SQL 语句中占位符的绑定。
     *
     * <p>此方法用于为 limit、offset 和 fetch first 参数生成绑定。默认情况下,这些
     * 参数与其他参数相同。此方法支持 MyBatis Spring Batch 集成,其中
     * 参数键具有预定义值,需要特殊处理。
     *
     * @param prefix 用于在 SQL provider 对象中定位参数的参数前缀。通常是
     *     [DEFAULT_PARAMETER_PREFIX]。Spring 忽略。
     * @param parameterName 参数名称。通常通过调用
     *     [formatParameterMapKey] 生成
     * @return 生成的绑定
     */
    open fun getFormattedJdbcPlaceholderForPagingParameters(prefix: String, parameterName: String): String {
        return getFormattedJdbcPlaceholder(prefix, parameterName)
    }

    /**
     * 此方法为参数生成绑定到行式 insert 语句中占位符的绑定。
     *
     * <p>此绑定专门用于 insert、batch insert 和 multirow insert 语句。
     * 这些语句将参数绑定到行类的属性。Spring 实现更改绑定
     * 以匹配这些 insert 语句期望的值。对于 MyBatis,绑定与
     * [getFormattedJdbcPlaceholder] 相同。
     *
     * <p>对于 MyBatis,绑定如下所示: "#{prefix.parameterName,jdbcType=xxx,typeHandler=xxx,javaType=xxx}"
     *
     * <p>对于 Spring,绑定如下所示: ":prefix.parameterName"
     *
     * @param column 用于在 MyBatis 绑定中生成类型详细信息的列定义。Spring 忽略。
     * @param prefix 用于在 SQL provider 对象中定位参数的参数前缀。通常是
     *     "row" 或 "records[x]",以匹配生成的语句对象类的属性。
     * @param parameterName 参数名称。通常,这是与 insert 语句关联的行类中的属性。
     * @return 生成的绑定
     */
    open fun getRecordBasedInsertBinding(column: BindableColumn<*>, prefix: String, parameterName: String): String {
        return getFormattedJdbcPlaceholder(column, prefix, parameterName)
    }

    /**
     * 此方法为参数生成绑定到行式 insert 语句中占位符的绑定。
     *
     * <p>此绑定专门用于 insert、batch insert 和 multirow insert 语句以及
     * MapToRow 映射。这些语句直接将参数绑定到行类。
     *
     * <p>对于 MyBatis,绑定如下所示: "#{parameterName,jdbcType=xxx,typeHandler=xxx,javaType=xxx}"
     *
     * <p>对于 Spring,绑定如下所示: ":parameterName"
     *
     * @param column 用于在 MyBatis 绑定中生成类型详细信息的列定义。Spring 忽略。
     * @param parameterName 参数名称。通常是
     *     "row" 或 "records[x]",以匹配生成的语句对象类的属性。
     * @return 生成的绑定
     */
    abstract fun getRecordBasedInsertBinding(column: BindableColumn<*>, parameterName: String): String

    companion object {
        const val DEFAULT_PARAMETER_PREFIX: String = "parameters" //$NON-NLS-1$
    }
}
