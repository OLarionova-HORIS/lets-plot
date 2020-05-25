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
    private var mySkipOutliers: Boolean = false

    fun setSkipOutliers(skipOutliers: Boolean) {
        mySkipOutliers = skipOutliers
    }

    // TODO Remove outlierAes (it should be existed in tooltipValueSources)
    fun getDataPoints(index: Int, outliers: List<ValueSource>): List<ValueSource.DataPoint> {
        return getDataPoints(index) + if (!mySkipOutliers) {
            outliers.mapNotNull { it.getDataPoint(index) }
        } else {
            emptyList()
        }
    }

    fun getDataPoints(index: Int): List<ValueSource.DataPoint> {
        return tooltipValueSources.mapNotNull { it.getDataPoint(index) }
    }
}