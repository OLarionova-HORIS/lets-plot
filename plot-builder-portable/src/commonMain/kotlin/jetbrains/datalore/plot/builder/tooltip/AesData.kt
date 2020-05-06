/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.Scale
import jetbrains.datalore.plot.base.interact.DataAccess

class AesData(
    aes: Aes<*>,
    private val label: String,
    private val format: String
) : AesValue(aes) {

    override fun getValue(
        data: DataFrame,
        index: Int,
        variable: DataFrame.Variable,
        scale: Scale<*>
    ): DataAccess.ValueData {

        val value = super.getValue(data, index, variable, scale)
        return DataAccess.ValueData(
            label = label,
            value = value.value, //todo use formatter
            isContinuous = scale.isContinuous
        )
    }
}