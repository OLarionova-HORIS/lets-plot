/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.interact

import jetbrains.datalore.plot.base.Aes

interface MappedDataAccess {

    val mappedAes: Set<Aes<*>>

    fun isMapped(aes: Aes<*>): Boolean

    fun <T> getMappedData(aes: Aes<T>, index: Int): MappedData<T>

    fun getAesData(aesName: String, index: Int): String

    fun getVarData(variableName: String, index: Int): String

    class MappedData<T>(
            val label: String,
            val value: String,
            val isContinuous: Boolean)
}
