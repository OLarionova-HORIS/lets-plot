/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.interact

import jetbrains.datalore.plot.base.Aes

interface DataAccess {

    fun getValueData(dataValue: AbstractDataValue, index: Int): ValueData

    val mappedAes: Set<Aes<*>>

    fun isAesMapped(aes: Aes<*>): Boolean

    class ValueData(
            val label: String,
            val value: String,
            val isContinuous: Boolean)
}
