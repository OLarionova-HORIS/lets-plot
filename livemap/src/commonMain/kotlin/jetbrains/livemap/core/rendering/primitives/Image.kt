/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.livemap.core.rendering.primitives

import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.vis.canvas.Canvas.Snapshot
import jetbrains.datalore.vis.canvas.Context2d

class Image(
    private val snapshot: Snapshot,
    override val origin: DoubleVector,
    override val dimension: DoubleVector
) : RenderBox {

    override fun render(ctx: Context2d) {
        ctx.drawImage(snapshot, 0.0, 0.0, dimension.x, dimension.y)
    }
}