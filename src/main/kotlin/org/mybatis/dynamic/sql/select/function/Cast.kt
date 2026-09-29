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
package org.mybatis.dynamic.sql.select.function

import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.render.RenderingContext
import org.mybatis.dynamic.sql.util.FragmentAndParameters
import java.util.Objects

/**
 * cast 类型转换函数。
 */
class Cast private constructor(builder: Builder) : BasicColumn {
    private val column: BasicColumn
    private val targetType: String
    private val alias: String?

    init {
        column = Objects.requireNonNull(builder.column!!)
        targetType = Objects.requireNonNull(builder.targetType!!)
        alias = builder.alias
    }

    override fun alias(): String? {
        return alias
    }

    override fun `as`(alias: String): Cast {
        return Builder().withColumn(column)
            .withTargetType(targetType)
            .withAlias(alias)
            .build()
    }

    override fun render(renderingContext: RenderingContext): FragmentAndParameters {
        return column.render(renderingContext).mapFragment { inFragment: String -> applyCast(inFragment) }
    }

    private fun applyCast(inFragment: String): String {
        return "cast($inFragment as $targetType)" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    class Builder {
        // 字段公开,以便外部类访问(Kotlin 外部类不能访问嵌套类私有成员)
        var column: BasicColumn? = null
        var targetType: String? = null
        var alias: String? = null

        fun withColumn(column: BasicColumn): Builder {
            this.column = column
            return this
        }

        fun withTargetType(targetType: String): Builder {
            this.targetType = targetType
            return this
        }

        fun withAlias(alias: String): Builder {
            this.alias = alias
            return this
        }

        fun build(): Cast {
            return Cast(this)
        }
    }
}
