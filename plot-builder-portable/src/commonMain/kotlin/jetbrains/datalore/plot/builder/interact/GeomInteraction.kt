/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.interact

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.interact.ContextualMapping
import jetbrains.datalore.plot.base.interact.GeomTargetLocator.*
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.ValueSource
import jetbrains.datalore.plot.builder.tooltip.DataValueSourceAccessor
import jetbrains.datalore.plot.builder.tooltip.MappedAes
import jetbrains.datalore.plot.builder.tooltip.TooltipContentGenerator

class GeomInteraction(builder: GeomInteractionBuilder) :
    ContextualMappingProvider {

    private val myLocatorLookupSpace: LookupSpace = builder.locatorLookupSpace
    private val myLocatorLookupStrategy: LookupStrategy = builder.locatorLookupStrategy
    private val myAesListForTooltip: List<Aes<*>> = builder.aesListForTooltip
    private val myAxisAes: List<Aes<*>> = builder.axisAesListForTooltip

    fun createLookupSpec(): LookupSpec {
        return LookupSpec(myLocatorLookupSpace, myLocatorLookupStrategy)
    }

    override fun createContextualMapping(dataFrame: DataFrame, dataAccess: MappedDataAccess, formatters: List<ValueSource>?): ContextualMapping {
        return createContextualMapping(
            myAesListForTooltip,
            myAxisAes,
            dataFrame,
            dataAccess,
            formatters
        )
    }

    companion object {
        fun createContextualMapping(
            aesListForTooltip: List<Aes<*>>,
            axisAes: List<Aes<*>>,
            dataFrame: DataFrame,
            dataAccess: MappedDataAccess,
            formatters: List<ValueSource>?
        ): ContextualMapping {

            val showInTip = aesListForTooltip.filter(dataAccess::isMapped)

            val defaultTooltipAes: List<Aes<*>>? = if (formatters == null) aesListForTooltip else null
            val allFormatters: List<ValueSource> = prepareFormatters(
                defaultTooltipAes,
                axisAes,
                formatters
            )

            return ContextualMapping(
                showInTip,
                axisAes,
                DataValueSourceAccessor(dataFrame, dataAccess),
                TooltipContentGenerator(allFormatters)
            )
        }

        private fun prepareFormatters(
            defaultTooltipAes: List<Aes<*>>?,
            axisTooltipAes: List<Aes<*>>?,
            formatters: List<ValueSource>?
        ): List<ValueSource> {

            val result = mutableListOf<ValueSource>()

            // use user formatters or set default aes
            result += (formatters ?: defaultTooltipAes?.map { aes ->
                MappedAes(
                    aes,
                    isOutlier = false,
                    isAxis = false
                )
            }) ?: emptyList()

            // add axis
            axisTooltipAes?.filter { listOf(Aes.X, Aes.Y).contains(it) }
                ?.forEach { aes ->
                    result += MappedAes(aes, isOutlier = true, isAxis = true)
                }

            return result
        }
    }
}
