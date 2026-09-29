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

import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import org.mybatis.dynamic.sql.util.StringUtilities
import java.sql.JDBCType
import java.util.*

/**
 * This class represents the definition of a column in a table.
 *
 *
 * The class contains many attributes that are helpful for use in MyBatis and Spring runtime
 * environments, but the only required attributes are the name of the column and a reference to
 * the [SqlTable] the column is a part of.
 *
 *
 * The class can be extended if you wish to associate additional attributes with a column for your
 * own purposes. Extending the class is a bit more challenging than you might expect because you may need to
 * handle the covariant types for many methods in `SqlColumn`. Additionally, many methods in `SqlColumn`
 * create new instances of the class in keeping with the library's primary strategy of immutability. You will also
 * need to ensure that these methods create instances of your extended class, rather than the base `SqlColumn`
 * class. We have worked to keep this process as simple as possible.
 *
 *
 * Extending the class involves the following activities:
 *
 *  1. Create a class that extends [SqlColumn]
 *  1. In your extended class, create a static builder class that extends [AbstractBuilder]
 *  1. Add your desired attributes to the class and the builder
 *  1. You MUST override the [copyBuilder] method and return a new instance of
 * your builder with all attributes set. In the overridden method you should call the superclass
 * [populateBaseBuilder] method
 * to set the attributes from the base `SqlColumn`, then populate your extended attributes. During normal
 * usage, the library may create additional instances of your class. If you do not override the
 * [copyBuilder] method properly, then your extended attributes will be lost.
 *
 *  1. You MAY override the following methods. These methods are used with regular operations in the library and
 * create new instances of the class. However, these methods are not typically chained, so losing the specific
 * type may not be a problem. If you want to preserve the type, then you can override these methods
 * to specify the covariant return type. See below for usage of the [cast] method
 * to make it easier to override these methods.
 *
 *  * [SqlColumn. as]
 *  * [asCamelCase]
 *  * [descending]
 *  * [qualifiedWith]
 *
 *
 *  1. You SHOULD override the following methods. These methods can be used to add additional attributes to a
 * column by creating a new instance with a specified attribute set. These methods are used during the
 * construction of columns. If you do not override these methods, and a user calls them, then the specific type
 * will be lost. If you want to preserve the type, then you can override these methods
 * to specify the covariant return type. See below for usage of the [cast] method
 * to make it easier to override these methods.
 *
 *  * [withJavaProperty]
 *  * [withRenderingStrategy]
 *  * [withTypeHandler]
 *  * [withJavaType]
 *  * [withParameterTypeConverter]
 *
 *
 *
 *
 *
 * For all overridden methods except `copyBuilder()`, the process is to call the superclass
 * method and cast the result properly. We provide a [cast] method to aid with this
 * process. For example, overriding the `descending` method could look like this:
 *
 * <pre>
 * `publicMyExtendedColumn<T> descending() {     return cast(super.descending()); } `
</pre> *
 *
 *
 * The test code for this library contains an example of a fully executed extension of this class.
 *
 * @param <T> the Java type associated with the column
</T> */
open class SqlColumn<T> protected constructor(builder: AbstractBuilder<T, *>) : BindableColumn<T>, SortSpecification {

    protected val name: String
    protected val table: SqlTable
    protected val jdbcType: JDBCType?
    protected val descendingPhrase: String
    protected val alias: String?
    protected val typeHandler: String?
    protected val renderingStrategy: RenderingStrategy?
    protected val parameterTypeConverter: ParameterTypeConverter<T, *>
    protected val tableQualifier: String?
    protected val javaType: Class<T>?
    protected val javaProperty: String?

    init {
        name = builder.name
        table = builder.table
        jdbcType = builder.jdbcType
        descendingPhrase = builder.descendingPhrase
        alias = builder.alias
        typeHandler = builder.typeHandler
        renderingStrategy = builder.renderingStrategy
        parameterTypeConverter = Objects.requireNonNull(builder.parameterTypeConverter)!!
        tableQualifier = builder.tableQualifier
        javaType = builder.javaType
        javaProperty = builder.javaProperty
    }

    fun name(): String {
        return name
    }

    fun table(): SqlTable {
        return table
    }

    override fun jdbcType(): JDBCType? {
        return jdbcType
    }

    override fun alias(): String? {
        return alias
    }

    override fun typeHandler(): String? {
        return typeHandler
    }



    override fun javaType(): Class<T>? {
        return javaType
    }

    fun javaProperty(): Optional<String> {
        return Optional.ofNullable(javaProperty)
    }

    override fun convertParameterType(value: T?): Any? {
        return if (value == null) null else parameterTypeConverter.convert(value)
    }

    /**
     * Create a new column instance that will render as descending when used in an order by phrase.
     *
     * @return a new column instance that will render as descending when used in an order by phrase
     */
    override fun descending(): SqlColumn<T> {
        return copyBuilder().withDescendingPhrase(" DESC").build() //$NON-NLS-1$
    }

    /**
     * Create a new column instance with the specified alias that will render as "as alias" in a column list.
     *
     * @param alias
     * the column alias to set
     *
     * @return a new column instance with the specified alias
     */
    override fun `as`(alias: String): SqlColumn<T> {
        return copyBuilder().withAlias(alias).build()
    }

    /**
     * Override the calculated table qualifier if there is one. This is useful for sub-queries
     * where the calculated table qualifier may not be correct in all cases.
     *
     * @param tableQualifier the table qualifier to apply to the rendered column name
     * @return a new column that will be rendered with the specified table qualifier
     */
    open fun qualifiedWith(tableQualifier: String?): SqlColumn<T> {
        return copyBuilder().withTableQualifier(tableQualifier).build()
    }

    /**
     * Set an alias with a camel-cased string based on the column name. This can be useful for queries using
     * the [org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper] where the columns are placed into
     * a map based on the column name returned from the database.
     *
     *
     * A camel case string is a mixed case string, and most databases do not support unquoted mixed case strings
     * as identifiers. Therefore, the generated alias will be surrounded by double quotes thereby making it a
     * quoted identifier. Most databases will respect quoted mixed case identifiers.
     *
     * @return a new column aliased with a camel case version of the column name
     */
    open fun asCamelCase(): SqlColumn<T> {
        return copyBuilder()
            .withAlias("\"" + StringUtilities.toCamelCase(name) + "\"")
            .build()
    }

    override fun renderForOrderBy(renderingContext: RenderingContext): FragmentAndParameters {
        return FragmentAndParameters.fromFragment((alias() ?: name) + descendingPhrase)
    }

    override fun render(renderingContext: RenderingContext): FragmentAndParameters {
        if (tableQualifier == null) {
            return FragmentAndParameters.fromFragment(renderingContext.aliasedColumnName<T>(this))
        } else {
            return FragmentAndParameters.fromFragment(renderingContext.aliasedColumnName<T>(this, tableQualifier))
        }
    }

    override fun renderingStrategy(): RenderingStrategy? {
        return renderingStrategy
    }

    /**
     * Create a new column instance with the specified type handler.
     *
     *
     * This method uses a different type (S). This allows it to be chained with the other
     * with* methods. Using new types forces the compiler to delay type inference until the end of a call chain.
     * Without this different type (for example, if we used T), the compiler would erase the type after the call
     * and method chaining would not work. This is a workaround for Java's lack of reification.
     *
     * @param typeHandler the type handler to set
     * @param <S> the type of the new column (will be the same as T)
     * @return a new column instance with the specified type handler
    </S> */
    open fun <S : Any> withTypeHandler(typeHandler: String?): SqlColumn<S> {
        return cast<SqlColumn<S>>(copyBuilder().withTypeHandler(typeHandler).build())
    }

    /**
     * Create a new column instance with the specified rendering strategy.
     *
     *
     * This method uses a different type (S). This allows it to be chained with the other
     * with* methods. Using new types forces the compiler to delay type inference until the end of a call chain.
     * Without this different type (for example, if we used T), the compiler would erase the type after the call
     * and method chaining would not work. This is a workaround for Java's lack of reification.
     *
     * @param renderingStrategy the rendering strategy to set
     * @param <S> the type of the new column (will be the same as T)
     * @return a new column instance with the specified type handler
    </S> */
    open fun <S : Any> withRenderingStrategy(renderingStrategy: RenderingStrategy?): SqlColumn<S> {
        return cast<SqlColumn<S>>(copyBuilder().withRenderingStrategy(renderingStrategy).build())
    }

    /**
     * Create a new column instance with the specified parameter type converter.
     *
     *
     * Parameter type converters are useful with Spring JDBC. Typically, they are not needed for MyBatis.
     *
     *
     * This method uses a different type (S). This allows it to be chained with the other
     * with* methods. Using new types forces the compiler to delay type inference until the end of a call chain.
     * Without this different type (for example, if we used T), the compiler would erase the type after the call
     * and method chaining would not work. This is a workaround for Java's lack of reification.
     *
     * @param parameterTypeConverter the parameter type converter to set
     * @param <S> the type of the new column (will be the same as T)
     * @return a new column instance with the specified type handler
    </S> */
    open fun <S : Any> withParameterTypeConverter(parameterTypeConverter: ParameterTypeConverter<S, *>?): SqlColumn<S> {
        return cast<SqlColumn<S>>(
            copyBuilder().withParameterTypeConverter(parameterTypeConverter as ParameterTypeConverter<T, *>)
                .build()
        )
    }

    /**
     * Create a new column instance with the specified Java type.
     *
     *
     * Specifying a Java type will force rendering of the Java type for MyBatis parameters. This can be useful
     * with some MyBatis type handlers.
     *
     *
     * This method uses a different type (S). This allows it to be chained with the other
     * with* methods. Using new types forces the compiler to delay type inference until the end of a call chain.
     * Without this different type (for example, if we used T), the compiler would erase the type after the call
     * and method chaining would not work. This is a workaround for Java's lack of reification.
     *
     * @param javaType the Java type to set
     * @param <S> the type of the new column (will be the same as T)
     * @return a new column instance with the specified type handler
    </S> */
    open fun <S> withJavaType(javaType: Class<S>): SqlColumn<S> {
        return cast(copyBuilder().withJavaType(javaType as Class<T>).build())
    }

    /**
     * Create a new column instance with the specified Java property.
     *
     *
     * Specifying a Java property in the column will allow usage of the column as a "mapped column" in record-based
     * insert statements.
     *
     *
     * This method uses a different type (S). This allows it to be chained with the other
     * with* methods. Using new types forces the compiler to delay type inference until the end of a call chain.
     * Without this different type (for example, if we used T), the compiler would erase the type after the call
     * and method chaining would not work. This is a workaround for Java's lack of reification.
     *
     * @param javaProperty the Java property to set
     * @param <S> the type of the new column (will be the same as T)
     * @return a new column instance with the specified type handler
    </S> */
    open fun <S> withJavaProperty(javaProperty: String?): SqlColumn<S> {
        return cast<SqlColumn<S>>(copyBuilder().withJavaProperty(javaProperty).build())
    }

    /**
     * Create a new Builder, then populate all attributes in the builder with current values.
     *
     *
     * This method is used to create copies of the class during normal operations (e.g. when calling the
     * [SqlColumn. as] method). Any subclass of `SqlColumn` MUST override this method.
     *
     * @return a new Builder instance with all current values populated
     */
    protected open fun copyBuilder(): AbstractBuilder<T, *> {
        return populateBaseBuilder(Builder())
    }

    protected fun <S : SqlColumn<*>> cast(column: SqlColumn<*>): S {
        return column as S
    }

    /**
     * This method will add all current attributes to the specified builder. It is useful when creating
     * new class instances that only change one attribute - we set all current attributes, then
     * change the one attribute. This utility can be used with the with* methods and other methods that
     * create new instances.
     *
     * @param <B> the concrete builder type
     * @return the populated builder
    </B> */
    protected fun <B : AbstractBuilder<T, B>> populateBaseBuilder(builder: B): B {
        return builder
            .withName(this.name)
            .withTable(this.table)
            .withJdbcType(this.jdbcType)
            .withDescendingPhrase(this.descendingPhrase)
            .withAlias(this.alias)
            .withTypeHandler(this.typeHandler)
            .withRenderingStrategy(this.renderingStrategy)
            .withParameterTypeConverter(this.parameterTypeConverter)
            .withTableQualifier(this.tableQualifier)
            .withJavaType(this.javaType)
            .withJavaProperty(this.javaProperty)
    }


    abstract class AbstractBuilder<T, B : AbstractBuilder<T, B>> {

        lateinit var name: String
        lateinit var table: SqlTable
        var jdbcType: JDBCType? = null
        var descendingPhrase: String = "" //$NON-NLS-1$
        var alias: String? = null
        var typeHandler: String? = null
        var renderingStrategy: RenderingStrategy? = null
        var parameterTypeConverter: ParameterTypeConverter<T, *> = ParameterTypeConverter { it }
        var tableQualifier: String? = null
        var javaType: Class<T>? = null
        var javaProperty: String? = null

        fun withName(name: String): B {
            this.name = name
            return self()
        }

        fun withTable(table: SqlTable): B {
            this.table = table
            return self()
        }

        fun withJdbcType(jdbcType: JDBCType?): B {
            this.jdbcType = jdbcType
            return self()
        }

        fun withDescendingPhrase(descendingPhrase: String): B {
            this.descendingPhrase = descendingPhrase
            return self()
        }

        fun withAlias(alias: String?): B {
            this.alias = alias
            return self()
        }

        fun withTypeHandler(typeHandler: String?): B {
            this.typeHandler = typeHandler
            return self()
        }

        fun withRenderingStrategy(renderingStrategy: RenderingStrategy?): B {
            this.renderingStrategy = renderingStrategy
            return self()
        }

        fun withParameterTypeConverter(parameterTypeConverter: ParameterTypeConverter<T, *>): B {
            this.parameterTypeConverter = parameterTypeConverter
            return self()
        }

        fun withTableQualifier(tableQualifier: String?): B {
            this.tableQualifier = tableQualifier
            return self()
        }

        fun withJavaType(javaType: Class<T>?): B {
            this.javaType = javaType
            return self()
        }

        fun withJavaProperty(javaProperty: String?): B {
            this.javaProperty = javaProperty
            return self()
        }

        protected abstract fun self() : B

        abstract fun build(): SqlColumn<T>
    }

    class Builder<T> : AbstractBuilder<T, Builder<T>>() {

        override fun build(): SqlColumn<T> {
            return SqlColumn(this)
        }

        override fun self(): Builder<T> {
            return this
        }

    }

    companion object {

        @JvmStatic
        fun <T> of(name: String, table: SqlTable): SqlColumn<T> {
            return Builder<T>().withName(name).withTable(table).build()
        }

        @JvmStatic
        fun <T> of(name: String, table: SqlTable, jdbcType: JDBCType?): SqlColumn<T> {
            return Builder<T>().withName(name).withTable(table).withJdbcType(jdbcType).build()
        }

    }
}
