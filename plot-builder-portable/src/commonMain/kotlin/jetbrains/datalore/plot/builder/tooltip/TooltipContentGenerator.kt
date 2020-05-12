/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.DataAccessor
import jetbrains.datalore.plot.base.interact.TooltipContent
import jetbrains.datalore.plot.base.interact.TooltipContent.TooltipLine
import jetbrains.datalore.plot.base.interact.ValueSource

class TooltipContentGenerator(private val formatters: List<ValueSource>) : TooltipContent {

    override fun generateLines(index: Int, outlierAes: List<Aes<*>>, dataAccessor: DataAccessor): List<TooltipLine> {
        val result = mutableListOf<TooltipLine?>()
        result += outlierAes.map { aes ->
            dataAccessor.getFormattedData(
                MappedAes(aes, isOutlier = true, isAxis = false),
                index
            )
        }
        result += formatters.map { dataAccessor.getFormattedData(it, index) }
        return result.filterNotNull()
    }
}