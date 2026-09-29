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
package org.mybatis.dynamic.sql.util

import org.mybatis.dynamic.sql.exception.InvalidSqlException

/**
 * 校验工具类。
 */
class Validator private constructor() {
    companion object {
        @JvmField
        val ERROR_32: String = "ERROR.32" //$NON-NLS-1$

        @JvmStatic
        fun assertNotEmpty(collection: Collection<*>, messageNumber: String) {
            assertFalse(collection.isEmpty(), messageNumber)
        }

        @JvmStatic
        fun assertNotEmpty(collection: Collection<*>, messageNumber: String, p1: String) {
            assertFalse(collection.isEmpty(), messageNumber, p1)
        }

        @JvmStatic
        fun assertFalse(condition: Boolean, messageNumber: String) {
            if (condition) {
                throw InvalidSqlException(Messages.getString(messageNumber))
            }
        }

        @JvmStatic
        fun assertFalse(condition: Boolean, messageNumber: String, p1: String) {
            if (condition) {
                throw InvalidSqlException(Messages.getString(messageNumber, p1))
            }
        }

        @JvmStatic
        fun assertTrue(condition: Boolean, messageNumber: String) {
            assertFalse(!condition, messageNumber)
        }

        @JvmStatic
        fun assertTrue(condition: Boolean, messageNumber: String, p1: String) {
            assertFalse(!condition, messageNumber, p1)
        }

        @JvmStatic
        fun assertNull(`object`: Any?, messageNumber: String) {
            if (`object` != null) {
                throw InvalidSqlException(Messages.getString(messageNumber))
            }
        }
    }
}
