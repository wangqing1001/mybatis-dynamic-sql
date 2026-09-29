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
package org.mybatis.dynamic.sql.util

/**
 * 所有列映射的访问器。insert 和 update 语句使用各种列映射。
 * 只有 null 和 constant 映射被所有语句支持。其他映射可能受支持,也可能不受支持。
 * 例如,在 insert 中将一列映射到另一列没有意义 - 因此 ColumnToColumnMapping 仅在 update 语句上受支持。
 *
 * <p>与其直接实现此接口,不如扩展其中一个派生类。派生类封装了不同语句类型适用的映射规则。
 *
 * @author Jeff Butler
 *
 * @param <R> 访问器创建的对象类型
 */
interface ColumnMappingVisitor<R> {
    fun visit(mapping: NullMapping): R

    fun visit(mapping: ConstantMapping): R

    fun visit(mapping: StringConstantMapping): R

    fun <T> visit(mapping: ValueMapping<T>): R

    fun <T> visit(mapping: ValueOrNullMapping<T>): R

    fun <T> visit(mapping: ValueWhenPresentMapping<T>): R

    fun visit(mapping: SelectMapping): R

    fun visit(mapping: PropertyMapping): R

    fun visit(mapping: PropertyWhenPresentMapping): R

    fun visit(mapping: ColumnToColumnMapping): R

    fun visit(mapping: RowMapping): R

    fun visit(mapping: MappedColumnMapping): R

    fun visit(mapping: MappedColumnWhenPresentMapping): R
}
