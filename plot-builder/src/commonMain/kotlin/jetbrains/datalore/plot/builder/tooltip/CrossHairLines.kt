/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.base.geometry.DoubleRectangle
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.base.values.Color
import jetbrains.datalore.plot.base.render.svg.SvgComponent
import jetbrains.datalore.vis.svg.*

class CrossHairLines(coord: DoubleVector, geomBounds: DoubleRectangle) : SvgComponent() {
    private val myLines = listOf(
        SvgLineElement(coord.x, geomBounds.bottom, coord.x, geomBounds.top),
        SvgLineElement(geomBounds.left, coord.y, geomBounds.right, coord.y)
    )

    override fun buildComponent() {
        myLines.forEach {
            add(it)
            it.strokeColor().set(Color.LIGHT_GRAY)
            it.strokeWidth().set(1.0)
        }
    }
}