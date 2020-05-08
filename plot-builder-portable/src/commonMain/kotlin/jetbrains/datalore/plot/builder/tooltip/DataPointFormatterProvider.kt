/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.interact.DataPointFormatter

open class DataPointFormatterProvider(builder: DataPointFormatterBuilder) {
    val dataFormatters: List<DataPointFormatter>? = builder.dataFormatters
}
