/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.interact

import jetbrains.datalore.plot.base.Aes

interface MappedDataAccess {

    fun getMappedData(dataValue: AbstractDataValue, index: Int): MappedData?

    val mappedAes: Set<Aes<*>>

    fun isAesMapped(aes: Aes<*>): Boolean

    class MappedData(
        val label: String,
        val value: String,
        val isContinuous: Boolean
    )
}
