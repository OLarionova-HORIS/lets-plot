/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.interact.DataAccessor
import jetbrains.datalore.plot.base.interact.TooltipContent.TooltipLine
import jetbrains.datalore.plot.base.interact.TooltipContent.ValueSourceInfo
import jetbrains.datalore.plot.base.interact.ValueSource

class CompositeData(
    private val values: List<ValueSource>,
    private val label: String,
    format: String
) : ValueSource {

    private val myFormatter = if (format.isEmpty()) null else LineFormatter(format)

    fun format(index: Int, dataAccessor: DataAccessor): TooltipLine? {
        val dataValues = values.map { dataValue ->
            dataAccessor.getSourceData(dataValue, index)
        }
        if (dataValues.filterNotNull() != dataValues) {
            return null
        }
        val line = combine(dataValues.filterNotNull())
        return TooltipLine(
            line,
            ValueSourceInfo(isContinuous = false, aes = null, isAxis = false, isOutlier = false)
        )
    }

    private fun combine(values: List<ValueSource.ValueSourceData>): String {
        val valuesString =
            myFormatter?.format(values) ?: values.joinToString { LineFormatter.makeLine(it.label, it.value) }
        return LineFormatter.makeLine(label, valuesString)
    }
}
