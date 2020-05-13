/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.interact.ValueSource

class DataPointFormatterBuilder {

    private var myDataFormatters: MutableList<ValueSource>? = null

    val dataFormatters: List<ValueSource>?
        get() = myDataFormatters

    fun addTooltipLine(dataValues: List<ValueSource>, label: String, format: String): DataPointFormatterBuilder {
        ensureFormattersInitialized()
        myDataFormatters!!.add(CompositeData(dataValues, label, format))
        return this
    }

    fun addTooltipLine(dataValue: ValueSource, label: String, format: String): DataPointFormatterBuilder {
        addTooltipLine(listOf(dataValue), label, format)
        return this
    }

    fun addTooltipLine(dataValue: ValueSource): DataPointFormatterBuilder {
        ensureFormattersInitialized()
        myDataFormatters!!.add(dataValue)
        return this
    }

    fun addTooltipLine(tooltipLineSpecification: TooltipLineSpecification): DataPointFormatterBuilder {
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

    private fun ensureFormattersInitialized() {
        if (myDataFormatters == null) {
            myDataFormatters = ArrayList()
        }
    }

    internal fun initDataFormattersList(): DataPointFormatterBuilder {
        ensureFormattersInitialized()
        return this
    }

    fun build(): DataPointFormatterProvider {
        return DataPointFormatterProvider(this)
    }

    companion object {
        fun initDataPointFormatterBuilder() = DataPointFormatterBuilder().initDataFormattersList()
    }
}
