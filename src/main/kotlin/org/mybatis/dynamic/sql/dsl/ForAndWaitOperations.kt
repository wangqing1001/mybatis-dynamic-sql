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

/**
 * for 子句与 wait 子句操作接口。
 */
interface ForAndWaitOperations<T> {
    fun forUpdate(): T {
        return setForClause("for update") //$NON-NLS-1$
    }

    fun forNoKeyUpdate(): T {
        return setForClause("for no key update") //$NON-NLS-1$
    }

    fun forShare(): T {
        return setForClause("for share") //$NON-NLS-1$
    }

    fun forKeyShare(): T {
        return setForClause("for key share") //$NON-NLS-1$
    }

    fun skipLocked(): T {
        return setWaitClause("skip locked") //$NON-NLS-1$
    }

    fun nowait(): T {
        return setWaitClause("nowait") //$NON-NLS-1$
    }

    fun setWaitClause(waitClause: String): T

    fun setForClause(forClause: String): T
}
