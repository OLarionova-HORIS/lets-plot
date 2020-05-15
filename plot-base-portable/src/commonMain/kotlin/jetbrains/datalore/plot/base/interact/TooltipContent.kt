/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.base.interact

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.ValueSource.DataPoint

interface TooltipContent {

    fun getOutlierDataLines(index: Int, outlierAes: List<Aes<*>>, dataContext: DataContext): List<DataPoint>

    fun getGeneralDataLines(index: Int): List<DataPoint>

    fun getAxisDataLines(index: Int): List<DataPoint>
}
