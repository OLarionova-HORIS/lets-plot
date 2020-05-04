/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.interact.AbstractDataValue
import jetbrains.datalore.plot.base.interact.DataAccess

class StaticValue(private val text: String) : AbstractDataValue {

    override fun getValueName(): String {
        return text
    }

    override fun getValue(context: AbstractDataValue.TooltipContext): DataAccess.ValueData {
        return DataAccess.ValueData(
            label = "",
            value = text,
            isContinuous = false
        )
    }
}
