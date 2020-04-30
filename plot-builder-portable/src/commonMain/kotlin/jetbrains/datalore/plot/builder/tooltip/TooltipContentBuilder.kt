/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.ContextualMapping
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.TooltipContentGenerator
import jetbrains.datalore.plot.base.interact.TooltipContentGenerator.TooltipLine

class TooltipContentBuilder(
    contextualMapping: ContextualMapping,
    private val myFormatters: List<DataPointFormatter>?
) : TooltipContentGenerator {

    private val myDataAccess: MappedDataAccess = contextualMapping.dataAccess
    private val myAesGenerator: AesLinesGenerator = AesLinesGenerator(contextualMapping, emptyMap())

    override fun generateLines(index: Int, aesHint: List<Aes<*>>): List<TooltipLine> {
        val result = mutableListOf<TooltipLine>()
        result += myFormatters?.map { it.format(myDataAccess, index) } ?: emptyList()
        result += myAesGenerator.generateLines(index, aesHint, hasUserTooltips = myFormatters != null)
        return result
    }
}