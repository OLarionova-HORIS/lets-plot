/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.DataContext
import jetbrains.datalore.plot.base.interact.ValueSource

class ValueSourcesProvider(private val dataContext: DataContext) {

    private val myTooltipValueSources = mutableListOf<ValueSource>()

    val tooltipValueSources: List<ValueSource>
        get() = myTooltipValueSources

    fun addGeneralTooltipSources(
        defaultTooltipAes: List<Aes<*>>,
        userTooltipValueSources: List<ValueSource>?
    ): ValueSourcesProvider {
        // use user's sources or set from default aes list
        if (userTooltipValueSources != null) {
            userTooltipValueSources.forEach { it.setDataPointProvider(dataContext) }
            myTooltipValueSources += userTooltipValueSources
        } else {
            defaultTooltipAes.forEach { aes ->
                myTooltipValueSources += MappedAes.createMappedAes(aes, isOutlier = false, dataContext = dataContext)
            }
        }
        return this
    }

    fun addOutlierTooltipSources(outlierAes: List<Aes<*>>): ValueSourcesProvider {
        myTooltipValueSources += outlierAes
            .map { aes -> MappedAes.createMappedAes(aes = aes, isOutlier = true, dataContext = dataContext) }
        return this
    }

    fun addAxisTooltipSources(axisTooltipAes: List<Aes<*>>): ValueSourcesProvider {
        myTooltipValueSources += axisTooltipAes
            .filter { listOf(Aes.X, Aes.Y).contains(it) }
            .map { aes -> MappedAes.createMappedAxis(aes, dataContext) }
        return this
    }
}

