/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.ValueSource

class ConstantAes(
    aes: Aes<*>,
    private val label: String,
    format: String
) : MappedAes(aes) {

    private val myFormatter = if (format.isEmpty()) null else LineFormatter(format)

    override fun getMappedData(index: Int, dataAccess: MappedDataAccess): ValueSource.ValueSourceData? {
        val mappedData = super.getMappedData(index, dataAccess) ?: return null

        val value = myFormatter?.format(mappedData) ?: mappedData.value
        val label = LineFormatter.chooseLabel(dataLabel = mappedData.label, userLabel = label)
        return ValueSource.ValueSourceData(
            label = label,
            value = value,
            isContinuous = mappedData.isContinuous,
            aes = aes
        )
    }
}