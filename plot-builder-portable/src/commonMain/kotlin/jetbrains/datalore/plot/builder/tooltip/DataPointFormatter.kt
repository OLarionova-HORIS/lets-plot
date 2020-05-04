/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.base.numberFormat.NumberFormat
import jetbrains.datalore.plot.base.interact.AbstractDataValue
import jetbrains.datalore.plot.base.interact.DataAccess
import jetbrains.datalore.plot.base.interact.TooltipContent.TooltipLine


class DataPointFormatter(
    private val values: List<AbstractDataValue>,
    private val myLabel: String,
    private val myFormatPattern: String
) {

    fun format(dataAccess: DataAccess, index: Int): TooltipLine {
        val parts = values.map { dataValue ->
            val mappedData = dataAccess.getValueData(dataValue, index)
            val label = if (myLabel.isEmpty() && myFormatPattern.isEmpty()) mappedData.label else ""
            val value = mappedData.value
            makeLine(label, value)
        }

        val value = combine(parts)
        val line = makeLine(myLabel, value)

        return TooltipLine(line)
    }

    private fun makeLine(label: String, value: String): String {
        return if (label.isNotEmpty()) "$label: $value" else value
    }

    private fun combine(values: List<String>): String {
        // todo make formatting with myLabel and myFormatPattern

        var valuesString = values.joinToString { it }
        if (myFormatPattern.isEmpty()) {
            return valuesString
        }

        val myFormatList = RE_PATTERN.findAll(myFormatPattern).map { it.groupValues[1] }.toList()
        if (myFormatList.size == values.size) {
            var index = 0
            val formatted = RE_PATTERN.replace(myFormatPattern) { match ->
                val pattern = match.value.removeSurrounding("{", "}")
                require(pattern == myFormatList[index])

                val formatter = NumberFormat(pattern)
                formatter.apply(values[index++].toFloat())
            }
            valuesString = formatted
        }
        return valuesString
    }

    companion object {
        val RE_PATTERN = """\{([^{}]*)}""".toRegex()
    }
}

