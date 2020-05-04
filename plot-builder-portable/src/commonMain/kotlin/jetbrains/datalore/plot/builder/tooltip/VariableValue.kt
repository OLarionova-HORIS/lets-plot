/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.interact.AbstractDataValue
import jetbrains.datalore.plot.base.interact.DataAccess

class VariableValue(val name: String) : AbstractDataValue {

    fun getValue(data: DataFrame, index: Int): DataAccess.ValueData {
        val variable = data.variables().find { it.name == name }
        val value = if (variable != null) {
            data[variable][index].toString()
        } else {
            ""
        }
        return DataAccess.ValueData(
            label = name,
            value = value,
            isContinuous = false
        )
    }
}