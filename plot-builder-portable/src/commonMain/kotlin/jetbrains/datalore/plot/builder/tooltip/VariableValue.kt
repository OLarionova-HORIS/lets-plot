/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.interact.TooltipContent
import jetbrains.datalore.plot.base.interact.ValueSource

class VariableValue(
    private val name: String,
    private val label: String = "",
    format: String = ""
) : ValueSource {

    private val myFormatter = if (format.isEmpty()) null else LineFormatter(format)

    fun getVariableName(): String {
        return name
    }

    fun getSourceData(index: Int, dataFrame: DataFrame): ValueSource.ValueSourceData? {
        val variable = dataFrame.variables().find { it.name == name } ?: return null
        return ValueSource.ValueSourceData(
            label = name, // name of the variable
            value = dataFrame[variable][index].toString(),
            isContinuous = dataFrame.isNumeric(variable),
            aes = null
        )
    }

    fun format(index: Int, dataFrame: DataFrame): TooltipContent.TooltipLine? {
        val data = getSourceData(index, dataFrame) ?: return null
        val value = myFormatter?.format(data) ?: data.value
        val label = LineFormatter.chooseLabel(dataLabel = data.label, userLabel = label)
        return TooltipContent.TooltipLine(
            LineFormatter.makeLine(label, value),
            TooltipContent.ValueSourceInfo(
                isContinuous = data.isContinuous,
                aes = null,
                isAxis = false,
                isOutlier = false
            )
        )
    }
}