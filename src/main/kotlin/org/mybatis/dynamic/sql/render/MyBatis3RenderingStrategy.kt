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

/**
 * MyBatis3 渲染策略。生成 MyBatis 风格的 JDBC 占位符。
 */
open class MyBatis3RenderingStrategy : RenderingStrategy() {
    override fun getFormattedJdbcPlaceholder(prefix: String, parameterName: String): String {
        return "#{$prefix.$parameterName}" //$NON-NLS-1$
    }

    override fun getFormattedJdbcPlaceholder(column: BindableColumn<*>, prefix: String, parameterName: String): String {
        return "#{"  + prefix+ "." + parameterName+ renderJdbcType(column)+ renderJavaType(column)+ renderTypeHandler(column)+ "}" //$NON-NLS-1$
    }

    override fun getRecordBasedInsertBinding(column: BindableColumn<*>, parameterName: String): String {
        return "#{"+ parameterName+ renderJdbcType(column)+ renderJavaType(column)+ renderTypeHandler(column)+ "}" //$NON-NLS-1$
    }

    private fun renderTypeHandler(column: BindableColumn<*>): String {
        val typeHandler = column.typeHandler()
        return if (typeHandler == null) {
            ""
        } else {
            ",typeHandler=$typeHandler"
        }
    }

    private fun renderJdbcType(column: BindableColumn<*>): String {
        val jdbcType = column.jdbcType()
        return if (jdbcType == null) {
            ""
        } else {
            ",jdbcType=" + jdbcType.name
        }
    }

    private fun renderJavaType(column: BindableColumn<*>): String {
        val javaType = column.javaType()
        return if (javaType == null) {
            ""
        } else {
            ",javaType=" + javaType.name
        }
    }
}
