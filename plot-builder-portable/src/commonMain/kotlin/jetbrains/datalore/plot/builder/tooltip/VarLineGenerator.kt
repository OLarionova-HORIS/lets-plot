/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.TooltipContentGenerator.TooltipLine

class VarLineGenerator(
    private val dataAccess: MappedDataAccess,
    tooltipVarLabels: Map<String, String>
) : TooltipContentBuilder.BaseLineGenerator() {

    private val myTooltipLabels = createTooltipLabels(tooltipVarLabels)

    override fun generateLines(index: Int): List<TooltipLine> {
        val lines = createLines(index)
        return lines.map {
            TooltipLine(
                line = it,
                isForAxis = false,
                isOutlier = false
            )
        }
    }

    fun createLines(index: Int): List<String> {
        val result = mutableListOf<String>()
        for (tooltip in myTooltipLabels) {
            val value = makeLine(tooltip.key, index)
            val label = tooltip.value
            result.add(makeText(label, value))
        }
        return result
    }

    private fun makeLine(variable: DataFrame.Variable, index: Int): String {
        return dataAccess.getVariableData(variable, index)
    }

    private fun makeText(label: String, value: String): String {
        return if (label.isNotEmpty()) "$label: $value" else value
    }

    private fun createTooltipLabels(tooltipVarLabels: Map<String, String>): Map<DataFrame.Variable, String> {
        fun getVarByName(name: String): DataFrame.Variable {
            return dataAccess.getVariableByName(name)!!
        }

        val result = mutableMapOf<DataFrame.Variable, String>()
        tooltipVarLabels
            .forEach { (name, label) ->
                val aes = getVarByName(name)
                result.put(aes, label)
            }
        return result
    }
}
