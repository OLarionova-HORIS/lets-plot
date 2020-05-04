/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.base.gcommon.base.Preconditions
import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.interact.DataAccess
import jetbrains.datalore.plot.builder.VarBinding

class AesData(
    aes: Aes<*>,
    private val label: String,
    private val format: String
) : AesValue(aes) {

    override fun getValue(data: DataFrame, index: Int, bindings: List<VarBinding>): DataAccess.ValueData {
        Preconditions.checkArgument(isMapped(bindings), "Not mapped: $aes")

        val binding = bindings.find { it.aes == aes }
        val scale = binding?.scale!!

        val originalValue = getOriginalValue(data, index, binding)

        return DataAccess.ValueData(
            label = label,
            value = originalValue.toString(), //todo use formatter
            isContinuous = scale.isContinuous
        )
    }
}