/*
 *    Copyright 2016-2026 the original author or authors.
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
package org.mybatis.dynamic.sql.dsl

import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils
import java.util.function.Function

/**
 * 表示可用于创建通用 count 方法的函数。使用此函数时,可以创建不需要用户调用 build() 和 render()
 * 方法的方法——使客户端代码看起来更简洁。
 *
 * <p>此函数旨在与 {@link MyBatis3Utils#countFrom(ToLongFunction, CountDSL, CountDSLCompleter)}
 * 等工具方法结合使用。
 *
 * <p>例如,可以像这样创建 mapper 接口方法:
 *
 * <pre>
 * &#64;SelectProvider(type=SqlProviderAdapter.class, method="select")
 * long count(SelectStatementProvider selectStatement);
 *
 * default long count(CountDSLCompleter completer) {
 *     return MyBatis3Utils.count(this::count, person, completer);
 * }
 * </pre>
 *
 * <p>然后像这样调用简化的 default 方法:
 *
 * <pre>
 * long rows = mapper.count(c -&gt;
 *         c.where(occupation, isNull()));
 * </pre>
 *
 * <p>可以使用以下代码实现 "count all":
 *
 * <pre>
 * long rows = mapper.count(c -&gt; c);
 * </pre>
 *
 * <p>或者
 *
 * <pre>
 * long rows = mapper.count(CountDSLCompleter.allRows());
 * </pre>
 */
fun interface CountDSLCompleter : Function<CountDSL, Buildable<SelectModel>> {

    companion object {
        /**
         * 返回一个可用于统计表中每一行的 completer。
         *
         * @return 将统计表中每一行的 completer
         */
        @JvmStatic
        fun allRows(): CountDSLCompleter {
            return CountDSLCompleter { c: CountDSL -> c }
        }
    }
}
