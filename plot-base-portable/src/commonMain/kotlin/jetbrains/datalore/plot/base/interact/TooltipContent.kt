/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.interact

import jetbrains.datalore.plot.base.Aes

// TODO remove interface, use class TooltipContentGenerator

interface TooltipContent {

    fun generateLines(index: Int, outlierAes: List<Aes<*>>, dataAccessor: DataAccessor): List<TooltipLine>

    data class ValueSourceInfo(
        private val isContinuous: Boolean, // todo use it in TooltipSpecFactory
        internal val aes: Aes<*>?,
        internal val isAxis: Boolean,
        internal val isOutlier: Boolean
    )

    class TooltipLine(
        val line: String,
        private val sourceInfo: ValueSourceInfo
    ) {
        fun isAes(): Boolean = sourceInfo.aes != null

        fun isAxis(): Boolean = sourceInfo.isAxis

        fun isOutlier(): Boolean = sourceInfo.isOutlier

        val aes: Aes<*>? = sourceInfo.aes
    }
}
