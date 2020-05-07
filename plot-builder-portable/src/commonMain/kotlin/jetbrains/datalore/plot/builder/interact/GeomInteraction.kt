/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.interact

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.ContextualMapping
import jetbrains.datalore.plot.base.interact.DataPointFormatter
import jetbrains.datalore.plot.base.interact.GeomTargetLocator.*
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.builder.tooltip.AesFormatter
import jetbrains.datalore.plot.builder.tooltip.AxisFormatter
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

    override fun createContextualMapping(dataAccess: MappedDataAccess, formatters: List<DataPointFormatter>?): ContextualMapping {
        return createContextualMapping(
            myAesListForTooltip,
            myAxisAes,
            dataAccess,
            formatters
        )
    }

    companion object {
        fun createContextualMapping(
            aesListForTooltip: List<Aes<*>>,
            axisAes: List<Aes<*>>,
            dataAccess: MappedDataAccess,
            formatters: List<DataPointFormatter>?
        ): ContextualMapping {

            val showInTip = aesListForTooltip.filter(dataAccess::isAesMapped)

            val defaultTooltipAes: List<Aes<*>>? = if (formatters == null) aesListForTooltip else null
            val allFormatters: List<DataPointFormatter> = prepareFormatters(
                defaultTooltipAes,
                axisAes,
                formatters
            )

            return ContextualMapping(
                showInTip,
                axisAes,
                dataAccess,
                TooltipContentGenerator(allFormatters, defaultTooltipAes)
            )
        }

        private fun prepareFormatters(
            defaultTooltipAes: List<Aes<*>>?,
            axisTooltipAes: List<Aes<*>>?,
            formatters: List<DataPointFormatter>?
        ): List<DataPointFormatter> {

            val result = mutableListOf<DataPointFormatter>()

            // use user formatters or set default aes
            result += (formatters ?: defaultTooltipAes?.map { aes ->
                AesFormatter(
                    aes,
                    isOutlier = false
                )
            }) ?: emptyList()

            // add axis
            axisTooltipAes?.filter { listOf(Aes.X, Aes.Y).contains(it) }
                ?.forEach { aes ->
                    result += AxisFormatter(aes)
                }

            return result
        }
    }
}
