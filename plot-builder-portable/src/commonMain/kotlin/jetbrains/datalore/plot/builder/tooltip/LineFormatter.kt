/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.base.numberFormat.NumberFormat
import jetbrains.datalore.plot.base.interact.ValueSource

class LineFormatter(
    private val formatPattern: String
) {
    fun format(valuePoints: List<ValueSource.DataPoint>): String {
        val myFormatList = RE_PATTERN.findAll(formatPattern).map { it.groupValues[MATCHED_INDEX] }.toList()
        if (myFormatList.size != valuePoints.size) {
            return ""
        }
        var index = 0
        return RE_PATTERN.replace(formatPattern) { valuePoints[index++].value }
    }

    companion object {
        val RE_PATTERN = """\{([^{}]*)}""".toRegex()
        private const val MATCHED_INDEX = 1

        private const val DEFAULT_LABEL = "@"
        internal fun chooseLabel(dataLabel: String, userLabel: String?): String {
            return when {
                userLabel == null -> ""     // value without label
                userLabel.isEmpty() -> ""   // todo value with empty label ("label : value" format)
                userLabel == DEFAULT_LABEL -> dataLabel
                else -> userLabel
            }
        }
    }
}