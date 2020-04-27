/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.ContextualMapping
import jetbrains.datalore.plot.base.interact.TooltipContentGenerator
import jetbrains.datalore.plot.base.interact.TooltipContentGenerator.TooltipLine

class TooltipContentBuilder(
    contextualMapping: ContextualMapping,
    tooltipSettings: List<TooltipConfigLine>? = null
) : TooltipContentGenerator {

    enum class ValueType {
        AES, VAR, TEXT, UNKNOWN
    }

    private val myLineGenerators: Map<ValueType, BaseLineGenerator> = createLineGenerators(contextualMapping, tooltipSettings)

    override fun generateLines(index: Int, aesHint: List<Aes<*>>): List<TooltipLine> {
        val result = mutableListOf<TooltipLine>()
        myLineGenerators.forEach {
            result += it.value.generateLines(index, aesHint)
        }
        return result
    }

    abstract class BaseLineGenerator {

        abstract fun generateLines(index: Int): List<TooltipLine>

        open fun generateLines(index: Int, aesHint: List<Aes<*>>): List<TooltipLine> {
           return generateLines(index)
        }
    }

    private fun createLineGenerators(
        contextualMapping: ContextualMapping,
        tooltipSettings: List<TooltipConfigLine>? = null
    ):  Map<ValueType, BaseLineGenerator> {

        if (tooltipSettings == null) {
            return mapOf(ValueType.AES to AesLinesGenerator(contextualMapping, emptyMap()))
        }

        fun getTypeByName(name: String?): ValueType {
            return when {
                name.isNullOrEmpty() -> ValueType.TEXT
                TooltipConfigLine.hasAesPrefix(name) -> ValueType.AES
                else -> ValueType.VAR
                // else -> ValueType.UNKNOWN
            }
        }

        fun getAesLabels(tooltipSettings: List<TooltipConfigLine>?): Map<String, String> {
            return tooltipSettings
                ?.filter { it.name != null}
                ?.filter { TooltipConfigLine.hasAesPrefix(it.name!!) }
                ?.associateBy( {TooltipConfigLine.detachAesName(it.name!!)}, { it.label } )
                ?: emptyMap()
        }

        fun getVarLabels(tooltipSettings: List<TooltipConfigLine>?): Map<String, String> {
            return tooltipSettings
                ?.filterNot { it.name.isNullOrEmpty() }
                ?.filter { !TooltipConfigLine.hasAesPrefix(it.name!!) }
                ?.associateBy( {it.name!!}, { it.label } )
                ?: emptyMap()
        }

        fun getStaticLines(tooltipConfigSettings: List<TooltipConfigLine>?): List<String> {
            return tooltipConfigSettings
                ?.filter { it.name.isNullOrEmpty() }
                ?.map { it.label }
                ?: emptyList()
        }

        val result = mutableMapOf<ValueType, BaseLineGenerator>()

        val setTypes = mutableSetOf<ValueType>()
        tooltipSettings
            .map { it.name }
            .forEach { setTypes.add(getTypeByName(it)) }

        for (type in setTypes) {
            result[type] = when (type) {
                ValueType.AES -> AesLinesGenerator(contextualMapping, getAesLabels(tooltipSettings))
                ValueType.VAR -> VarLineGenerator(contextualMapping.dataAccess, getVarLabels(tooltipSettings))
                ValueType.TEXT -> StaticTextLineGenerator(getStaticLines(tooltipSettings))
                else -> error("Unknown parameter")
            }
        }
        return result
    }
}