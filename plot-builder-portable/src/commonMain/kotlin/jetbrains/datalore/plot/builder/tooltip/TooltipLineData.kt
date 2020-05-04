/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.interact.AbstractDataValue


open class TooltipLineData(
    val data: List<AbstractDataValue>,
    val label: String,
    val format: String
)

class CompositeData(
    label: String,
    format: String,
    vararg values: AbstractDataValue
) : TooltipLineData(values.map { it }, label, format)

class SingleTooltipLineData(
    data: AbstractDataValue,
    label: String,
    format: String
) : TooltipLineData(listOf(data), label, format)
