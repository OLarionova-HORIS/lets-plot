/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.base.numberFormat.NumberFormat
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.TooltipContentGenerator

class DataPointFormatter(
    private val varNames: List<Pair<String, ValueType>>,  // var name + type of var
    private val myLabel: String = "",
    private val myFormatPattern: String = ""
) {

    private val myFormatList = RE_PATTERN.findAll(myFormatPattern).map { it.groupValues[1] }.toList()

    enum class ValueType {
        AES, VAR, TEXT, UNKNOWN
    }

    fun format(dataAccess: MappedDataAccess, index: Int): TooltipContentGenerator.TooltipLine {
        val values = varNames.map { (name, type) ->
            if (type == ValueType.AES)
                dataAccess.getAesData(name, index)
            else
                dataAccess.getVarData(name, index)
        }

        val resLine = format(values)
        return TooltipContentGenerator.TooltipLine(line = resLine)
    }

    private fun format(values: List<String>): String {
        var valuesString = values.joinToString { it }
        if (myFormatPattern.isNotEmpty() && myFormatList.size == values.size) {
            var index = 0
            val formatted = RE_PATTERN.replace(myFormatPattern) { match ->
                val formatter = NumberFormat(match.value.removeSurrounding("{", "}"))
                formatter.apply(values[index++].toFloat())
            }
            valuesString = formatted
        }
        return when {
            myLabel.isNotEmpty() && valuesString.isNotEmpty() -> "$myLabel: $valuesString"
            valuesString.isEmpty() -> myLabel
            else -> valuesString
        }
    }

    companion object {
        val RE_PATTERN = """\{([^{}]*)}""".toRegex()
    }
}

