/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.interact

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame.Variable

interface MappedDataAccess {

    val mappedAes: Set<Aes<*>>

    fun isMapped(aes: Aes<*>): Boolean

    fun <T> getMappedData(aes: Aes<T>, index: Int): MappedData<T>

    fun getVariableByName(variableName: String): Variable?

    fun getVariableData(variable: Variable, index: Int): String

    class MappedData<T>(
            val label: String,
            val value: String,
            val isContinuous: Boolean)
}
