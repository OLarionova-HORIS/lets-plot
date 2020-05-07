/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.DataPointFormatter
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.TooltipContent
import jetbrains.datalore.plot.builder.map.GeoPositionField

class AxisFormatter(axis: Aes<*>) : DataPointFormatter {

    private val axisDataValue = AesValue(axis)

    override fun format(dataAccess: MappedDataAccess, index: Int): TooltipContent.TooltipLine? {
        val mappedData = dataAccess.getMappedData(axisDataValue, index)
        return if (!isAxisTooltipAllowed(mappedData)) {
            return null
        } else {
            TooltipContent.TooltipLine(
                line = mappedData!!.value,
                isForAxis = true,
                isOutlier = true,
                forAesName = axisDataValue.getValueName()
            )
        }
    }

    private fun isAxisTooltipAllowed(mappedData: MappedDataAccess.MappedData?): Boolean {
        return when {
            mappedData == null -> false
            MAP_COORDINATE_NAMES.contains(mappedData.label) -> false
            else -> mappedData.isContinuous
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