/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.interact

import jetbrains.datalore.plot.base.interact.ContextualMapping
import jetbrains.datalore.plot.base.interact.DataPointFormatter
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.builder.tooltip.TooltipContentGenerator

interface ContextualMappingProvider {
    fun createContextualMapping(dataAccess: MappedDataAccess, formatters: List<DataPointFormatter>?): ContextualMapping

    companion object {
        val NONE = object : ContextualMappingProvider {
            override fun createContextualMapping(dataAccess: MappedDataAccess, formatters: List<DataPointFormatter>?): ContextualMapping {
                return ContextualMapping(
                    emptyList(),
                    emptyList(),
                    dataAccess,
                    TooltipContentGenerator(formatters = formatters ?: emptyList(), defaultTooltipAes = emptyList())
                )
            }
        }
    }
}
