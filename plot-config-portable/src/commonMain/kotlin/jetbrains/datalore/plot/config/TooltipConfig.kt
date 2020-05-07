/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.config

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.AbstractDataValue
import jetbrains.datalore.plot.builder.tooltip.*

class TooltipConfig(opts: Map<*, *>) : OptionsAccessor(opts) {

    fun createTooltips(option: String): List<TooltipLineData>? {
        return if (!has(option)) {
            null
        } else {
            getList(option).let(::parseLines)
        }
    }

    private fun parseLines(tooltipLines: List<*>): List<TooltipLineData> {

        class TooltipConfigLine(val names: List<String>, val label: String, val format: String)

        fun parseLine(tooltipLine: Map<*, *>): TooltipConfigLine {
            val value = tooltipLine.get(Option.TooltipLine.VALUE)
            val names = (value as? String)?.let { listOf(it) }
                ?: tooltipLine.getList(Option.TooltipLine.VALUE)?.map { it.toString() }

            val label = tooltipLine.getString(Option.TooltipLine.LABEL) ?: ""
            val format = tooltipLine.getString(Option.TooltipLine.FORMAT) ?: ""
            return TooltipConfigLine(names = names ?: emptyList(), label = label, format = format)
        }

        fun createDataValue(name: String): AbstractDataValue {
            fun getAesByName(aesName: String): Aes<*> {
                return Aes.values().find { it.name == aesName } ?: error("$aesName is not aes name ")
            }

            return when {
                name.startsWith("text@") -> StaticValue((name.removePrefix("text@")))
                name.startsWith("aes@") -> AesValue(getAesByName(name.removePrefix("aes@")))
                name == "@@Y" -> PositionalYValue()
                else -> VariableValue(name)
            }
        }

        return tooltipLines.map { tooltipLine ->
            when {
                tooltipLine is String -> TooltipLineData.singleValueLine(
                    label = "",
                    format = "",
                    datum = createDataValue(tooltipLine)
                )
                tooltipLine is Map<*, *> -> {
                    val line = parseLine(tooltipLine)
                    val values = line.names.map { createDataValue(it) }
                    TooltipLineData.multiValueLine(
                        label = line.label,
                        format = line.format,
                        data = values
                    )
                }
                else -> error("Error tooltip_line parsing")
            }
        }
    }
}