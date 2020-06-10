/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.config

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.ValueSource
import jetbrains.datalore.plot.builder.tooltip.*
import jetbrains.datalore.plot.config.Option.Layer.TOOLTIP_FORMATS
import jetbrains.datalore.plot.config.Option.Layer.TOOLTIP_LINES

class TooltipConfig(opts: Map<*, *>) : OptionsAccessor(opts) {
    fun createTooltips(): List<TooltipLineSpecification>? {
        return if (!has(TOOLTIP_LINES)) {
            null
        } else {
            TooltipConfigParseHelper(
                getStringList(TOOLTIP_LINES),
                getMap(TOOLTIP_FORMATS)
            ).parse()
        }
    }

    private inner class TooltipConfigParseHelper(
        private val tooltipLines: List<String>,
        private val tooltipFormats: Map<*, *>
    ) {
        internal fun parse(): List<TooltipLineSpecification> {
            return tooltipLines.map(::parseLine)
        }

        private fun parseLine(tooltipLine: String): TooltipLineSpecification {
            val label = if (tooltipLine.contains(LABEL_SEPARATOR)) {
                tooltipLine.substringBefore(LABEL_SEPARATOR)
            } else {
                null
            }
            val value = tooltipLine.substringAfter(LABEL_SEPARATOR)
            val valueSourceNames = RE_PATTERN.findAll(value).map {
                val matched = it.groupValues[MATCHED_INDEX]
                matched.removePrefix(VALUE_SOURCE_PREFIX)
            }.toList()
            val linePattern = RE_PATTERN.replace(value) { REPLACE_PATTERN }

            if (valueSourceNames.size == 1 && linePattern == REPLACE_PATTERN) {
                return TooltipLineSpecification.singleValueLine(
                    label = null,
                    linePattern = "",
                    datum = createValueSource(valueSourceNames.first(), label)
                )
            }
            return TooltipLineSpecification.multiValueLine(
                label = label,
                linePattern = linePattern,
                data = valueSourceNames.map { valueSourceName ->
                    createValueSource(valueSourceName, null)
                }
            )
        }

        private fun createValueSource(name: String, label: String?): ValueSource {
            val valueName = name.removeSurrounding("{", "}")

            fun getAesByName(aesName: String): Aes<*> {
                return Aes.values().find { it.name == aesName } ?: error("$aesName is not aes name")
            }

            val format = tooltipFormats[valueName] as String? ?: ""
            return when {
                valueName.startsWith("aes@") -> {
                    val aes = getAesByName(valueName.removePrefix("aes@"))
                    when {
                        format.isEmpty() && needDefaultLabel(label) -> MappedAes(aes)
                        else -> ConstantAes(aes, label, format)
                    }
                }
                else -> VariableValue(valueName, label, format)
            }
        }
    }

    companion object {
        private const val VALUE_SOURCE_PREFIX = "$"
        private const val LABEL_SEPARATOR = "|"
        private const val MATCHED_INDEX = 0
        private const val REPLACE_PATTERN = "{}"
        private val RE_PATTERN = Regex("""\$[\w@]*(\{[\w\s]+})?""")

        private fun needDefaultLabel(label: String?): Boolean {
            return label != null && label == "@"
        }
    }
}