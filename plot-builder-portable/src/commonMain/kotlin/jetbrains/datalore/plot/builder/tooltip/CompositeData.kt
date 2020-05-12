/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.base.numberFormat.NumberFormat
import jetbrains.datalore.plot.base.interact.DataAccessor
import jetbrains.datalore.plot.base.interact.TooltipContent.TooltipLine
import jetbrains.datalore.plot.base.interact.TooltipContent.ValueSourceInfo
import jetbrains.datalore.plot.base.interact.ValueSource

class CompositeData(
    private val values: List<ValueSource>,
    private val myLabel: String,
    private val myFormatPattern: String
) : ValueSource {

    fun format(index: Int, dataAccessor: DataAccessor): TooltipLine? {
        val parts = values.map { dataValue ->
            val srcData = dataAccessor.getSourceData(dataValue, index)
            if (srcData != null) {
                val label = if (myLabel.isEmpty() && myFormatPattern.isEmpty()) srcData.label else ""
                makeLine(label, srcData.value)
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
            ValueSourceInfo(isContinuous = false, aes = null, isAxis = false, isOutlier = false)
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
        return makeLine(myLabel, valuesString)
    }

    companion object {
        val RE_PATTERN = """\{([^{}]*)}""".toRegex()
        const val STRING_PATTERN = "s"
    }
}
