/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.ContextualMapping
import jetbrains.datalore.plot.base.interact.DataAccess
import jetbrains.datalore.plot.base.interact.TooltipContent
import jetbrains.datalore.plot.base.interact.TooltipContent.TooltipLine

class TooltipContentGenerator(
    contextualMapping: ContextualMapping,
    private val formatters: List<DataPointFormatter>?
) : TooltipContent {

    private val myDataAccess: DataAccess = contextualMapping.dataAccess
    // TODO remove myAesGenerator:
    private val myAesGenerator: AesLinesGenerator = AesLinesGenerator(contextualMapping, emptyMap())

    override fun generateLines(index: Int, outlierAes: List<Aes<*>>): List<TooltipLine> {
        val result = mutableListOf<TooltipLine>()

        // TODO move this functionality
        result += myAesGenerator.generateLines(index, outlierAes, hasUserTooltips = formatters != null)

        result += formatters?.map { it.format(myDataAccess, index) } ?: emptyList()

        return result
    }
}