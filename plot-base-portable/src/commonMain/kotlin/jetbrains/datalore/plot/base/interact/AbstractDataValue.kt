/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.interact

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.Scale

interface AbstractDataValue {

    class InteractContext(
        val data: DataFrame,
        val variables: Map<Aes<*>, DataFrame.Variable>,
        val scales: Map<Aes<*>, Scale<*>?>
    )

    fun getMappedData(context: InteractContext, index: Int): MappedDataAccess.MappedData?

    fun getValueName(): String
}