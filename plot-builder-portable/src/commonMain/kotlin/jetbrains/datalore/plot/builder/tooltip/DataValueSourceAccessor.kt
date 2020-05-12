/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.interact.DataAccessor
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.TooltipContent
import jetbrains.datalore.plot.base.interact.ValueSource

class DataValueSourceAccessor(
    dataFrame: DataFrame,
    mappedDataAccess: MappedDataAccess
): DataAccessor(dataFrame, mappedDataAccess) {

    override fun getSourceData(valueSource: ValueSource, index: Int): ValueSource.ValueSourceData? {
        return when (valueSource) {
            is MappedAes -> valueSource.getMappedData(index, mappedDataAccess)
            is VariableValue -> valueSource.getSourceData(index, dataFrame)
            is StaticValue -> valueSource.getValue()
            else -> null
        }
    }

    override fun getFormattedData(valueSource: ValueSource, index: Int): TooltipContent.TooltipLine? {
        return when (valueSource) {
            is MappedAes -> valueSource.format(index, mappedDataAccess)
            is VariableValue -> valueSource.format(index, dataFrame)
            is StaticValue -> valueSource.format()
            is CompositeData -> valueSource.format(index, this)
            else -> null
        }
    }
}