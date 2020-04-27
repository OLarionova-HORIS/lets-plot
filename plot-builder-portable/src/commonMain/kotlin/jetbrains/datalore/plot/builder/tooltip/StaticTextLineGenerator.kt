/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.interact.TooltipContentGenerator

class StaticTextLineGenerator(val myStaticLines: List<String>)
    : TooltipContentBuilder.BaseLineGenerator() {

   override fun generateLines(index: Int): List<TooltipContentGenerator.TooltipLine> {
        return myStaticLines.map {
            TooltipContentGenerator.TooltipLine(
                line = it,
                isForAxis = false,
                isOutlier = false
            )
        }
    }
}