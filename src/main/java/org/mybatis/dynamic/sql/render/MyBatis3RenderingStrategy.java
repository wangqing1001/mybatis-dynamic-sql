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
package org.mybatis.dynamic.sql.render;

import org.mybatis.dynamic.sql.BindableColumn;

import java.sql.JDBCType;

public class MyBatis3RenderingStrategy extends RenderingStrategy {
    @Override
    public String getFormattedJdbcPlaceholder(String prefix, String parameterName) {
        return "#{" //$NON-NLS-1$
                + prefix
                + "." //$NON-NLS-1$
                + parameterName
                + "}"; //$NON-NLS-1$
    }

    @Override
    public String getFormattedJdbcPlaceholder(BindableColumn<?> column, String prefix, String parameterName) {
        return "#{" //$NON-NLS-1$
                + prefix
                + "." //$NON-NLS-1$
                + parameterName
                + renderJdbcType(column)
                + renderJavaType(column)
                + renderTypeHandler(column)
                + "}"; //$NON-NLS-1$
    }

    @Override
    public String getRecordBasedInsertBinding(BindableColumn<?> column, String parameterName) {
        return "#{" //$NON-NLS-1$
                + parameterName
                + renderJdbcType(column)
                + renderJavaType(column)
                + renderTypeHandler(column)
                + "}"; //$NON-NLS-1$
    }

    private String renderTypeHandler(BindableColumn<?> column) {
        String typeHandler = column.typeHandler();
        if(typeHandler==null){
            return "";
        }
        return ",typeHandler=" + typeHandler;
    }

    private String renderJdbcType(BindableColumn<?> column) {
        JDBCType jdbcType = column.jdbcType();
        if(jdbcType==null){
            return "";
        }
        return ",jdbcType=" + jdbcType.getName();
    }

    private String renderJavaType(BindableColumn<?> column) {
        Class<?> javaType = column.javaType();
        if(javaType==null){
            return "";
        }
        return ",javaType=" + javaType.getName();
    }
}
