/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.interact.AbstractDataValue


open class TooltipLineData(
    val label: String,
    val format: String,
    val data: List<AbstractDataValue>
) {
    companion object {

        fun multiValueLine(label: String, format: String, data: List<AbstractDataValue>): TooltipLineData =
            TooltipLineData(label, format, data)

        fun multiValueLine(label: String, format: String, vararg data: AbstractDataValue): TooltipLineData =
            TooltipLineData(label, format, data.map { it })

        fun singleValueLine(label: String, format: String, datum: AbstractDataValue): TooltipLineData =
            TooltipLineData(label, format, listOf(datum))
    }
}
