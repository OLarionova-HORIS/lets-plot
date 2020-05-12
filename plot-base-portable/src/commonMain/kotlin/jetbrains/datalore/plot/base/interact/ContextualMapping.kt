/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.interact

import jetbrains.datalore.plot.base.Aes

// `open` for Mockito tests
open class ContextualMapping(
    val tooltipAes: List<Aes<*>>,
    val axisAes: List<Aes<*>>,
    val dataAccessor: DataAccessor,
    private val tooltipGenerator: TooltipContent
) {
    fun generateLines(index: Int, outlierAes: List<Aes<*>>): List<TooltipContent.TooltipLine> =
        tooltipGenerator.generateLines(index, outlierAes, dataAccessor)
}