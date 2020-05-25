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
    private var myTooltipValueSources: List<ValueSource>? = null

    fun setTooltipValueSources(tooltipValueSources: List<ValueSource>): GeomInteraction {
        myTooltipValueSources = tooltipValueSources
        return this
    }

    fun createLookupSpec(): LookupSpec {
        return LookupSpec(myLocatorLookupSpace, myLocatorLookupStrategy)
    }

    override fun createContextualMapping(dataAccess: MappedDataAccess, dataFrame: DataFrame): ContextualMapping {
        return if (myTooltipValueSources != null) {
            createUserDefinedContextualMapping(
                myTooltipValueSources!!,
                myAxisAes,
                dataAccess,
                dataFrame
            )
        } else {
            createContextualMapping(
                myAesListForTooltip,
                myAxisAes,
                dataAccess,
                dataFrame
            )
        }
    }

    companion object {
        fun createContextualMapping(
            aesListForTooltip: List<Aes<*>>,
            axisAes: List<Aes<*>>,
            dataAccess: MappedDataAccess,
            dataFrame: DataFrame
        ): ContextualMapping {
            val showInTip = aesListForTooltip.filter(dataAccess::isMapped)
            val dataContext = DataContext(dataFrame, dataAccess)
            val valueSources = defaultValueSources(dataContext, showInTip) + axisValueSources(dataContext, axisAes)
            return ContextualMapping(dataContext, valueSources)
        }

       private fun createUserDefinedContextualMapping(
            tooltipValueSources: List<ValueSource>,
            axisAes: List<Aes<*>>,
            dataAccess: MappedDataAccess,
            dataFrame: DataFrame
        ): ContextualMapping {
            val dataContext = DataContext(dataFrame = dataFrame, mappedDataAccess = dataAccess)
            val valueSources =
                userDefinedValueSources(dataContext, tooltipValueSources) + axisValueSources(dataContext, axisAes)
            return ContextualMapping(dataContext, valueSources)
        }

        private fun defaultValueSources(
            dataContext: DataContext,
            aesListForTooltip: List<Aes<*>>
        ): List<ValueSource> {
            return aesListForTooltip.map { aes ->
                MappedAes.createMappedAes(aes, isOutlier = false, dataContext = dataContext)
            }
        }

        private fun userDefinedValueSources(
            dataContext: DataContext,
            tooltipValueSources: List<ValueSource>
        ): List<ValueSource> {
            tooltipValueSources.forEach { it.setDataPointProvider(dataContext) }
            return tooltipValueSources
        }

        private fun axisValueSources(dataContext: DataContext, axisTooltipAes: List<Aes<*>>): List<ValueSource> {
            return axisTooltipAes
                .filter { listOf(Aes.X, Aes.Y).contains(it) }
                .map { aes ->
                    MappedAes.createMappedAxis(aes, dataContext)
                }
        }
    }
}
