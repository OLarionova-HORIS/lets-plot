/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.ContextualMapping
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.TooltipContentGenerator.TooltipLine
import jetbrains.datalore.plot.builder.map.GeoPositionField


class AesLinesGenerator(
    contextualMapping: ContextualMapping,
    tooltipAesLabels: Map<String, String>) {

    private val myTooltipAes = contextualMapping.tooltipAes
    private val myAxisAes: List<Aes<*>> = contextualMapping.axisAes
    private val myDataAccess: MappedDataAccess = contextualMapping.dataAccess
    private val myTooltipLabels: Map<Aes<*>, String> = createTooltipLabels(tooltipAesLabels)

    fun generateLines(index: Int, aesHint: List<Aes<*>>, hasUserTooltips: Boolean): List<TooltipLine> {

        val myAesWithoutHint = if (!hasUserTooltips) ArrayList(myTooltipAes) else ArrayList()

        val result = mutableListOf<TooltipLine>()

        // hint aes
        aesHint.forEach { aes ->
            result += createLines(index, listOf(aes), forAxisTooltip = false, isOutlier = true)
            myAesWithoutHint.remove(aes)
        }

        // aes
        result += createLines(index, myAesWithoutHint, forAxisTooltip = false, isOutlier = false)

        // axis
        result += createLines(index, myAxisAes, forAxisTooltip = true, isOutlier = true)

        return result
    }

    private fun createLines(
        index: Int,
        aesList: List<Aes<*>>,
        forAxisTooltip: Boolean,
        isOutlier: Boolean
    ): List<TooltipLine> {

        val aesListForTooltip = removeDiscreteDuplicatedMappings(index, aesList)

        val result = mutableListOf<TooltipLine>()
        aesListForTooltip.forEach { aes ->
            val line = createLine(index, aes, forAxisTooltip)
            if (line != null) {
                result.add(
                    TooltipLine(
                        line = line,
                        isForAxis = forAxisTooltip,
                        isOutlier = isOutlier,
                        forAesName = aes.name
                    )
                )
            }
        }
        return result
    }

    private fun isMapped(aes: Aes<*>): Boolean {
        return myDataAccess.isMapped(aes)
    }

    private fun <T> getMappedData(index: Int, aes: Aes<T>): MappedDataAccess.MappedData<T> {
        return myDataAccess.getMappedData(aes, index)
    }

    private fun createLine(index: Int, aes: Aes<*>, forAxisTooltip: Boolean): String? {
        if (!isMapped(aes))
            return null

        val myShortLabels = ArrayList<String>()
        AXES.forEach {
            if (isMapped(it)) {
                myShortLabels.add(getMappedData(index, it).label)
            }
        }

        val mappedData = getMappedData(index, aes)
        val label = getLabel(index, aes)


        fun isShortLabel() = myShortLabels.contains(label)

        fun isAxisTooltipAllowed() = if (MAP_COORDINATE_NAMES.contains(label)) {
            false
        } else {
            mappedData.isContinuous
        }

        fun shortText() = mappedData.value

        fun fullText() = "$label: ${mappedData.value}"

        return when {
            isAxisAes(aes) && forAxisTooltip && !isAxisTooltipAllowed() -> null
            label.isEmpty() -> shortText()
            isAxisAes(aes) && forAxisTooltip -> shortText()
            isAxisAes(aes) && !forAxisTooltip && !hasUserLabel(aes) -> shortText()
            !isAxisAes(aes) && !hasUserLabel(aes) && isShortLabel() -> shortText()
            else -> fullText()
        }

    }

    private fun getLabel(index: Int, aes: Aes<*>): String {
        return when {
            hasUserLabel(aes) -> myTooltipLabels.getValue(aes)
            isMapped(aes) -> getMappedData(index, aes).label
            else -> ""
        }
    }

    private fun hasUserLabel(aes: Aes<*>): Boolean {
        return myTooltipLabels.containsKey(aes)
    }

    private fun removeDiscreteDuplicatedMappings(index: Int, aesWithoutHint: List<Aes<*>>): List<Aes<*>> {
        if (aesWithoutHint.isEmpty() || myTooltipLabels.isNotEmpty()) {
            return aesWithoutHint
        }

        val mappingsToShow = HashMap<String, Pair<Aes<*>, MappedDataAccess.MappedData<*>>>()
        for (aes in aesWithoutHint) {
            if (!isMapped(aes)) {
                continue
            }

            val mappingToCheck = getMappedData(index, aes)
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

    companion object {

        private val MAP_COORDINATE_NAMES = setOf(
            GeoPositionField.POINT_X,
            GeoPositionField.POINT_X1,
            GeoPositionField.POINT_Y,
            GeoPositionField.POINT_Y1
        )
        private val AXES = listOf(Aes.X, Aes.Y)

        fun isAxisAes(aes: Aes<*>) = AXES.contains(aes)

        fun createTooltipLabels(tooltipAesLabels: Map<String, String>): Map<Aes<*>, String> {
            fun getAesByName(aesName: String): Aes<*> {
                    return Aes.values().find { it.name == aesName } ?: error("$aesName is not aes name ")
            }

            val result = mutableMapOf<Aes<*>, String>()
            tooltipAesLabels
                .forEach { (name, label) ->
                    val aes = getAesByName(name)
                    result.put(aes, label)
                }
            return result
        }
    }
}