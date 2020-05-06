/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.interact

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.Scale

interface AbstractDataValue {

    class TooltipContext(
        val data: DataFrame,
        val index: Int,
        val variables: Map<Aes<*>, DataFrame.Variable>,
        val scales: Map<Aes<*>, Scale<*>?>
    )

    fun getValue(context: TooltipContext): DataAccess.ValueData?

    fun getValueName(): String
}