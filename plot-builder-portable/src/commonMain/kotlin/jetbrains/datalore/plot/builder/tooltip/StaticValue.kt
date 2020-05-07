/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.interact.AbstractDataValue
import jetbrains.datalore.plot.base.interact.MappedDataAccess

class StaticValue(private val text: String) : AbstractDataValue {

    override fun getValueName(): String {
        return text
    }

    override fun getMappedData(context: AbstractDataValue.InteractContext, index: Int): MappedDataAccess.MappedData? {
        return MappedDataAccess.MappedData(
            label = "",
            value = text,
            isContinuous = false
        )
    }
}
