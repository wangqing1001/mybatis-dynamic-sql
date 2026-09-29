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
package org.mybatis.dynamic.sql.configuration

import org.mybatis.dynamic.sql.exception.DynamicSqlException
import org.mybatis.dynamic.sql.util.Messages
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * 全局配置类。用于设置框架的全局行为。
 */
class GlobalConfiguration {

    private var nonRenderingWhereClauseAllowed = false
    private val properties = Properties()

    init {
        initializeProperties()
        initializeKnownProperties()
    }

    private fun initializeProperties() {
        val configFileName = getConfigurationFileName()
        val inputStream: InputStream? = this.javaClass.classLoader.getResourceAsStream(configFileName)
        if (inputStream != null) {
            loadProperties(inputStream, configFileName)
        }
    }

    private fun getConfigurationFileName(): String {
        val property = System.getProperty(CONFIGURATION_FILE_PROPERTY)
        return property?:DEFAULT_PROPERTY_FILE
    }

    fun loadProperties(inputStream: InputStream, propertyFile: String) {
        try {
            properties.load(inputStream)
        } catch (e: IOException) {
            throw DynamicSqlException(Messages.getString("ERROR.3", propertyFile), e) //$NON-NLS-1$
        }
    }

    private fun initializeKnownProperties() {
        nonRenderingWhereClauseAllowed = properties.getProperty("nonRenderingWhereClauseAllowed", "false")?.toBoolean() ?: false
    }


    fun nonRenderingWhereClauseAllowed(): Boolean {
        return this.nonRenderingWhereClauseAllowed
    }


    companion object {
        const val CONFIGURATION_FILE_PROPERTY = "mybatis-dynamic-sql.configurationFile" //$NON-NLS-1$
        private const val DEFAULT_PROPERTY_FILE = "mybatis-dynamic-sql.properties" //$NON-NLS-1$
    }
}
