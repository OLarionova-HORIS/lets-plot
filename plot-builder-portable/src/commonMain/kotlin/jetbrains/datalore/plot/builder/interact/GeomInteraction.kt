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

            val tooltipSources = mutableListOf<ValueSource>()

            // use user's sources or set default aes
            val hasEmptyUserTooltipList: Boolean
            if (tooltipValueSources != null) {
                tooltipValueSources.forEach { it.setDataPointProvider(dataContext) }
                tooltipSources += tooltipValueSources
                hasEmptyUserTooltipList = tooltipSources.isEmpty()
            } else {
                showInTip.forEach { aes ->
                    tooltipSources += MappedAes.createMappedAes(aes, isOutlier = false, dataContext = dataContext)
                }
                hasEmptyUserTooltipList = false
            }
            // add axis
            tooltipSources += createAxisTooltipSources(dataContext, axisAes)

            return ContextualMapping(dataContext, tooltipSources).also { it.setSkipOutliers(hasEmptyUserTooltipList) }
        }

        private fun createAxisTooltipSources(dataContext: DataContext, axisTooltipAes: List<Aes<*>>?): List<ValueSource> {
            return axisTooltipAes
                ?.filter { listOf(Aes.X, Aes.Y).contains(it) }
                ?.map { aes ->
                    MappedAes.createMappedAxis(aes, dataContext)
                }                ?: emptyList()
        }
    }
}
