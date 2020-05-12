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
    private val aes: Aes<*>,
    private val isOutlier: Boolean = false,
    private val isAxis: Boolean = false
) : ValueSource {

    fun getAesName(): String {
        return aes.name
    }

    fun format(index: Int, dataAccess: MappedDataAccess): TooltipContent.TooltipLine? {
        if (!dataAccess.isMapped(aes)) {
            return null
        }
        val mappedData = dataAccess.getMappedData(aes, index)
        val sourceInfo = TooltipContent.ValueSourceInfo(
            isContinuous = mappedData.isContinuous,
            aes = aes,
            isAxis = isAxis,
            isOutlier = isOutlier
        )

        return when {
            isAxis && !isAxisTooltipAllowed(mappedData) -> null
            isAxis -> TooltipContent.TooltipLine(line = mappedData.value, sourceInfo = sourceInfo)
            else -> TooltipContent.TooltipLine(
                line = createLine(
                    dataAccess = dataAccess,
                    index = index,
                    srcData = mappedData,
                    label = mappedData.label
                ),
                sourceInfo = sourceInfo
            )
        }
    }

    fun getMappedData(index: Int, dataAccess: MappedDataAccess): ValueSource.ValueSourceData? {
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

    private fun isAxisTooltipAllowed(mappedData: MappedDataAccess.MappedData<*>): Boolean {
        return when {
            MAP_COORDINATE_NAMES.contains(mappedData.label) -> false
            else -> mappedData.isContinuous
        }
    }

    private fun createLine(
        dataAccess: MappedDataAccess,
        index: Int,
        srcData: MappedDataAccess.MappedData<*>,
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

        fun shortText() = srcData.value

        fun fullText() = makeLine(label, srcData.value)

        return when {
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
