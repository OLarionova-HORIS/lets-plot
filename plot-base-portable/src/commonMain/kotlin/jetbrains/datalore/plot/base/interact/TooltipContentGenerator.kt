/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.interact

import jetbrains.datalore.plot.base.Aes

interface TooltipContentGenerator {

    fun generateLines(index: Int, aesHint: List<Aes<*>>): List<TooltipLine>

    class TooltipLine(
        val line: String,
        val isForAxis: Boolean,
        val isOutlier: Boolean,
        val forAesName: String? = null
    )
}
