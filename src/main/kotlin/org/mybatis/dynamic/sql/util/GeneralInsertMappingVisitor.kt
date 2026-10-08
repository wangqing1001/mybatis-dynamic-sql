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
 * 通用 insert 映射访问器基类。封装了 insert 语句适用的映射规则。
 */
abstract class GeneralInsertMappingVisitor<R> : ColumnMappingVisitor<R> {
    override fun visit(mapping: SelectMapping): R {
        throw UnsupportedOperationException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_1))
    }

    override fun visit(mapping: PropertyMapping): R {
        throw UnsupportedOperationException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_2))
    }

    override fun visit(mapping: PropertyWhenPresentMapping): R {
        throw UnsupportedOperationException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_3))
    }

    override fun visit(mapping: ColumnToColumnMapping): R {
        throw UnsupportedOperationException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_4))
    }

    override fun visit(mapping: RowMapping): R {
        throw UnsupportedOperationException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_14))
    }

    override fun visit(mapping: MappedColumnMapping): R {
        throw UnsupportedOperationException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_16))
    }

    override fun visit(mapping: MappedColumnWhenPresentMapping): R {
        throw UnsupportedOperationException(Messages.getInternalErrorString(InternalError.INTERNAL_ERROR_17))
    }
}
