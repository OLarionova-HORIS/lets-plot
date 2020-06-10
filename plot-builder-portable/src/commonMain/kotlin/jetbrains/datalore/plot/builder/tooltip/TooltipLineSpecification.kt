/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.interact.ValueSource


open class TooltipLineSpecification(
    val label: String?,
    val linePattern: String,
    val data: List<ValueSource>
) {
    companion object {

        fun multiValueLine(label: String?, linePattern: String, data: List<ValueSource>): TooltipLineSpecification =
            TooltipLineSpecification(label, linePattern, data)

        fun singleValueLine(label: String?, linePattern: String, datum: ValueSource): TooltipLineSpecification =
            TooltipLineSpecification(label, linePattern, listOf(datum))
    }
}
