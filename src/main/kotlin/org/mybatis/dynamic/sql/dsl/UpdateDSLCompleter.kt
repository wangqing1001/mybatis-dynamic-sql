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

import org.mybatis.dynamic.sql.update.UpdateModel
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils
import java.util.function.Function

/**
 * 表示可用于创建通用 update 方法的函数。使用此函数时,可以创建不需要用户调用 build() 和 render()
 * 方法的方法——使客户端代码看起来更简洁。
 *
 * <p>此函数旨在与 {@link MyBatis3Utils#update(ToIntFunction, SqlTable, UpdateDSLCompleter)}
 * 等工具方法结合使用。
 *
 * <p>例如,可以像这样创建 mapper 接口方法:
 *
 * <pre>
 * &#64;UpdateProvider(type=SqlProviderAdapter.class, method="update")
 * int update(UpdateStatementProvider updateStatement);
 *
 * default int update(UpdateDSLCompleter completer) {
 *     return MyBatis3Utils.update(this::update, person, completer);
 * }
 * </pre>
 *
 * <p>然后像这样调用简化的 default 方法:
 *
 * <pre>
 * int rows = mapper.update(c -&gt;
 *                c.set(firstName).equalTo("Fred")
 *                .where(id, isEqualTo(100))
 *            );
 * </pre>
 *
 * <p>可以通过省略 where 子句实现 "update all":
 *
 * <pre>
 * int rows = mapper.update(c -&gt;
 *                c.set(firstName).equalTo("Fred")
 *            );
 * </pre>
 */
fun interface UpdateDSLCompleter : Function<UpdateDSL, Buildable<UpdateModel>>
