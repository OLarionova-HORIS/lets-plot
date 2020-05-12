/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.interact.TooltipContent
import jetbrains.datalore.plot.base.interact.ValueSource

class StaticValue(private val text: String) : ValueSource {

    fun getValue(): ValueSource.ValueSourceData? {
        return ValueSource.ValueSourceData(
            label = "",
            value = text,
            isContinuous = false,
            aes = null
        )
    }

    fun format(): TooltipContent.TooltipLine? {
        return TooltipContent.TooltipLine(
            text,
            TooltipContent.ValueSourceInfo(
                isContinuous = false,
                aes = null,
                isAxis = false,
                isOutlier = false
            )
        )
    }
}
