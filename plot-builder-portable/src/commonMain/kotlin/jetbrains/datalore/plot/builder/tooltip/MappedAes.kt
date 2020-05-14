/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.TooltipContent
import jetbrains.datalore.plot.base.interact.ValueSource
import jetbrains.datalore.plot.builder.map.GeoPositionField

open class MappedAes(
    protected val aes: Aes<*>,
    private val isOutlier: Boolean = false,
    private val isAxis: Boolean = false
) : ValueSource {

    fun getAesName(): String {
        return aes.name
    }

    fun format(index: Int, dataAccess: MappedDataAccess): TooltipContent.TooltipLine? {
        val sourceData = getMappedData(index, dataAccess) ?: return null
        val sourceInfo = TooltipContent.ValueSourceInfo(
            isContinuous = sourceData.isContinuous,
            aes = aes,
            isAxis = isAxis,
            isOutlier = isOutlier
        )

        return when {
            isAxis && !isAxisTooltipAllowed(sourceData) -> null
            isAxis -> TooltipContent.TooltipLine(line = sourceData.value, sourceInfo = sourceInfo)
            else -> TooltipContent.TooltipLine(
                line = createLine(
                    dataAccess = dataAccess,
                    index = index,
                    sourceData = sourceData,
                    label = sourceData.label
                ),
                sourceInfo = sourceInfo
            )
        }
    }

    open fun getMappedData(index: Int, dataAccess: MappedDataAccess): ValueSource.ValueSourceData? {
        if (!dataAccess.isMapped(aes)) {
            return null
        }
        val mappedData = dataAccess.getMappedData(aes, index)
        return ValueSource.ValueSourceData(
            label = mappedData.label,
            value = mappedData.value,
            isContinuous = mappedData.isContinuous,
            aes = aes
        )
    }

    private fun isAxisTooltipAllowed(sourceData: ValueSource.ValueSourceData): Boolean {
        return when {
            MAP_COORDINATE_NAMES.contains(sourceData.label) -> false
            else -> sourceData.isContinuous
        }
    }

    private fun createLine(
        dataAccess: MappedDataAccess,
        index: Int,
        sourceData: ValueSource.ValueSourceData,
        label: String
    ): String {

        val myShortLabels = listOf(Aes.X, Aes.Y).mapNotNull { aes ->
            if (dataAccess.isMapped(aes)) {
                val value = dataAccess.getMappedData(aes, index)
                value.label
            } else {
                null
            }
        }

        fun isShortLabel() = myShortLabels.contains(label)

        fun shortText() = sourceData.value

        fun fullText() = LineFormatter.makeLine(label, sourceData.value)

        return when {
            label.isEmpty() -> shortText()
            isShortLabel() -> shortText()
            else -> fullText()
        }
    }

    companion object {
        private val MAP_COORDINATE_NAMES = setOf(
            GeoPositionField.POINT_X,
            GeoPositionField.POINT_X1,
            GeoPositionField.POINT_Y,
            GeoPositionField.POINT_Y1
        )
    }
}
