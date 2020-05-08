/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.assemble.geom

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.Scale
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.ValueSource
import jetbrains.datalore.plot.base.interact.ValueSource.InteractContext

internal class PointDataAccess(
    private val data: DataFrame,
    val aesVariables: Map<Aes<*>, DataFrame.Variable>,
    val aesScales: Map<Aes<*>, Scale<*>?>
) : MappedDataAccess {

    override fun getMappedData(dataValue: ValueSource, index: Int): MappedDataAccess.MappedData? {
        return dataValue.getMappedData(
            index,
            InteractContext(
                data,
                aesVariables,
                aesScales
            )
        )
    }

    override val mappedAes: Set<Aes<*>> = HashSet(aesVariables.keys)

    override fun isAesMapped(aes: Aes<*>): Boolean {
        return aesVariables.containsKey(aes)
    }
}
