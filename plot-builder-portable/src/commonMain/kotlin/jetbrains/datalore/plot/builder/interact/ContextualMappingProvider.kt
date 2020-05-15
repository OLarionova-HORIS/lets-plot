/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.interact

import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.interact.ContextualMapping
import jetbrains.datalore.plot.base.interact.DataContext
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.ValueSource
import jetbrains.datalore.plot.builder.tooltip.TooltipContentGenerator

interface ContextualMappingProvider {
    fun createContextualMapping(dataFrame: DataFrame, dataAccess: MappedDataAccess, tooltipValueSources: List<ValueSource>?): ContextualMapping

    companion object {
        val NONE = object : ContextualMappingProvider {
            override fun createContextualMapping(dataFrame: DataFrame, dataAccess: MappedDataAccess, tooltipValueSources: List<ValueSource>?): ContextualMapping {
                return ContextualMapping(
                    emptyList(),
                    emptyList(),
                    DataContext(dataFrame, dataAccess),
                    TooltipContentGenerator(tooltipValueSources ?: emptyList())
                )
            }
        }
    }
}
