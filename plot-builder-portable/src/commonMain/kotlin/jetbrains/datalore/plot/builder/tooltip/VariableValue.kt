/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.interact.DataContext
import jetbrains.datalore.plot.base.interact.ValueSource
import jetbrains.datalore.plot.base.interact.ValueSource.DataPoint
import jetbrains.datalore.base.gcommon.base.Preconditions.checkState

class VariableValue(
    private val name: String,
    private val label: String = "",
    format: String = ""
) : ValueSource {

    private val myFormatter = if (format.isEmpty()) null else LineFormatter(format)
    private lateinit var myDataFrame: DataFrame
    private lateinit var myVariable: DataFrame.Variable
    private var myIsContinuous: Boolean = false

    override fun setDataContext(dataContext: DataContext) {
        myDataFrame = dataContext.dataFrame

        val variable = myDataFrame.variables().find { it.name == name }
        if (variable != null) {
            myVariable = variable
            myIsContinuous = myDataFrame.isNumeric(myVariable)
        }
    }

    override fun getDataPoint(index: Int): DataPoint? {
        checkState(::myVariable.isInitialized, "Not initialized variable $name")

        val originalValue = myDataFrame[myVariable][index]
        return DataPoint(
            label = LineFormatter.chooseLabel(dataLabel = name, userLabel = label),
            value = format(originalValue, myIsContinuous),
            isContinuous = myIsContinuous,
            aes = null,
            isAxis = false,
            isOutlier = false
        )
    }

    private fun format(originalValue: Any?, isContinuous:Boolean): String {
        // todo Need proper formatter.
        val strValue = originalValue.toString()
        return myFormatter?.format(strValue, isContinuous) ?: strValue
    }

    fun getVariableName(): String {
        return name
    }
}