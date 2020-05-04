/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.ContextualMapping
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.TooltipContent.TooltipLine

class TooltipContentGenerator(
    contextualMapping: ContextualMapping,
    private val formatters: List<DataPointFormatter>?
) {
    private val myDataAccess: MappedDataAccess = contextualMapping.dataAccess

    fun generateLines(index: Int, outlierAes: List<Aes<*>>): List<TooltipLine> {
        return formatters?.map { it.format(myDataAccess, index) } ?: emptyList()
    }
}