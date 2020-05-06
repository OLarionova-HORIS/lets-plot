/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.DataAccess
import jetbrains.datalore.plot.base.interact.DataPointFormatter
import jetbrains.datalore.plot.base.interact.TooltipContent

class AesFormatter(aes: Aes<*>, private val isOutlier: Boolean) : DataPointFormatter {
    private val aesDataValue = AesValue(aes)

    override fun format(dataAccess: DataAccess, index: Int): TooltipContent.TooltipLine? {
        val valueData = dataAccess.getValueData(aesDataValue, index) ?: return null
        val line = createLine(dataAccess, index, valueData, valueData.label)
        return TooltipContent.TooltipLine(
            line = line,
            isForAxis = false,
            isOutlier = isOutlier,
            forAesName = aesDataValue.getValueName()
        )
    }

    companion object {
        internal fun makeLine(label: String, value: String): String {
            return if (label.isNotEmpty()) "$label: $value" else value
        }

        internal fun createLine(
            dataAccess: DataAccess,
            index: Int,
            valueData: DataAccess.ValueData,
            label: String
        ): String {

            val myShortLabels = listOf(Aes.X, Aes.Y).map {
                val value = dataAccess.getValueData(AesValue(it), index)
                value?.label
            }.filterNotNull()

            fun isShortLabel() = myShortLabels.contains(label)

            fun shortText() = valueData.value

            fun fullText() = "$label: ${valueData.value}"

            return when {
                label.isEmpty() -> shortText()
                isShortLabel() -> shortText()
                else -> fullText()
            }
        }
    }
}