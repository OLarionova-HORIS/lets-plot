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
    val dataContext: DataContext,
    private val tooltipGenerator: TooltipContent
) {
    fun getOutlierDataLines(index: Int, outlierAes: List<Aes<*>>): List<ValueSource.DataPoint> =
        tooltipGenerator.getOutlierDataLines(index, outlierAes, dataContext)

    fun getGeneralDataLines(index: Int): List<ValueSource.DataPoint> =
        tooltipGenerator.getGeneralDataLines(index)

    fun getAxisDataLines(index: Int): List<ValueSource.DataPoint> =
        tooltipGenerator.getAxisDataLines(index)
}