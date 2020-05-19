/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.DataContext
import jetbrains.datalore.plot.base.interact.TooltipContent
import jetbrains.datalore.plot.base.interact.ValueSource
import jetbrains.datalore.plot.base.interact.ValueSource.DataPoint

class TooltipContentGenerator(private val tooltipValueSources: List<ValueSource>) : TooltipContent {

    override fun getDataPoints(index: Int, outlierAes: List<Aes<*>>, dataContext: DataContext): List<DataPoint> {
        return getOutlierDataPoints(index, outlierAes, dataContext) + getValueSourcesDataPoints(index)
    }

    private fun getOutlierDataPoints(index: Int, outlierAes: List<Aes<*>>, dataContext: DataContext): List<DataPoint> {
        return outlierAes.mapNotNull { aes ->
            MappedAes.createMappedAes(aes = aes, isOutlier = true, dataContext = dataContext)
                .getDataPoint(index)
        }
    }

    private fun getValueSourcesDataPoints(index: Int): List<DataPoint> {
        return tooltipValueSources.mapNotNull { it.getDataPoint(index) }
    }
}