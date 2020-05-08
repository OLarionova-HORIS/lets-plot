/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.ValueSource

class VariableValue(private val name: String) : ValueSource {

    override fun getValueName(): String {
        return name
    }

    override fun getMappedData(index: Int, context: ValueSource.InteractContext): MappedDataAccess.MappedData? {
       return getValue(context.data, index)
    }

    private fun getValue(data: DataFrame, index: Int): MappedDataAccess.MappedData? {
        val variable = data.variables().find { it.name == name } ?: return null
        return MappedDataAccess.MappedData(
            label = name,
            value = data[variable][index].toString(),
            isContinuous = false
        )
    }
}