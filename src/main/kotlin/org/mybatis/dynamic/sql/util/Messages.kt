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

import java.text.MessageFormat
import java.util.ResourceBundle

/**
 * 消息资源工具类。
 */
class Messages private constructor() {
    companion object {
        private const val BUNDLE_NAME = "org.mybatis.dynamic.sql.util.messages" //$NON-NLS-1$

        private val RESOURCE_BUNDLE: ResourceBundle = ResourceBundle.getBundle(BUNDLE_NAME)

        @JvmStatic
        fun getString(key: String): String {
            return RESOURCE_BUNDLE.getString(key)
        }

        @JvmStatic
        fun getString(key: String, p1: String): String {
            return MessageFormat.format(getString(key), p1)
        }

        @JvmStatic
        fun getString(key: String, p1: String, p2: String, p3: String): String {
            return MessageFormat.format(getString(key), p1, p2, p3)
        }

        @JvmStatic
        fun getInternalErrorString(internalError: InternalError): String {
            return MessageFormat.format(getString("INTERNAL.ERROR"), internalError.number) //$NON-NLS-1$
        }
    }
}
