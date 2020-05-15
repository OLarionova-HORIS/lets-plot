/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.interact.ValueSource

class DataPointFormatterProvider {

    val tooltipValueSourceList = mutableListOf<ValueSource>()

    fun addTooltipLine(dataValues: List<ValueSource>, label: String, format: String): DataPointFormatterProvider {
        tooltipValueSourceList.add(CompositeValue(dataValues, label, format))
        return this
    }

    fun addTooltipLine(dataValue: ValueSource, label: String, format: String): DataPointFormatterProvider {
        addTooltipLine(listOf(dataValue), label, format)
        return this
    }

    fun addTooltipLine(dataValue: ValueSource): DataPointFormatterProvider {
        tooltipValueSourceList.add(dataValue)
        return this
    }

    fun addTooltipLine(tooltipLineSpecification: TooltipLineSpecification): DataPointFormatterProvider {
        if (tooltipLineSpecification.data.size == 1) {
            addTooltipLine(tooltipLineSpecification.data.single())
        } else {
            addTooltipLine(
                tooltipLineSpecification.data,
                tooltipLineSpecification.label,
                tooltipLineSpecification.format
            )
        }
        return this
    }
}
