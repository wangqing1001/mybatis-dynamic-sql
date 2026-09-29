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
 * 字符串工具类。
 */
interface StringUtilities {

    companion object {
        @JvmStatic
        fun spaceAfter(str: String): String {
            return "$str " //$NON-NLS-1$
        }

        @JvmStatic
        fun spaceBefore(str: String): String {
            return " $str" //$NON-NLS-1$
        }

        @JvmStatic
        fun toCamelCase(inputString: String): String {
            val sb = StringBuilder()

            var nextUpperCase = false

            for (i in 0 until inputString.length) {
                val c = inputString[i]
                if (Character.isLetterOrDigit(c)) {
                    if (nextUpperCase) {
                        sb.append(Character.toUpperCase(c))
                        nextUpperCase = false
                    } else {
                        sb.append(Character.toLowerCase(c))
                    }
                } else {
                    if (!sb.isEmpty()) {
                        nextUpperCase = true
                    }
                }
            }

            return sb.toString()
        }

        @JvmStatic
        fun formatConstantForSQL(str: String): String {
            val escaped = str.replace("'", "''") //$NON-NLS-1$ //$NON-NLS-2$
            return "'$escaped'" //$NON-NLS-1$ //$NON-NLS-2$
        }

        @JvmStatic
        fun <T> upperCaseIfPossible(value: T): T {
            if (value is String) {
                @Suppress("UNCHECKED_CAST")
                val t = value.uppercase() as T
                return t
            }

            return value
        }
    }
}
