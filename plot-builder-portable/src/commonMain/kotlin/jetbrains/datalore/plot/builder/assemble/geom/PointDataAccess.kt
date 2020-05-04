/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.assemble.geom

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.interact.AbstractDataValue
import jetbrains.datalore.plot.base.interact.DataAccess
import jetbrains.datalore.plot.builder.VarBinding
import jetbrains.datalore.plot.builder.tooltip.AesValue
import jetbrains.datalore.plot.builder.tooltip.StaticValue
import jetbrains.datalore.plot.builder.tooltip.VariableValue

internal class PointDataAccess(
    private val data: DataFrame,
    private val myBindings: List<VarBinding>
) : DataAccess {

    override fun getValueData(dataValue: AbstractDataValue, index: Int): DataAccess.ValueData {
        return when (dataValue) {
            is AesValue -> dataValue.getValue(data, index, myBindings)
            is VariableValue -> dataValue.getValue(data, index)
            is StaticValue -> dataValue.getValue()
            else -> DataAccess.ValueData("", "", false)
        }
    }

    override val mappedAes: Set<Aes<*>> = myBindings.map { it.aes }.toSet()

    override fun isAesMapped(aes: Aes<*>): Boolean {
        return AesValue(aes).isMapped(myBindings)
    }
}
