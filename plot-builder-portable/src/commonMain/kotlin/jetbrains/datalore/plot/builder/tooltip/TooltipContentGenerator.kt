/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.ContextualMapping
import jetbrains.datalore.plot.base.interact.DataAccess
import jetbrains.datalore.plot.base.interact.DataPointFormatter
import jetbrains.datalore.plot.base.interact.TooltipContent
import jetbrains.datalore.plot.base.interact.TooltipContent.TooltipLine

class TooltipContentGenerator(
    contextualMapping: ContextualMapping,
    formatters: List<DataPointFormatter>?
) : TooltipContent {

    private val myDataAccess: DataAccess = contextualMapping.dataAccess
    private val defaultTooltipAes: List<Aes<*>>? = if (formatters == null) contextualMapping.tooltipAes else null
    private val myFormatters: List<DataPointFormatter> = initFormatters(
        defaultTooltipAes,
        contextualMapping.axisAes,
        formatters
    )

    override fun generateLines(index: Int, outlierAes: List<Aes<*>>): List<TooltipLine> {

        val outlierLines = outlierAes.map { aes ->
            AesFormatter(aes, isOutlier = true).format(myDataAccess, index)
        }

        // TODO move duplicating detection to TooltipSpecFactory?

        // build aes without hints and duplicated mapping
        val tooltipAesWithoutHint = when {
            defaultTooltipAes != null -> removeDiscreteDuplicatedMappings(
                myDataAccess,
                index,
                defaultTooltipAes - outlierAes
            )
            else -> emptyList()
        }

        val otherLines = myFormatters.map { it.format(myDataAccess, index) }

        // remove duplicated aes
        val duplicated = otherLines.filterNotNull()
            .filter { !it.isOutlier && it.forAesName != null }
            .filter { line ->
                tooltipAesWithoutHint.find { it.name == line.forAesName } == null
            }


        return (outlierLines + otherLines - duplicated).filterNotNull()
    }

    private fun removeDiscreteDuplicatedMappings(
        dataAccess: DataAccess,
        index: Int,
        aesWithoutHint: List<Aes<*>>
    ): List<Aes<*>> {
        if (aesWithoutHint.isEmpty()) {
            return aesWithoutHint
        }

        val mappingsToShow = HashMap<String, Pair<Aes<*>, DataAccess.ValueData>>()
        for (aes in aesWithoutHint) {
            val mappingToCheck = dataAccess.getValueData(AesValue(aes), index)
            if (mappingToCheck == null) {
                continue
            }

            if (!mappingsToShow.containsKey(mappingToCheck.label)) {
                mappingsToShow[mappingToCheck.label] = Pair(aes, mappingToCheck)
                continue
            }

            val mappingToShow = mappingsToShow[mappingToCheck.label]?.second
            if (!mappingToShow!!.isContinuous && mappingToCheck.isContinuous) {
                mappingsToShow[mappingToCheck.label] = Pair(aes, mappingToCheck)
            }
        }

        return mappingsToShow.values.map { pair -> pair.first }
    }

    private fun initFormatters(
        defaultTooltipAes: List<Aes<*>>?,
        axisTooltipAes: List<Aes<*>>?,
        formatters: List<DataPointFormatter>?
    ): List<DataPointFormatter> {

        val result = mutableListOf<DataPointFormatter>()

        // use user formatters or set default aes
        result += (formatters ?: defaultTooltipAes?.map { aes ->
            AesFormatter(
                aes,
                isOutlier = false
            )
        }) ?: emptyList()

        // add axis
        axisTooltipAes?.filter { listOf(Aes.X, Aes.Y).contains(it) }
            ?.forEach { aes ->
                result += AxisFormatter(aes)
            }

        return result
    }
}