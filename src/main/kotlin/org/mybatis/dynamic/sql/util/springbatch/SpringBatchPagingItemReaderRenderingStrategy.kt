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
package org.mybatis.dynamic.sql.util.springbatch

import org.mybatis.dynamic.sql.render.MyBatis3RenderingStrategy
import java.util.concurrent.atomic.AtomicInteger

/**
 * 此渲染策略应用于使用 mybatis-spring 集成提供的 MyBatisPagingItemReader 的 MyBatis3 语句
 * (<a href="http://www.mybatis.org/spring/">http://www.mybatis.org/spring/</a>)。
 */
class SpringBatchPagingItemReaderRenderingStrategy : MyBatis3RenderingStrategy() {

    override fun getFormattedJdbcPlaceholderForPagingParameters(prefix: String, parameterName: String): String {
        return "#{$parameterName}" //$NON-NLS-1$
    }

    override fun formatParameterMapKeyForFetchFirstRows(sequence: AtomicInteger): String {
        return "_pagesize" //$NON-NLS-1$
    }

    override fun formatParameterMapKeyForLimit(sequence: AtomicInteger): String {
        return "_pagesize" //$NON-NLS-1$
    }

    override fun formatParameterMapKeyForOffset(sequence: AtomicInteger): String {
        return "_skiprows" //$NON-NLS-1$
    }
}
