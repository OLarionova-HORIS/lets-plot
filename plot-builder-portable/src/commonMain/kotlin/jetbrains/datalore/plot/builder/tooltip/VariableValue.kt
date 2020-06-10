/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.base.numberFormat.NumberFormat
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.interact.DataContext
import jetbrains.datalore.plot.base.interact.ValueSource
import jetbrains.datalore.plot.base.interact.ValueSource.DataPoint

class VariableValue(
    private val name: String,
    private val label: String? = null,
    private val format: String = ""
) : ValueSource {

    private lateinit var myDataFrame: DataFrame

    override fun setDataContext(dataContext: DataContext) {
        myDataFrame = dataContext.dataFrame
    }

    override fun getDataPoint(index: Int): DataPoint? {
        val variable = myDataFrame.variables().find { it.name == name } ?: return null
        val originalValue = myDataFrame[variable][index]
        val isContinuous = myDataFrame.isNumeric(variable)
        return DataPoint(
            label = LineFormatter.chooseLabel(dataLabel = name, userLabel = label),
            value = format(originalValue, isContinuous),
            isContinuous = isContinuous,
            aes = null,
            isAxis = false,
            isOutlier = false
        )
    }

    private fun format(originalValue: Any?, isContinuous:Boolean): String {
        // todo Need proper formatter.
        val strValue = originalValue.toString()
        return  when {
            isContinuous -> NumberFormat(format).apply(strValue.toFloat())
            else -> strValue
        }
    }

    fun getVariableName(): String {
        return name
    }
}