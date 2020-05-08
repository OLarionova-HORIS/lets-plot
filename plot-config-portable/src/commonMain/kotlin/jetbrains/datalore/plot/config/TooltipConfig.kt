/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.config

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.ValueSource
import jetbrains.datalore.plot.builder.tooltip.MappedAes
import jetbrains.datalore.plot.builder.tooltip.StaticValue
import jetbrains.datalore.plot.builder.tooltip.TooltipLineSpecification
import jetbrains.datalore.plot.builder.tooltip.VariableValue
import jetbrains.datalore.plot.config.Option.LayerTooltips.LINES
import jetbrains.datalore.plot.config.Option.TooltipLine

class TooltipConfig(opts: Map<*, *>) : OptionsAccessor(opts) {

    fun createTooltips(): List<TooltipLineSpecification>? {
        return if (!has(LINES)) {
            null
        } else {
            getList(LINES).let(::parseLines)
        }
    }

    private fun parseLines(tooltipLines: List<*>): List<TooltipLineSpecification> {
        return tooltipLines.map { tooltipLine ->
            when (tooltipLine) {
                is String -> TooltipLineSpecification.singleValueLine(
                    label = "",
                    format = "",
                    datum = createValueSource(tooltipLine)
                )
                is Map<*, *> -> {
                    val line = parseLine(tooltipLine)
                    val values = line.value.map { createValueSource(it) }
                    TooltipLineSpecification.multiValueLine(
                        label = line.label,
                        format = line.format,
                        data = values
                    )
                }
                else -> error("Error tooltip_line parsing")
            }
        }
    }

    class TooltipConfigLine(val value: List<String>, val label: String, val format: String)

    private fun parseLine(tooltipLine: Map<*, *>): TooltipConfigLine {
        val value: List<String> = when (val value = tooltipLine[TooltipLine.VALUE]) {
            is String -> listOf(value)
            is List<*> -> value.mapNotNull(Any?::toString)
            else -> error("Unsupported tooltip format type")
        }

        val label = tooltipLine.getString(TooltipLine.LABEL) ?: ""
        val format = tooltipLine.getString(TooltipLine.FORMAT) ?: ""
        return TooltipConfigLine(value = value, label = label, format = format)
    }

    private fun createValueSource(name: String): ValueSource {
        fun getAesByName(aesName: String): Aes<*> {
            return Aes.values().find { it.name == aesName } ?: error("$aesName is not aes name")
        }

        return when {
            name.startsWith("text@") -> StaticValue((name.removePrefix("text@")))
            name.startsWith("aes@") -> MappedAes(getAesByName(name.removePrefix("aes@")))
            else -> VariableValue(name)
        }
    }
}