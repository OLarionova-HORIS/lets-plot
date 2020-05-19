/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.interact

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.interact.*
import jetbrains.datalore.plot.base.interact.GeomTargetLocator.*
import jetbrains.datalore.plot.builder.tooltip.MappedAes

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
            val allTooltipSources: List<ValueSource> = prepareTooltipSourceList(
                dataContext,
                defaultTooltipAes = aesListForTooltip,
                axisTooltipAes = axisAes,
                tooltipValueSources = tooltipValueSources
            )

            return ContextualMapping(
                showInTip,
                axisAes,
                dataContext,
                TooltipContent(allTooltipSources)
            )
        }

        private fun prepareTooltipSourceList(
            dataContext: DataContext,
            defaultTooltipAes: List<Aes<*>>,
            axisTooltipAes: List<Aes<*>>?,
            tooltipValueSources: List<ValueSource>?
        ): List<ValueSource> {

            val result = mutableListOf<ValueSource>()

            // use user's sources or set default aes
            if (tooltipValueSources != null) {
                tooltipValueSources.forEach { it.setDataPointProvider(dataContext) }
                result += tooltipValueSources
            } else {
                defaultTooltipAes.forEach { aes ->
                    result += MappedAes.createMappedAes(aes, isOutlier = false, dataContext = dataContext)
                }
            }
            // add axis
            axisTooltipAes?.filter { listOf(Aes.X, Aes.Y).contains(it) }
                ?.forEach { aes ->
                    result += MappedAes.createMappedAxis(aes, dataContext)
                }

            return result
        }
    }
}
