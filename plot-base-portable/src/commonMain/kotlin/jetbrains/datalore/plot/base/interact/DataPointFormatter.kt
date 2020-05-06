/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.interact

interface DataPointFormatter {
    fun format(dataAccess: DataAccess, index: Int): TooltipContent.TooltipLine?
}
