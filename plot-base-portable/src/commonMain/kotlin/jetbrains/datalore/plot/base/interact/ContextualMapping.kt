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
    val dataAccess: MappedDataAccess
) {
    private lateinit var tooltipValueSources: List<ValueSource>

    fun initTooltipValueSources(valueSources: List<ValueSource>)  {
        tooltipValueSources = valueSources
    }

    fun getDataPoints(index: Int): List<ValueSource.DataPoint> {
        return tooltipValueSources.mapNotNull { it.getDataPoint(index) }
    }
}