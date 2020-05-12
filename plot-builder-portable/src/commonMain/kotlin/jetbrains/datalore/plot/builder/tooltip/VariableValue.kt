/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.interact.TooltipContent
import jetbrains.datalore.plot.base.interact.ValueSource

class VariableValue(private val name: String) : ValueSource {

    fun getValueName(): String {
        return name
    }

    fun getSourceData(index: Int, dataFrame: DataFrame): ValueSource.ValueSourceData? {
        val variable = dataFrame.variables().find { it.name == name } ?: return null
        return ValueSource.ValueSourceData(
            label = name,
            value = dataFrame[variable][index].toString(),
            isContinuous = dataFrame.isNumeric(variable),
            aes = null
        )
    }

    fun format(index: Int, dataFrame: DataFrame): TooltipContent.TooltipLine? {
        val data = getSourceData(index, dataFrame) ?: return null
        return TooltipContent.TooltipLine(
            makeLine(data.label, data.value),
            TooltipContent.ValueSourceInfo(
                isContinuous = data.isContinuous,
                aes = null,
                isAxis = false,
                isOutlier = false
            )
        )
    }
}