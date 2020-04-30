/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.builder.tooltip.DataPointFormatter.ValueType

class DataPointFormatterBuilder(tooltipSettings: List<TooltipConfigLine>?) {

    val dataFormatters = createDataFormatters(tooltipSettings)

    private fun createDataFormatters(tooltipSettings: List<TooltipConfigLine>?): List<DataPointFormatter>? {
        return tooltipSettings?.map {
            val names = it.names.map { name -> getTypeNamePair(name) }
            DataPointFormatter(names, it.label, it.format)
        }
    }

    private fun getTypeNamePair(name: String?): Pair<String, ValueType> {
        return when {
            name.isNullOrEmpty() -> Pair("", ValueType.TEXT)
            TooltipConfigLine.hasAesPrefix(name) -> Pair(TooltipConfigLine.detachAesName(name), ValueType.AES)
            else -> Pair(name, ValueType.VAR)
        }
    }
}