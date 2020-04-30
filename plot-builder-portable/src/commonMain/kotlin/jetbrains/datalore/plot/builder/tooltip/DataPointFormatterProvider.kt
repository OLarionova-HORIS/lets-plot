/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.interact.ContextualMapping
import jetbrains.datalore.plot.base.interact.TooltipContentGenerator

open class DataPointFormatterProvider(builder: DataPointFormatterBuilder) {

    private val myDataFormatters: List<DataPointFormatter>? = builder.dataFormatters

    fun createDataPointFormatter(contextualMapping: ContextualMapping): TooltipContentGenerator {
        return TooltipContentBuilder(contextualMapping, myDataFormatters)
    }

    companion object {
        val NONE = object : DataPointFormatterProvider(DataPointFormatterBuilder(null)) {
        }
    }
}
