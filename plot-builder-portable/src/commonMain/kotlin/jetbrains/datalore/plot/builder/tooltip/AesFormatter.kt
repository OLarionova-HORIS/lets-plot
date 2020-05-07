/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.DataPointFormatter
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.TooltipContent

class AesFormatter(aes: Aes<*>, private val isOutlier: Boolean) : DataPointFormatter {
    private val aesDataValue = AesValue(aes)

    override fun format(dataAccess: MappedDataAccess, index: Int): TooltipContent.TooltipLine? {
        val mappedData = dataAccess.getMappedData(aesDataValue, index) ?: return null
        val line = createLine(dataAccess, index, mappedData, mappedData.label)
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
            dataAccess: MappedDataAccess,
            index: Int,
            mappedData: MappedDataAccess.MappedData,
            label: String
        ): String {

            val myShortLabels = listOf(Aes.X, Aes.Y).map {
                val value = dataAccess.getMappedData(AesValue(it), index)
                value?.label
            }.filterNotNull()

            fun isShortLabel() = myShortLabels.contains(label)

            fun shortText() = mappedData.value

            fun fullText() = "$label: ${mappedData.value}"

            return when {
                label.isEmpty() -> shortText()
                isShortLabel() -> shortText()
                else -> fullText()
            }
        }
    }
}