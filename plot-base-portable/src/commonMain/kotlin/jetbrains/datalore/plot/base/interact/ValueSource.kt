/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.interact

import jetbrains.datalore.plot.base.Aes

interface ValueSource {

    class ValueSourceData(
        val label: String,
        val value: String,
        val isContinuous: Boolean,
        val aes: Aes<*>?
    )

    fun makeLine(label: String, value: String): String {
        return if (label.isNotEmpty()) "$label: $value" else value
    }
}