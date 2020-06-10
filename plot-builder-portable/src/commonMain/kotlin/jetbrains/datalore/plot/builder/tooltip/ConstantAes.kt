/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.base.numberFormat.NumberFormat
import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.ValueSource.DataPoint

class ConstantAes(
    aes: Aes<*>,
    private val label: String?,
    private val format: String
) : MappedAes(aes) {

    override fun getMappedDataPoint(index: Int): DataPoint? {
        val mappedData = super.getMappedDataPoint(index) ?: return null

        val value = when {
            mappedData.isContinuous -> NumberFormat(format).apply(mappedData.value.toFloat())
            else -> mappedData.value
        }
        val label = LineFormatter.chooseLabel(dataLabel = mappedData.label, userLabel = label)
        return DataPoint(
            label = label,
            value = value,
            isContinuous = mappedData.isContinuous,
            aes = mappedData.aes,
            isAxis = mappedData.isAxis,
            isOutlier = mappedData.isOutlier
        )
    }
}