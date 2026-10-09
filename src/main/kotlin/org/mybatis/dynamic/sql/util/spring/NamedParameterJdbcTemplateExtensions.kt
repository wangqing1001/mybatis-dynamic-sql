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
package org.mybatis.dynamic.sql.util.spring

import org.mybatis.dynamic.sql.delete.DeleteModel
import org.mybatis.dynamic.sql.delete.DeleteStatementProvider
import org.mybatis.dynamic.sql.insert.batch.BatchInsertModel
import org.mybatis.dynamic.sql.insert.GeneralInsertModel
import org.mybatis.dynamic.sql.insert.InsertModel
import org.mybatis.dynamic.sql.insert.batch.MultiRowInsertModel
import org.mybatis.dynamic.sql.insert.batch.BatchInsert
import org.mybatis.dynamic.sql.insert.GeneralInsertStatementProvider
import org.mybatis.dynamic.sql.insert.InsertStatementProvider
import org.mybatis.dynamic.sql.insert.batch.MultiRowInsertStatementProvider
import org.mybatis.dynamic.sql.render.RenderingStrategies
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.SelectStatementProvider
import org.mybatis.dynamic.sql.update.UpdateModel
import org.mybatis.dynamic.sql.update.UpdateStatementProvider
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.Utilities
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.KeyHolder
import java.util.Objects
import java.util.Optional

/**
 * NamedParameterJdbcTemplate 的扩展类,提供类型安全的数据库操作方法。
 */
class NamedParameterJdbcTemplateExtensions(template: NamedParameterJdbcTemplate) {
    private val template: NamedParameterJdbcTemplate = Objects.requireNonNull(template)

    fun count(countStatement: Buildable<SelectModel>): Long {
        return count(countStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER))
    }

    fun count(countStatement: SelectStatementProvider): Long {
        val answer = template.queryForObject(
            countStatement.selectStatement,
            countStatement.parameters,
            Long::class.java
        )

        return Utilities.safelyUnbox(answer)
    }

    fun delete(deleteStatement: Buildable<DeleteModel>): Int {
        return delete(deleteStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER))
    }

    fun delete(deleteStatement: DeleteStatementProvider): Int {
        return template.update(deleteStatement.deleteStatement, deleteStatement.parameters)
    }

    fun generalInsert(insertStatement: Buildable<GeneralInsertModel>): Int {
        return generalInsert(insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER))
    }

    fun generalInsert(insertStatement: GeneralInsertStatementProvider): Int {
        return template.update(insertStatement.insertStatement, insertStatement.parameters)
    }

    fun generalInsert(insertStatement: Buildable<GeneralInsertModel>, keyHolder: KeyHolder): Int {
        return generalInsert(insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER), keyHolder)
    }

    fun generalInsert(insertStatement: GeneralInsertStatementProvider, keyHolder: KeyHolder): Int {
        return template.update(
            insertStatement.insertStatement,
            MapSqlParameterSource(insertStatement.parameters),
            keyHolder
        )
    }

    fun <T> insert(insertStatement: Buildable<InsertModel<T>>): Int {
        return insert(insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER))
    }

    fun <T> insert(insertStatement: InsertStatementProvider<T>): Int {
        return template.update(
            insertStatement.insertStatement,
            BeanPropertySqlParameterSource(insertStatement)
        )
    }

    fun <T> insert(insertStatement: Buildable<InsertModel<T>>, keyHolder: KeyHolder): Int {
        return insert(insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER), keyHolder)
    }

    fun <T> insert(insertStatement: InsertStatementProvider<T>, keyHolder: KeyHolder): Int {
        return template.update(
            insertStatement.insertStatement,
            BeanPropertySqlParameterSource(insertStatement),
            keyHolder
        )
    }

    fun <T> insertBatch(insertStatement: Buildable<BatchInsertModel<T>>): IntArray {
        return insertBatch(insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER))
    }

    fun <T> insertBatch(insertStatement: BatchInsert<T>): IntArray {
        return template.batchUpdate(
            insertStatement.insertStatement,
            BatchInsertUtility.createBatch(insertStatement.records)
        )
    }

    fun <T> insertMultiple(insertStatement: Buildable<MultiRowInsertModel<T>>): Int {
        return insertMultiple(insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER))
    }

    fun <T> insertMultiple(insertStatement: MultiRowInsertStatementProvider<T>): Int {
        return template.update(
            insertStatement.insertStatement,
            BeanPropertySqlParameterSource(insertStatement)
        )
    }

    fun <T> insertMultiple(insertStatement: Buildable<MultiRowInsertModel<T>>, keyHolder: KeyHolder): Int {
        return insertMultiple(insertStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER), keyHolder)
    }

    fun <T> insertMultiple(insertStatement: MultiRowInsertStatementProvider<T>, keyHolder: KeyHolder): Int {
        return template.update(
            insertStatement.insertStatement,
            BeanPropertySqlParameterSource(insertStatement),
            keyHolder
        )
    }

    fun <T> selectList(selectStatement: Buildable<SelectModel>, rowMapper: RowMapper<T>): List<T> {
        return selectList(selectStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER), rowMapper)
    }

    fun <T> selectList(selectStatement: SelectStatementProvider, rowMapper: RowMapper<T>): List<T> {
        return template.query(selectStatement.selectStatement, selectStatement.parameters, rowMapper)
    }

    fun <T> selectOne(selectStatement: Buildable<SelectModel>, rowMapper: RowMapper<T>): Optional<T & Any> {
        return selectOne(selectStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER), rowMapper)
    }

    fun <T> selectOne(selectStatement: SelectStatementProvider, rowMapper: RowMapper<T>): Optional<T & Any> {
        var result: T?
        try {
            result = template.queryForObject(
                selectStatement.selectStatement,
                selectStatement.parameters,
                rowMapper
            )

        } catch (e: EmptyResultDataAccessException) {
            result = null
        }

        return Optional.ofNullable(result)
    }

    fun update(updateStatement: Buildable<UpdateModel>): Int {
        return update(updateStatement.build().render(RenderingStrategies.SPRING_NAMED_PARAMETER))
    }

    fun update(updateStatement: UpdateStatementProvider): Int {
        return template.update(updateStatement.updateStatement, updateStatement.parameters)
    }
}
