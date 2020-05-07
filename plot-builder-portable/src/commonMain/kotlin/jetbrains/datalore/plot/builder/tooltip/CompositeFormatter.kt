/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.base.numberFormat.NumberFormat
import jetbrains.datalore.plot.base.interact.AbstractDataValue
import jetbrains.datalore.plot.base.interact.DataPointFormatter
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.TooltipContent.TooltipLine

class CompositeFormatter(
    private val values: List<AbstractDataValue>,
    private val myLabel: String,
    private val myFormatPattern: String
) : DataPointFormatter {

    override fun format(dataAccess: MappedDataAccess, index: Int): TooltipLine? {
        val parts = values.map { dataValue ->
            val mappedData = dataAccess.getMappedData(dataValue, index)
            if (mappedData != null) {
                val label = if (myLabel.isEmpty() && myFormatPattern.isEmpty()) mappedData.label else ""
                AesFormatter.createLine(dataAccess, index, mappedData, label)
            } else {
                null
            }
        }

        if (parts.filterNotNull() != parts) {
            return null
        }

        val line = combine(parts.filterNotNull())
        return TooltipLine(
            line,
            isForAxis = false,
            isOutlier = false,
            forAesName = null
        )
    }

    private fun combine(values: List<String>): String {
        // todo make formatting with myLabel and myFormatPattern

        fun substitute(): String {
            val myFormatList = RE_PATTERN.findAll(myFormatPattern).map { it.groupValues[1] }.toList()
            if (myFormatList.size != values.size) {
                return ""
            }
            var index = 0
            return RE_PATTERN.replace(myFormatPattern) { match ->
                val pattern = match.groupValues[1]
                val value = values[index++]
                when {
                    pattern == STRING_PATTERN -> value
                    // NumberFormat.isNumericFormat(pattern)
                    else -> {
                        val formatter = NumberFormat(pattern)
                        formatter.apply(value.toFloat())
                    }
                }
            }
        }

        val valuesString = if (myFormatPattern.isEmpty()) {
            values.joinToString { it }
        } else {
            substitute()
        }
        return AesFormatter.makeLine(myLabel, valuesString)
    }

    companion object {
        val RE_PATTERN = """\{([^{}]*)}""".toRegex()
        val STRING_PATTERN = "s"
    }
}
