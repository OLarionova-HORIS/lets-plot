/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.interact

import jetbrains.datalore.plot.base.interact.ValueSource.DataPoint

class TooltipContent(private val tooltipValueSources: List<ValueSource>) {
    fun getDataPoints(index: Int, outlierAes: List<ValueSource>): List<DataPoint> {
        // TODO Remove outlierAes (it should be existed in tooltipValueSources)
        return tooltipValueSources.mapNotNull { it.getDataPoint(index) } + outlierAes.mapNotNull { it.getDataPoint(index) }
    }
}
