/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.interact.DataContext
import jetbrains.datalore.plot.base.interact.TooltipLineSpec
import jetbrains.datalore.plot.base.interact.TooltipLineSpec.DataPoint

class TooltipLine(
    val label: String?,
    val pattern: String,
    val fields: List<ValueSource>
) : TooltipLineSpec {
    private val myLineFormatter =
        LinePatternFormatter(pattern)

    fun setDataContext(dataContext: DataContext) {
        fields.forEach { it.setDataContext(dataContext) }
    }

    override fun getDataPoint(index: Int): DataPoint? {
        val dataValues = fields.map { dataValue ->
            dataValue.getDataPoint(index) ?: return null
        }
        return if (dataValues.size == 1) {
            val dataValue = dataValues.single()
            DataPoint(
                label = chooseLabel(dataValue.label),
                value = myLineFormatter.format(dataValue.value),
                isContinuous = dataValue.isContinuous,
                aes = dataValue.aes,
                isAxis = dataValue.isAxis,
                isOutlier = dataValue.isOutlier
            )
        } else {
            DataPoint(
                label = chooseLabel(dataValues.joinToString(", ") { it.label ?: "" }),
                value = myLineFormatter.format(dataValues.map { it.value }),
                isContinuous = false,
                aes = null,
                isAxis = false,
                isOutlier = false
            )
        }
    }

    private fun chooseLabel(dataLabel: String?): String? {
        return when (label) {
            USE_DEFAULT_LABEL -> dataLabel    // use default label (from data)
            else -> label                     // use the given label (can be null)
        }

    }

    companion object {
        fun defaultLineForValueSource(valueSource: ValueSource): TooltipLine = TooltipLine(
            label = USE_DEFAULT_LABEL,
            pattern = LinePatternFormatter.valueInLinePattern(),
            fields = listOf(valueSource)
        )
        private const val USE_DEFAULT_LABEL = "@"
    }
}