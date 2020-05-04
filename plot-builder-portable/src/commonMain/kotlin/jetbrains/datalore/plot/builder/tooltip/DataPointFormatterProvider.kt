/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.interact.AbstractDataValue
import jetbrains.datalore.plot.base.interact.ContextualMapping

open class DataPointFormatterProvider(private var myDataFormatters: ArrayList<DataPointFormatter>? = null) {

    fun addFormatter(dataValue: AbstractDataValue, label: String, format: String) {
        myDataFormatters?.add(DataPointFormatter(listOf(dataValue), label, format))
    }

    fun addFormatter(dataValues: List<AbstractDataValue>, label: String, format: String) {
        myDataFormatters?.add(DataPointFormatter(dataValues, label, format))
    }

    fun createDataPointFormatter(contextualMapping: ContextualMapping): TooltipContentGenerator {
        return TooltipContentGenerator(contextualMapping, myDataFormatters)
    }

    companion object {
        val NONE = object : DataPointFormatterProvider(null) {
        }
    }
}
