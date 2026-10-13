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
 *
 * <p>配置有两个来源,优先级从高到低:
 * <ol>
 *     <li>属性文件(默认 {@code mybatis-dynamic-sql.properties},可通过系统属性
 *         {@value #CONFIGURATION_FILE_PROPERTY} 指定);</li>
 *     <li>代码中的默认值。</li>
 * </ol>
 *
 * <dl>
 *     <dt>nonRenderingWhereClauseAllowed</dt>
 *     <dd>是否允许不渲染的 where 子句,默认为 false。</dd>
 *     <dt>enabledFilter</dt>
 *     <dd>是否启用 SQL 过滤器,默认为 true。</dd>
 * </dl>
 */
class GlobalConfiguration {

    private var nonRenderingWhereClauseAllowed = false
    private var enabledFilter = true
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
        enabledFilter = properties.getProperty("enabledFilter", "true")?.toBoolean() ?: true
    }


    fun nonRenderingWhereClauseAllowed(): Boolean {
        return this.nonRenderingWhereClauseAllowed
    }

    /**
     * 是否全局启用 SQL 过滤器。
     *
     * @return 启用时返回 true;默认为 true
     */
    fun enabledFilter(): Boolean {
        return this.enabledFilter
    }


    companion object {
        const val CONFIGURATION_FILE_PROPERTY = "mybatis-dynamic-sql.configurationFile" //$NON-NLS-1$
        private const val DEFAULT_PROPERTY_FILE = "mybatis-dynamic-sql.properties" //$NON-NLS-1$
    }
}
