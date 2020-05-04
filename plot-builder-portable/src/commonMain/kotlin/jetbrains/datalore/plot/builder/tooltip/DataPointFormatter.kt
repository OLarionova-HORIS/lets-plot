/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.interact.AbstractDataValue
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.TooltipContent.TooltipLine


class DataPointFormatter(
    private val values: List<AbstractDataValue>,
    private val myLabel: String,
    private val myFormatPattern: String
) {

    fun format(dataAccess: MappedDataAccess, index: Int): TooltipLine {
        val parts = values.map { dataValue ->
            val mappedData = dataAccess.getMappedData(dataValue, index)
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
        return values.joinToString { it }
    }
}

