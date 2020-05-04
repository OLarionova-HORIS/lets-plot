/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.interact

import jetbrains.datalore.plot.base.DataFrame


class TooltipContext(
    val data: DataFrame,
    val index: Int
    //add bindings
    //...
)

interface AbstractDataValue {
    fun getValue(visitor: AbstractDataValueVisitor, tooltipContext: TooltipContext) = visitor.getValue(this, tooltipContext)
}

interface AbstractDataValueVisitor {
    fun getValue(dataValue: AbstractDataValue, tooltipContext: TooltipContext):  MappedDataAccess.MappedData
}