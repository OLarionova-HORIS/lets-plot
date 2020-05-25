/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.interact

// `open` for Mockito tests
open class ContextualMapping(
    val dataContext: DataContext,
    private val tooltipValueSources: List<ValueSource>
) {

    // TODO Remove outlierAes (it should be existed in tooltipValueSources)
    fun getDataPoints(index: Int, outliers: List<ValueSource>): List<ValueSource.DataPoint> {
        if (tooltipValueSources.isEmpty()) {
            return emptyList()
        }
        return getDataPoints(index) + outliers.mapNotNull { it.getDataPoint(index) }
    }

    fun getDataPoints(index: Int): List<ValueSource.DataPoint> {
        return tooltipValueSources.mapNotNull { it.getDataPoint(index) }
    }
}