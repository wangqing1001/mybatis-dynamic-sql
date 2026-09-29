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
 * insert 映射访问器基类。
 */
abstract class InsertMappingVisitor<R> : ColumnMappingVisitor<R> {
    override fun <T> visit(mapping: ValueMapping<T>): R {
        throw UnsupportedOperationException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_5))
    }

    override fun <T> visit(mapping: ValueOrNullMapping<T>): R {
        throw UnsupportedOperationException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_6))
    }

    override fun <T> visit(mapping: ValueWhenPresentMapping<T>): R {
        throw UnsupportedOperationException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_7))
    }

    override fun visit(mapping: SelectMapping): R {
        throw UnsupportedOperationException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_8))
    }

    override fun visit(mapping: ColumnToColumnMapping): R {
        throw UnsupportedOperationException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_9))
    }
}
