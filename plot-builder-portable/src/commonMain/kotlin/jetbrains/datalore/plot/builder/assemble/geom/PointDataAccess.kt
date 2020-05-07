/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.assemble.geom

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.Scale
import jetbrains.datalore.plot.base.interact.AbstractDataValue
import jetbrains.datalore.plot.base.interact.AbstractDataValue.InteractContext
import jetbrains.datalore.plot.base.interact.MappedDataAccess

internal class PointDataAccess(
    private val data: DataFrame,
    val aesVariables: Map<Aes<*>, DataFrame.Variable>,
    val aesScales: Map<Aes<*>, Scale<*>?>
) : MappedDataAccess {

    override fun getMappedData(dataValue: AbstractDataValue, index: Int): MappedDataAccess.MappedData? {
        return dataValue.getMappedData(
            InteractContext(
                data,
                aesVariables,
                aesScales
            ),
            index
        )
    }

    override val mappedAes: Set<Aes<*>> = HashSet(aesVariables.keys)

    override fun isAesMapped(aes: Aes<*>): Boolean {
        return aesVariables.containsKey(aes)
    }
}
