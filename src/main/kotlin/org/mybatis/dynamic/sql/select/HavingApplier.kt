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
package org.mybatis.dynamic.sql.select

import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.SqlCriterion
import org.mybatis.dynamic.sql.dsl.BooleanOperations
import org.mybatis.dynamic.sql.dsl.WhereOrHavingApplier
import java.util.function.Consumer

/**
 * having 子句应用器,用于支持 and/or 后续操作。
 */
class HavingApplier : WhereOrHavingApplier<HavingApplier> {

    constructor(
        initialCriterion: SqlCriterion,
        subCriteria: List<AndOrCriteriaGroup>
    ) : super(initialCriterion, subCriteria)

    constructor(
        initialCriterion: SqlCriterion,
        subCriteria: List<AndOrCriteriaGroup>,
        after: Consumer<BooleanOperations<*>>
    ) : super(initialCriterion, subCriteria, after)

    override fun buildNew(
        initialCriterion: SqlCriterion,
        subCriteria: List<AndOrCriteriaGroup>,
        after: Consumer<BooleanOperations<*>>
    ): HavingApplier {
        return HavingApplier(initialCriterion, subCriteria, after)
    }
}
