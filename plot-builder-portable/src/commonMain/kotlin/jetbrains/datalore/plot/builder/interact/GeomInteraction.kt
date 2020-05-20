/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.interact

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.interact.ContextualMapping
import jetbrains.datalore.plot.base.interact.DataContext
import jetbrains.datalore.plot.base.interact.GeomTargetLocator.*
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.ValueSource
import jetbrains.datalore.plot.builder.tooltip.ValueSourcesProvider

class GeomInteraction(builder: GeomInteractionBuilder) :
    ContextualMappingProvider {

    private val myLocatorLookupSpace: LookupSpace = builder.locatorLookupSpace
    private val myLocatorLookupStrategy: LookupStrategy = builder.locatorLookupStrategy
    private val myAesListForTooltip: List<Aes<*>> = builder.aesListForTooltip
    private val myAxisAes: List<Aes<*>> = builder.axisAesListForTooltip

    fun createLookupSpec(): LookupSpec {
        return LookupSpec(myLocatorLookupSpace, myLocatorLookupStrategy)
    }

    override fun createContextualMapping(
        dataAccess: MappedDataAccess,
        dataFrame: DataFrame,
        tooltipValueSources: List<ValueSource>?
    ): ContextualMapping {
        return createContextualMapping(
            myAesListForTooltip,
            myAxisAes,
            dataAccess,
            dataFrame,
            tooltipValueSources
        )
    }

    companion object {
        fun createContextualMapping(
            aesListForTooltip: List<Aes<*>>,
            axisAes: List<Aes<*>>,
            dataAccess: MappedDataAccess,
            dataFrame: DataFrame,
            tooltipValueSources: List<ValueSource>?
        ): ContextualMapping {

            val showInTip = aesListForTooltip.filter(dataAccess::isMapped)
            val dataContext = DataContext(dataFrame, dataAccess)

            //todo not here
            val valuesSourceProvider = ValueSourcesProvider(dataContext)
                .addGeneralTooltipSources(
                    defaultTooltipAes = showInTip,
                    userTooltipValueSources = tooltipValueSources
                )
                .addAxisTooltipSources(axisAes)

            return ContextualMapping(
                showInTip,
                axisAes,
                dataContext
            )   //todo not here
                .also { it.initTooltipValueSources(valuesSourceProvider.tooltipValueSources) }
        }
    }
}
