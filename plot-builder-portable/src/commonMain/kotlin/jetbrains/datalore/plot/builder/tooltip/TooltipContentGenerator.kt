/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.DataPointFormatter
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.TooltipContent
import jetbrains.datalore.plot.base.interact.TooltipContent.TooltipLine

class TooltipContentGenerator(
    private val formatters: List<DataPointFormatter>,
    private val defaultTooltipAes: List<Aes<*>>?        //todo remove it
) : TooltipContent {

    override fun generateLines(index: Int, outlierAes: List<Aes<*>>, dataAccess: MappedDataAccess): List<TooltipLine> {

        val outlierLines = outlierAes.map { aes ->
            AesFormatter(aes, isOutlier = true).format(index, dataAccess)
        }

        // TODO move duplicating detection to TooltipSpecFactory?

        // build aes without hints and duplicated mapping
        val tooltipAesWithoutHint = when {
            defaultTooltipAes != null -> removeDiscreteDuplicatedMappings(
                dataAccess,
                index,
                defaultTooltipAes - outlierAes
            )
            else -> emptyList()
        }

        val otherLines = formatters.map { it.format(index, dataAccess) }

        // remove duplicated aes
        val duplicated = otherLines.filterNotNull()
            .filter { !it.isOutlier && it.forAesName != null }
            .filter { line ->
                tooltipAesWithoutHint.find { it.name == line.forAesName } == null
            }


        return (outlierLines + otherLines - duplicated).filterNotNull()
    }

    private fun removeDiscreteDuplicatedMappings(
        dataAccess: MappedDataAccess,
        index: Int,
        aesWithoutHint: List<Aes<*>>
    ): List<Aes<*>> {
        if (aesWithoutHint.isEmpty()) {
            return aesWithoutHint
        }

        val mappingsToShow = HashMap<String, Pair<Aes<*>, MappedDataAccess.MappedData>>()
        for (aes in aesWithoutHint) {
            val mappingToCheck = dataAccess.getMappedData(MappedAes(aes), index)
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
}