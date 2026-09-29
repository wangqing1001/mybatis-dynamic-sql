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
package org.mybatis.dynamic.sql

import org.mybatis.dynamic.sql.ColumnAndConditionCriterion.Companion.withColumn
import org.mybatis.dynamic.sql.Constant.Companion.of
import org.mybatis.dynamic.sql.SqlBuilder.Companion.concat
import org.mybatis.dynamic.sql.SqlBuilder.Companion.concatenate
import org.mybatis.dynamic.sql.SqlBuilder.Companion.insertBatch
import org.mybatis.dynamic.sql.dsl.*
import org.mybatis.dynamic.sql.insert.*
import org.mybatis.dynamic.sql.insert.InsertSelectDSL.SelectGatherer
import org.mybatis.dynamic.sql.select.ColumnSortSpecification
import org.mybatis.dynamic.sql.select.MultiSelectDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.select.SimpleSortSpecification
import org.mybatis.dynamic.sql.select.aggregate.*
import org.mybatis.dynamic.sql.select.aggregate.Sum.Companion.of
import org.mybatis.dynamic.sql.select.caseexpression.SearchedCaseDSL
import org.mybatis.dynamic.sql.select.caseexpression.SearchedCaseDSL.Companion.searchedCase
import org.mybatis.dynamic.sql.select.caseexpression.SimpleCaseDSL
import org.mybatis.dynamic.sql.select.caseexpression.SimpleCaseDSL.Companion.simpleCase
import org.mybatis.dynamic.sql.select.function.*
import org.mybatis.dynamic.sql.select.function.Substring.Companion.of
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.where.condition.*
import java.util.*
import java.util.function.Supplier

interface SqlBuilder {

    class InsertIntoNextStep(table: SqlTable) {
        private val table: SqlTable = Objects.requireNonNull(table)

        fun withSelectStatement(selectModelBuilder: Buildable<SelectModel>): InsertSelectDSL {
            return InsertSelectDSL.insertInto(table)
                .withSelectStatement(selectModelBuilder)
        }

        fun withColumnList(vararg columns: SqlColumn<*>): SelectGatherer {
            return InsertSelectDSL.insertInto(table)
                .withColumnList(*columns)
        }

        fun withColumnList(columns: List<SqlColumn<*>>): SelectGatherer {
            return InsertSelectDSL.insertInto(table)
                .withColumnList(columns)
        }

        fun <T> set(column: SqlColumn<T>): GeneralInsertDSL.SetClauseFinisher<T> {
            return GeneralInsertDSL.insertInto(table)
                .set(column)
        }
    }

    class CastFinisher(private val column: BasicColumn) {
        fun `as`(targetType: String): Cast {
            return Cast.Builder()
                .withColumn(column)
                .withTargetType(targetType)
                .build()
        }
    }

    companion object {
        // statements
        /**
         * Renders as select count(distinct column) from table...
         *
         * @param column
         * the column to count
         *
         * @return the next step in the DSL
         */
        @JvmStatic
        fun countDistinctColumn(column: BasicColumn): CountDSL {
            return CountDSL.countDistinct(column)
        }

        /**
         * Renders as select count(column) from table...
         *
         * @param column
         * the column to count
         *
         * @return the next step in the DSL
         */
        @JvmStatic
        fun countColumn(column: BasicColumn): CountDSL {
            return CountDSL.count(column)
        }

        /**
         * Renders as select count(*) from table...
         *
         * @param table
         * the table to count
         *
         * @return the next step in the DSL
         */
        @JvmStatic
        fun countFrom(table: SqlTable): CountDSL {
            return CountDSL.countFrom(table)
        }

        @JvmStatic
        fun countFrom(table: SqlTable, tableAlias: String): CountDSL {
            return CountDSL.countFrom(table, tableAlias)
        }

        @JvmStatic
        fun deleteFrom(table: SqlTable): DeleteDSL {
            return DeleteDSL.deleteFrom(table)
        }

        @JvmStatic
        fun deleteFrom(table: SqlTable, tableAlias: String): DeleteDSL {
            return DeleteDSL.deleteFrom(table, tableAlias)
        }
        @JvmStatic
        fun <T> insert(row: T): InsertDSL.IntoGatherer<T> {
            return InsertDSL.insert<T>(row)
        }

        /**
         * Insert a Batch of records. The model object is structured to support bulk inserts with JDBC batch support.
         *
         * @param records
         * records to insert
         * @param <T>
         * the type of record to insert
         *
         * @return the next step in the DSL
        </T> */
        @JvmStatic
        @SafeVarargs
        fun <T> insertBatch(vararg records: T): BatchInsertDSL.IntoGatherer<T> {
            return BatchInsertDSL.insert(*records)
        }

        /**
         * Insert a Batch of records. The model object is structured to support bulk inserts with JDBC batch support.
         *
         * @param records
         * records to insert
         * @param <T>
         * the type of record to insert
         *
         * @return the next step in the DSL
        </T> */
        @JvmStatic
        fun <T> insertBatch(records: MutableCollection<T>): BatchInsertDSL.IntoGatherer<T> {
            return BatchInsertDSL.insert<T>(records)
        }

        /**
         * Insert multiple records in a single statement. The model object is structured as a single insert statement with
         * multiple values clauses. This statement is suitable for use with a small number of records. It is not suitable
         * for large bulk inserts as it is possible to exceed the limit of parameter markers in a prepared statement.
         *
         *
         * For large bulk inserts, see [insertBatch]
         * @param records
         * records to insert
         * @param <T>
         * the type of record to insert
         *
         * @return the next step in the DSL
        </T> */
        @JvmStatic
        @SafeVarargs
        fun <T> insertMultiple(vararg records: T): MultiRowInsertDSL.IntoGatherer<T> {
            return MultiRowInsertDSL.insert(*records)
        }

        /**
         * Insert multiple records in a single statement. The model object is structured as a single insert statement with
         * multiple values clauses. This statement is suitable for use with a small number of records. It is not suitable
         * for large bulk inserts as it is possible to exceed the limit of parameter markers in a prepared statement.
         *
         *
         * For large bulk inserts, see [insertBatch]
         * @param records
         * records to insert
         * @param <T>
         * the type of record to insert
         *
         * @return the next step in the DSL
        </T> */
        @JvmStatic
        fun <T> insertMultiple(records: Collection<T>): MultiRowInsertDSL.IntoGatherer<T> {
            return MultiRowInsertDSL.insert(records)
        }

        @JvmStatic
        fun insertInto(table: SqlTable): InsertIntoNextStep {
            return InsertIntoNextStep(table)
        }

        @JvmStatic
        fun select(vararg selectList: BasicColumn): SelectDSL {
            return select(listOf(*selectList))
        }
        @JvmStatic
        fun select(selectList: Collection<BasicColumn>): SelectDSL {
            return SelectDSL.select(selectList)
        }

        @JvmStatic
        fun selectDistinct(vararg selectList: BasicColumn): SelectDSL {
            return selectDistinct(listOf(*selectList))
        }
        @JvmStatic
        fun selectDistinct(selectList: Collection<BasicColumn>): SelectDSL {
            return SelectDSL.selectDistinct(selectList)
        }
        @JvmStatic
        fun multiSelect(selectModelBuilder: Buildable<SelectModel>): MultiSelectDSL {
            return MultiSelectDSL(selectModelBuilder)
        }

        @JvmStatic
        fun update(table: SqlTable): UpdateDSL {
            return UpdateDSL.update(table)
        }

        @JvmStatic
        fun update(table: SqlTable, tableAlias: String): UpdateDSL {
            return UpdateDSL.update(table, tableAlias)
        }

        @JvmStatic
        fun where(): WhereDSL {
            return WhereDSL()
        }
        @JvmStatic
        fun <T> where(
            column: BindableColumn<T>, condition: RenderableCondition<T>,
            vararg subCriteria: AndOrCriteriaGroup
        ): WhereDSL {
            val initialCriterion = withColumn(column)
                .withCondition(condition)
                .build()

            return where(initialCriterion, *subCriteria)
        }

        @JvmStatic
        fun where(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): WhereDSL {
            return WhereDSL(initialCriterion, listOf(*subCriteria))
        }

        @JvmStatic
        fun where(existsPredicate: ExistsPredicate, vararg subCriteria: AndOrCriteriaGroup): WhereDSL {
            val existsCriterion = ExistsCriterion.Builder()
                .withExistsPredicate(existsPredicate).build()
            return where(existsCriterion, *subCriteria)
        }
        @JvmStatic
        fun <T> having(
            column: BindableColumn<T>, condition: RenderableCondition<T>,
            vararg subCriteria: AndOrCriteriaGroup
        ): HavingDSL {
            val initialCriterion: SqlCriterion = withColumn(column).withCondition(condition).build()
            return having(initialCriterion, *subCriteria)
        }

        @JvmStatic
        fun having(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): HavingDSL {
            return HavingDSL(initialCriterion, listOf(*subCriteria))
        }
        @JvmStatic
        // where condition connectors
        fun <T> group(
            column: BindableColumn<T>, condition: RenderableCondition<T>,
            vararg subCriteria: AndOrCriteriaGroup
        ): CriteriaGroup {
            return group(column, condition, listOf(*subCriteria))
        }
        @JvmStatic
        fun <T> group(
            column: BindableColumn<T>, condition: RenderableCondition<T>,
            subCriteria: List<AndOrCriteriaGroup>
        ): CriteriaGroup {
            return CriteriaGroup.Builder()
                .withInitialCriterion(
                    ColumnAndConditionCriterion.Builder<T>().withColumn(column)
                        .withCondition(condition).build()
                )
                .withSubCriteria(subCriteria)
                .build()
        }

        @JvmStatic
        fun group(existsPredicate: ExistsPredicate, vararg subCriteria: AndOrCriteriaGroup): CriteriaGroup {
            return group(existsPredicate, listOf(*subCriteria))
        }
        @JvmStatic
        fun group(existsPredicate: ExistsPredicate, subCriteria: List<AndOrCriteriaGroup>): CriteriaGroup {
            return CriteriaGroup.Builder()
                .withInitialCriterion(
                    ExistsCriterion.Builder()
                        .withExistsPredicate(existsPredicate).build()
                )
                .withSubCriteria(subCriteria)
                .build()
        }

        @JvmStatic
        fun group(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): CriteriaGroup {
            return group(initialCriterion, listOf(*subCriteria))
        }
        @JvmStatic
        fun group(initialCriterion: SqlCriterion, subCriteria: List<AndOrCriteriaGroup>): CriteriaGroup {
            return CriteriaGroup.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build()
        }
        @JvmStatic
        fun group(subCriteria: MutableList<AndOrCriteriaGroup>): CriteriaGroup {
            return CriteriaGroup.Builder()
                .withInitialCriterion(NullCriterion())
                .withSubCriteria(subCriteria)
                .build()
        }
        @JvmStatic
        fun <T> not(
            column: BindableColumn<T>, condition: RenderableCondition<T>,
            vararg subCriteria: AndOrCriteriaGroup
        ): NotCriterion {
            return not(column, condition, listOf(*subCriteria))
        }
        @JvmStatic
        fun <T> not(
            column: BindableColumn<T>, condition: RenderableCondition<T>,
            subCriteria: List<AndOrCriteriaGroup>
        ): NotCriterion {
            return NotCriterion.Builder()
                .withInitialCriterion(
                    ColumnAndConditionCriterion.Builder<T>().withColumn(column)
                        .withCondition(condition).build()
                )
                .withSubCriteria(subCriteria)
                .build()
        }

        @JvmStatic
        fun not(existsPredicate: ExistsPredicate, vararg subCriteria: AndOrCriteriaGroup): NotCriterion {
            return not(existsPredicate, listOf(*subCriteria))
        }
        @JvmStatic
        fun not(existsPredicate: ExistsPredicate, subCriteria: List<AndOrCriteriaGroup>): NotCriterion {
            return NotCriterion.Builder()
                .withInitialCriterion(
                    ExistsCriterion.Builder()
                        .withExistsPredicate(existsPredicate).build()
                )
                .withSubCriteria(subCriteria)
                .build()
        }

        @JvmStatic
        fun not(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): NotCriterion {
            return not(initialCriterion, listOf(*subCriteria))
        }
        @JvmStatic
        fun not(initialCriterion: SqlCriterion, subCriteria: List<AndOrCriteriaGroup>): NotCriterion {
            return NotCriterion.Builder()
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(subCriteria)
                .build()
        }
        @JvmStatic
        fun not(subCriteria: List<AndOrCriteriaGroup>): NotCriterion {
            return NotCriterion.Builder()
                .withInitialCriterion(NullCriterion())
                .withSubCriteria(subCriteria)
                .build()
        }
        @JvmStatic
        fun <T> or(
            column: BindableColumn<T>, condition: RenderableCondition<T>,
            vararg subCriteria: AndOrCriteriaGroup
        ): AndOrCriteriaGroup {
            return AndOrCriteriaGroup.Builder()
                .withInitialCriterion(
                    withColumn(column)
                        .withCondition(condition)
                        .build()
                )
                .withConnector("or") //$NON-NLS-1$
                .withSubCriteria(Arrays.asList(*subCriteria))
                .build()
        }

        @JvmStatic
        fun or(existsPredicate: ExistsPredicate, vararg subCriteria: AndOrCriteriaGroup): AndOrCriteriaGroup {
            return AndOrCriteriaGroup.Builder()
                .withInitialCriterion(
                    ExistsCriterion.Builder()
                        .withExistsPredicate(existsPredicate).build()
                )
                .withConnector("or") //$NON-NLS-1$
                .withSubCriteria(listOf(*subCriteria))
                .build()
        }

        @JvmStatic
        fun or(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): AndOrCriteriaGroup {
            return AndOrCriteriaGroup.Builder()
                .withConnector("or") //$NON-NLS-1$
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(listOf(*subCriteria))
                .build()
        }
        @JvmStatic
        fun or(subCriteria: List<AndOrCriteriaGroup>): AndOrCriteriaGroup {
            return AndOrCriteriaGroup.Builder()
                .withConnector("or") //$NON-NLS-1$
                .withInitialCriterion(NullCriterion())
                .withSubCriteria(subCriteria)
                .build()
        }
        @JvmStatic
        fun <T> and(
            column: BindableColumn<T>, condition: RenderableCondition<T>,
            vararg subCriteria: AndOrCriteriaGroup
        ): AndOrCriteriaGroup {
            return AndOrCriteriaGroup.Builder()
                .withInitialCriterion(
                    withColumn(column)
                        .withCondition(condition)
                        .build()
                )
                .withConnector("and") //$NON-NLS-1$
                .withSubCriteria(listOf(*subCriteria))
                .build()
        }

        @JvmStatic
        fun and(existsPredicate: ExistsPredicate, vararg subCriteria: AndOrCriteriaGroup): AndOrCriteriaGroup {
            return AndOrCriteriaGroup.Builder()
                .withInitialCriterion(
                    ExistsCriterion.Builder()
                        .withExistsPredicate(existsPredicate).build()
                )
                .withConnector("and")
                .withSubCriteria(listOf(*subCriteria))
                .build()
        }

        @JvmStatic
        fun and(initialCriterion: SqlCriterion, vararg subCriteria: AndOrCriteriaGroup): AndOrCriteriaGroup {
            return AndOrCriteriaGroup.Builder()
                .withConnector("and") //$NON-NLS-1$
                .withInitialCriterion(initialCriterion)
                .withSubCriteria(listOf(*subCriteria))
                .build()
        }
        @JvmStatic
        fun and(subCriteria: List<AndOrCriteriaGroup>): AndOrCriteriaGroup {
            return AndOrCriteriaGroup.Builder()
                .withConnector("and") //$NON-NLS-1$
                .withInitialCriterion(NullCriterion())
                .withSubCriteria(subCriteria)
                .build()
        }
        @JvmStatic
        // join support
        fun <T> on(
            joinColumn: BindableColumn<T>,
            joinCondition: RenderableCondition<T>
        ): ColumnAndConditionCriterion<T> {
            return withColumn<T>(joinColumn)
                .withCondition(joinCondition)
                .build()
        }
        @JvmStatic
        // case expressions
        fun <T> case_(column: BindableColumn<T>): SimpleCaseDSL<T> {
            return simpleCase(column)
        }

        @JvmStatic
        fun case_(): SearchedCaseDSL {
            return searchedCase()
        }

        // aggregate support
        @JvmStatic
        fun count(): CountAll {
            return CountAll()
        }

        @JvmStatic
        fun count(column: BasicColumn): Count {
            return Count.of(column)
        }

        @JvmStatic
        fun countDistinct(column: BasicColumn): CountDistinct {
            return CountDistinct.of(column)
        }
        @JvmStatic
        fun subQuery(subQuery: Buildable<SelectModel?>): SubQueryColumn {
            return SubQueryColumn.of(subQuery.build()!!)
        }
        @JvmStatic
        fun <T> max(column: BindableColumn<T>): Max<T> {
            return Max.of(column)
        }
        @JvmStatic
        fun <T> min(column: BindableColumn<T>): Min<T> {
            return Min.of(column)
        }
        @JvmStatic
        fun <T> avg(column: BindableColumn<T>): Avg<T> {
            return Avg.of(column)
        }
        @JvmStatic
        fun <T> sum(column: BindableColumn<T>): Sum<T> {
            return Sum.of(column)
        }

        @JvmStatic
        fun sum(column: BasicColumn): Sum<Any> {
            return Sum.of(column)
        }
        @JvmStatic
        fun <T> sum(column: BindableColumn<T>, condition: RenderableCondition<T>): Sum<T> {
            return of<T>(column, condition)
        }

        // constants
        @JvmStatic
        fun <T> constant(constant: String): Constant<T> {
            return of<T>(constant)
        }

        @JvmStatic
        fun stringConstant(constant: String): StringConstant {
            return StringConstant.of(constant)
        }
        @JvmStatic
        fun <T> value(value: T): BoundValue<T> {
            return BoundValue.of(value)
        }
        @JvmStatic
        // functions
        fun <T> add(
            firstColumn: BindableColumn<T>, secondColumn: BasicColumn,
            vararg subsequentColumns: BasicColumn
        ): Add<T> {
            return Add.of(firstColumn, secondColumn, *subsequentColumns)
        }
        @JvmStatic
        fun <T> divide(
            firstColumn: BindableColumn<T>, secondColumn: BasicColumn,
            vararg subsequentColumns: BasicColumn
        ): Divide<T> {
            return Divide.of<T>(firstColumn, secondColumn, *subsequentColumns)
        }
        @JvmStatic
        fun <T> multiply(
            firstColumn: BindableColumn<T>, secondColumn: BasicColumn,
            vararg subsequentColumns: BasicColumn
        ): Multiply<T> {
            return Multiply.of(firstColumn, secondColumn, *subsequentColumns)
        }
        @JvmStatic
        fun <T> subtract(
            firstColumn: BindableColumn<T>, secondColumn: BasicColumn,
            vararg subsequentColumns: BasicColumn
        ): Subtract<T> {
            return Subtract.of(firstColumn, secondColumn, *subsequentColumns)
        }

        @JvmStatic
        fun cast(value: String): CastFinisher {
            return cast(stringConstant(value))
        }

        @JvmStatic
        fun cast(value: Double): CastFinisher {
            return cast(constant<Any?>(value.toString()))
        }

        @JvmStatic
        fun cast(column: BasicColumn): CastFinisher {
            return CastFinisher(column)
        }

        /**
         * Concatenate function that renders as "(x || y || z)". This will not work on some
         * databases like MySql. In that case, use [concat]
         *
         * @param firstColumn first column
         * @param secondColumn second column
         * @param subsequentColumns subsequent columns
         * @param <T> type of column
         * @return a Concatenate instance
        </T> */
        @JvmStatic
        fun <T> concatenate(
            firstColumn: BindableColumn<T>, secondColumn: BasicColumn,
            vararg subsequentColumns: BasicColumn
        ): Concatenate<T> {
            return Concatenate.concatenate(firstColumn, secondColumn, *subsequentColumns)
        }

        /**
         * Concatenate function that renders as "concat(x, y, z)". This version works on more databases
         * than [concatenate]
         *
         * @param firstColumn first column
         * @param subsequentColumns subsequent columns
         * @param <T> type of column
         * @return a Concat instance
        </T> */
        @JvmStatic
        fun <T> concat(firstColumn: BindableColumn<T>, vararg subsequentColumns: BasicColumn): Concat<T> {
            return Concat.concat(firstColumn, *subsequentColumns)
        }
        @JvmStatic
        fun <T> applyOperator(
            operator: String, firstColumn: BindableColumn<T>,
            secondColumn: BasicColumn, vararg subsequentColumns: BasicColumn
        ): OperatorFunction<T> {
            return OperatorFunction.of(operator, firstColumn, secondColumn, *subsequentColumns)
        }
        @JvmStatic
        fun <T> lower(column: BindableColumn<T>): Lower<T> {
            return Lower.of(column)
        }
        @JvmStatic
        fun <T> substring(column: BindableColumn<T>, offset: Int, length: Int): Substring<T> {
            return of(column, offset, length)
        }
        @JvmStatic
        fun <T> upper(column: BindableColumn<T>): Upper<T> {
            return Upper.of(column)
        }
        @JvmStatic
        // conditions for all data types
        fun exists(selectModelBuilder: Buildable<SelectModel>): ExistsPredicate {
            return ExistsPredicate.exists(selectModelBuilder)
        }
        @JvmStatic
        fun notExists(selectModelBuilder: Buildable<SelectModel>): ExistsPredicate {
            return ExistsPredicate.notExists(selectModelBuilder)
        }

        @JvmStatic
        fun <T> isNull(): IsNull<T> {
            return IsNull()
        }

        @JvmStatic
        fun <T> isNotNull(): IsNotNull<T> {
            return IsNotNull()
        }
        @JvmStatic
        fun <T> isEqualTo(value: T): IsEqualTo<T> {
            return IsEqualTo.of(value)
        }
        @JvmStatic
        fun <T> isEqualTo(valueSupplier: Supplier<T>): IsEqualTo<T> {
            return isEqualTo(valueSupplier.get())
        }
        @JvmStatic
        fun <T> isEqualTo(selectModelBuilder: Buildable<SelectModel>): IsEqualToWithSubselect<T> {
            return IsEqualToWithSubselect.of(selectModelBuilder)
        }

        @JvmStatic
        fun <T> isEqualTo(column: BasicColumn): IsEqualToColumn<T> {
            return IsEqualToColumn.of(column)
        }
        @JvmStatic
        fun <T> isEqualToWhenPresent(value: T?): IsEqualToWhenPresent<T> {
            return IsEqualToWhenPresent.of(value)
        }
        @JvmStatic
        fun <T> isEqualToWhenPresent(valueSupplier: Supplier<T>): IsEqualToWhenPresent<T> {
            return isEqualToWhenPresent(valueSupplier.get())
        }
        @JvmStatic
        fun <T> isNotEqualTo(value: T): IsNotEqualTo<T> {
            return IsNotEqualTo.of(value)
        }
        @JvmStatic
        fun <T> isNotEqualTo(valueSupplier: Supplier<T>): IsNotEqualTo<T> {
            return isNotEqualTo<T>(valueSupplier.get())
        }
        @JvmStatic
        fun <T> isNotEqualTo(selectModelBuilder: Buildable<SelectModel>): IsNotEqualToWithSubselect<T> {
            return IsNotEqualToWithSubselect.of(selectModelBuilder)
        }
        @JvmStatic
        fun <T> isNotEqualTo(column: BasicColumn): IsNotEqualToColumn<T> {
            return IsNotEqualToColumn.of(column)
        }
        @JvmStatic
        fun <T> isNotEqualToWhenPresent(value: T?): IsNotEqualToWhenPresent<T> {
            return IsNotEqualToWhenPresent.of(value)
        }
        @JvmStatic
        fun <T> isNotEqualToWhenPresent(valueSupplier: Supplier<T>): IsNotEqualToWhenPresent<T> {
            return isNotEqualToWhenPresent(valueSupplier.get())
        }
        @JvmStatic
        fun <T> isGreaterThan(value: T): IsGreaterThan<T> {
            return IsGreaterThan.of<T>(value)
        }
        @JvmStatic
        fun <T> isGreaterThan(valueSupplier: Supplier<T>): IsGreaterThan<T> {
            return isGreaterThan(valueSupplier.get())
        }
        @JvmStatic
        fun <T> isGreaterThan(selectModelBuilder: Buildable<SelectModel>): IsGreaterThanWithSubselect<T> {
            return IsGreaterThanWithSubselect.of<T>(selectModelBuilder)
        }

        @JvmStatic
        fun <T> isGreaterThan(column: BasicColumn): IsGreaterThanColumn<T> {
            return IsGreaterThanColumn.of(column)
        }
        @JvmStatic
        fun <T> isGreaterThanWhenPresent(value: T?): IsGreaterThanWhenPresent<T> {
            return IsGreaterThanWhenPresent.of(value)
        }
        @JvmStatic
        fun <T> isGreaterThanWhenPresent(valueSupplier: Supplier<T>): IsGreaterThanWhenPresent<T> {
            return isGreaterThanWhenPresent(valueSupplier.get())
        }
        @JvmStatic
        fun <T> isGreaterThanOrEqualTo(value: T): IsGreaterThanOrEqualTo<T> {
            return IsGreaterThanOrEqualTo.of(value)
        }
        @JvmStatic
        fun <T> isGreaterThanOrEqualTo(valueSupplier: Supplier<T>): IsGreaterThanOrEqualTo<T> {
            return isGreaterThanOrEqualTo(valueSupplier.get())
        }
        @JvmStatic
        fun <T> isGreaterThanOrEqualTo(
            selectModelBuilder: Buildable<SelectModel>
        ): IsGreaterThanOrEqualToWithSubselect<T> {
            return IsGreaterThanOrEqualToWithSubselect.of<T>(selectModelBuilder)
        }
        @JvmStatic
        fun <T> isGreaterThanOrEqualTo(column: BasicColumn): IsGreaterThanOrEqualToColumn<T> {
            return IsGreaterThanOrEqualToColumn.of(column)
        }
        @JvmStatic
        fun <T> isGreaterThanOrEqualToWhenPresent(value: T?): IsGreaterThanOrEqualToWhenPresent<T> {
            return IsGreaterThanOrEqualToWhenPresent.of(value)
        }
        @JvmStatic
        fun <T> isGreaterThanOrEqualToWhenPresent(
            valueSupplier: Supplier<T>
        ): IsGreaterThanOrEqualToWhenPresent<T> {
            return isGreaterThanOrEqualToWhenPresent<T>(valueSupplier.get())
        }
        @JvmStatic
        fun <T> isLessThan(value: T): IsLessThan<T> {
            return IsLessThan.of(value)
        }
        @JvmStatic
        fun <T> isLessThan(valueSupplier: Supplier<T>): IsLessThan<T> {
            return isLessThan(valueSupplier.get())
        }
        @JvmStatic
        fun <T> isLessThan(selectModelBuilder: Buildable<SelectModel>): IsLessThanWithSubselect<T> {
            return IsLessThanWithSubselect.of(selectModelBuilder)
        }

        @JvmStatic
        fun <T> isLessThan(column: BasicColumn): IsLessThanColumn<T> {
            return IsLessThanColumn.of(column)
        }
        @JvmStatic
        fun <T> isLessThanWhenPresent(value: T?): IsLessThanWhenPresent<T> {
            return IsLessThanWhenPresent.of(value)
        }
        @JvmStatic
        fun <T> isLessThanWhenPresent(valueSupplier: Supplier<T>): IsLessThanWhenPresent<T> {
            return isLessThanWhenPresent(valueSupplier.get())
        }
        @JvmStatic
        fun <T> isLessThanOrEqualTo(value: T): IsLessThanOrEqualTo<T> {
            return IsLessThanOrEqualTo.of(value)
        }
        @JvmStatic
        fun <T> isLessThanOrEqualTo(valueSupplier: Supplier<T>): IsLessThanOrEqualTo<T> {
            return isLessThanOrEqualTo(valueSupplier.get())
        }
        @JvmStatic
        fun <T> isLessThanOrEqualTo(selectModelBuilder: Buildable<SelectModel>): IsLessThanOrEqualToWithSubselect<T> {
            return IsLessThanOrEqualToWithSubselect.of<T>(selectModelBuilder)
        }
        @JvmStatic
        fun <T> isLessThanOrEqualTo(column: BasicColumn): IsLessThanOrEqualToColumn<T> {
            return IsLessThanOrEqualToColumn.of(column)
        }
        @JvmStatic
        fun <T> isLessThanOrEqualToWhenPresent(value: T?): IsLessThanOrEqualToWhenPresent<T> {
            return IsLessThanOrEqualToWhenPresent.of(value)
        }
        @JvmStatic
        fun <T> isLessThanOrEqualToWhenPresent(valueSupplier: Supplier<T>): IsLessThanOrEqualToWhenPresent<T> {
            return isLessThanOrEqualToWhenPresent(valueSupplier.get())
        }
        @JvmStatic
        @SafeVarargs
        fun <T> isIn(vararg values: T): IsIn<T> {
            return IsIn.of(*values)
        }
        @JvmStatic
        fun <T> isIn(values: Collection<T>): IsIn<T> {
            return IsIn.of(values)
        }
        @JvmStatic
        fun <T> isIn(selectModelBuilder: Buildable<SelectModel>): IsInWithSubselect<T> {
            return IsInWithSubselect.of(selectModelBuilder)
        }

        @JvmStatic
        @SafeVarargs
        fun <T> isInWhenPresent(vararg values: T?): IsInWhenPresent<T> {
            return IsInWhenPresent.of(*values)
        }
        @JvmStatic
        fun <T> isInWhenPresent(values: Collection<T?>?): IsInWhenPresent<T> {
            return IsInWhenPresent.of(values)
        }
        @JvmStatic
        fun <T> isNotIn(vararg values: T): IsNotIn<T> {
            return IsNotIn.of(*values)
        }
        @JvmStatic
        fun <T> isNotIn(values: Collection<T>): IsNotIn<T> {
            return IsNotIn.of(values)
        }
        @JvmStatic
        fun <T> isNotIn(selectModelBuilder: Buildable<SelectModel>): IsNotInWithSubselect<T> {
            return IsNotInWithSubselect.of<T>(selectModelBuilder)
        }
        @JvmStatic
        fun <T> isNotInWhenPresent(vararg values: T?): IsNotInWhenPresent<T> {
            return IsNotInWhenPresent.of(*values)
        }
        @JvmStatic
        fun <T> isNotInWhenPresent(values: Collection<T?>?): IsNotInWhenPresent<T> {
            return IsNotInWhenPresent.of(values)
        }
        @JvmStatic
        fun <T> isBetween(value1: T): IsBetween.Builder<T> {
            return IsBetween.isBetween(value1)
        }
        @JvmStatic
        fun <T> isBetween(valueSupplier1: Supplier<T>): IsBetween.Builder<T> {
            return isBetween(valueSupplier1.get())
        }
        @JvmStatic
        fun <T> isBetweenWhenPresent(value1: T?): IsBetweenWhenPresent.Builder<T> {
            return IsBetweenWhenPresent.isBetweenWhenPresent<T>(value1)
        }
        @JvmStatic
        fun <T> isBetweenWhenPresent(valueSupplier1: Supplier<T>): IsBetweenWhenPresent.Builder<T> {
            return isBetweenWhenPresent(valueSupplier1.get())
        }
        @JvmStatic
        fun <T> isNotBetween(value1: T): IsNotBetween.Builder<T> {
            return IsNotBetween.isNotBetween(value1)
        }
        @JvmStatic
        fun <T> isNotBetween(valueSupplier1: Supplier<T>): IsNotBetween.Builder<T> {
            return isNotBetween(valueSupplier1.get())
        }
        @JvmStatic
        fun <T> isNotBetweenWhenPresent(value1: T?): IsNotBetweenWhenPresent.Builder<T> {
            return IsNotBetweenWhenPresent.isNotBetweenWhenPresent(value1)
        }
        @JvmStatic
        fun <T> isNotBetweenWhenPresent(valueSupplier1: Supplier<T>): IsNotBetweenWhenPresent.Builder<T> {
            return isNotBetweenWhenPresent(valueSupplier1.get())
        }
        @JvmStatic
        // for string columns, but generic for columns with type handlers
        fun <T> isLike(value: T): IsLike<T> {
            return IsLike.of(value)
        }
        @JvmStatic
        fun <T> isLike(valueSupplier: Supplier<T>): IsLike<T> {
            return isLike(valueSupplier.get())
        }
        @JvmStatic
        fun <T> isLikeWhenPresent(value: T?): IsLikeWhenPresent<T> {
            return IsLikeWhenPresent.of(value)
        }
        @JvmStatic
        fun <T> isLikeWhenPresent(valueSupplier: Supplier<T>): IsLikeWhenPresent<T> {
            return isLikeWhenPresent(valueSupplier.get())
        }
        @JvmStatic
        fun <T> isNotLike(value: T): IsNotLike<T> {
            return IsNotLike.of(value)
        }
        @JvmStatic
        fun <T> isNotLike(valueSupplier: Supplier<T>): IsNotLike<T> {
            return isNotLike(valueSupplier.get())
        }
        @JvmStatic
        fun <T> isNotLikeWhenPresent(value: T?): IsNotLikeWhenPresent<T> {
            return IsNotLikeWhenPresent.of(value)
        }
        @JvmStatic
        fun <T> isNotLikeWhenPresent(valueSupplier: Supplier<T>): IsNotLikeWhenPresent<T> {
            return isNotLikeWhenPresent(valueSupplier.get())
        }

        @JvmStatic
        val isTrue: IsEqualTo<Boolean>
            // shortcuts for booleans
            get() = isEqualTo(java.lang.Boolean.TRUE)

        @JvmStatic
        val isFalse: IsEqualTo<Boolean>
            get() = isEqualTo(java.lang.Boolean.FALSE)

        // conditions for strings only
        @JvmStatic
        fun isLikeCaseInsensitive(value: String): IsLikeCaseInsensitive<String> {
            return IsLikeCaseInsensitive.of<String>(value)
        }
        @JvmStatic
        fun isLikeCaseInsensitive(valueSupplier: Supplier<String?>): IsLikeCaseInsensitive<String> {
            return Companion.isLikeCaseInsensitive(valueSupplier.get()!!)
        }

        @JvmStatic
        fun isLikeCaseInsensitiveWhenPresent(value: String?): IsLikeCaseInsensitiveWhenPresent<String> {
            return IsLikeCaseInsensitiveWhenPresent.of<String>(value)
        }
        @JvmStatic
        fun isLikeCaseInsensitiveWhenPresent(
            valueSupplier: Supplier<String?>
        ): IsLikeCaseInsensitiveWhenPresent<String> {
            return isLikeCaseInsensitiveWhenPresent(valueSupplier.get())
        }

        @JvmStatic
        fun isNotLikeCaseInsensitive(value: String): IsNotLikeCaseInsensitive<String> {
            return IsNotLikeCaseInsensitive.of<String>(value)
        }
        @JvmStatic
        fun isNotLikeCaseInsensitive(valueSupplier: Supplier<String?>): IsNotLikeCaseInsensitive<String> {
            return Companion.isNotLikeCaseInsensitive(valueSupplier.get()!!)
        }

        @JvmStatic
        fun isNotLikeCaseInsensitiveWhenPresent(value: String?): IsNotLikeCaseInsensitiveWhenPresent<String> {
            return IsNotLikeCaseInsensitiveWhenPresent.of<String>(value)
        }
        @JvmStatic
        fun isNotLikeCaseInsensitiveWhenPresent(
            valueSupplier: Supplier<String?>
        ): IsNotLikeCaseInsensitiveWhenPresent<String> {
            return isNotLikeCaseInsensitiveWhenPresent(valueSupplier.get())
        }

        @JvmStatic
        fun isInCaseInsensitive(vararg values: String): IsInCaseInsensitive<String> {
            return IsInCaseInsensitive.of(*values)
        }
        @JvmStatic
        fun isInCaseInsensitive(values: Collection<String>): IsInCaseInsensitive<String> {
            return IsInCaseInsensitive.of(values)
        }

        @JvmStatic
        fun isInCaseInsensitiveWhenPresent(vararg values: String?): IsInCaseInsensitiveWhenPresent<String> {
            return IsInCaseInsensitiveWhenPresent.of(*values)
        }
        @JvmStatic
        fun isInCaseInsensitiveWhenPresent(
            values: Collection<String?>?
        ): IsInCaseInsensitiveWhenPresent<String> {
            return IsInCaseInsensitiveWhenPresent.of(values)
        }

        @JvmStatic
        fun isNotInCaseInsensitive(vararg values: String): IsNotInCaseInsensitive<String> {
            return IsNotInCaseInsensitive.of(*values)
        }
        @JvmStatic
        fun isNotInCaseInsensitive(values: Collection<String>): IsNotInCaseInsensitive<String> {
            return IsNotInCaseInsensitive.of(values)
        }

        @JvmStatic
        fun isNotInCaseInsensitiveWhenPresent(vararg values: String?): IsNotInCaseInsensitiveWhenPresent<String> {
            return IsNotInCaseInsensitiveWhenPresent.of(*values)
        }
        @JvmStatic
        fun isNotInCaseInsensitiveWhenPresent(
            values: Collection<String?>?
        ): IsNotInCaseInsensitiveWhenPresent<String> {
            return IsNotInCaseInsensitiveWhenPresent.of(values)
        }

        // order by support
        /**
         * Creates a sort specification based on a String. This is useful when a column has been
         * aliased in the select list. For example:
         *
         * <pre>
         * select(foo.as("bar"))
         * .from(baz)
         * .orderBy(sortColumn("bar"))
        </pre> *
         *
         * @param name the string to use as a sort specification
         * @return a sort specification
         */
        @JvmStatic
        fun sortColumn(name: String): SortSpecification {
            return SimpleSortSpecification.of(name)
        }

        /**
         * Creates a sort specification based on a column and a table alias. This can be useful in a join
         * where the desired sort order is based on a column not in the select list. This will likely
         * fail in union queries depending on database support.
         *
         * @param tableAlias the table alias
         * @param column the column
         * @return a sort specification
         */
        @JvmStatic
        fun sortColumn(tableAlias: String, column: SqlColumn<*>): SortSpecification {
            return ColumnSortSpecification(tableAlias, column)
        }
    }
}
