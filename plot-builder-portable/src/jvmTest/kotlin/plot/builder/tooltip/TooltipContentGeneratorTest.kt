/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.pos.PositionAdjustments
import jetbrains.datalore.plot.base.scale.Scales
import jetbrains.datalore.plot.base.stat.Stats
import jetbrains.datalore.plot.builder.GeomLayer
import jetbrains.datalore.plot.builder.VarBinding
import jetbrains.datalore.plot.builder.assemble.GeomLayerBuilder
import jetbrains.datalore.plot.builder.assemble.PosProvider
import jetbrains.datalore.plot.builder.assemble.geom.GeomProvider
import jetbrains.datalore.plot.builder.scale.ScaleProviderHelper
import kotlin.test.Test
import kotlin.test.assertEquals

class TooltipContentGeneratorTest {

    @Test
    fun userTooltips() {
        val formatterProvider: DataPointFormatterProvider = DataPointFormatterBuilder()
            .addTooltipLine(StaticValue(text = "mpg data set info:"))
            .addTooltipLine(MappedAes(aes = Aes.COLOR), label = "", format = "{.2f} (mpg)")
            .addTooltipLine(VariableValue(name = "origin"))
            .addTooltipLine(
                label = "",
                format = "{} car ({})",
                dataValues = listOf(
                    VariableValue("name"),
                    VariableValue("year")
                )
            )
            .addTooltipLine(
                label = "x/y",
                format = "{.3f} x {.1f}",
                dataValues = listOf(
                    MappedAes(Aes.X),
                    MappedAes(Aes.Y)
                )
            )
            .build()

        val geomLayer = buildGeomLayer(formatterProvider)

        val lines = geomLayer.contextualMapping.generateLines(0, emptyList())
        assertEquals(5, lines.size, "Wrong lines count in the tooltip")

        val expectedLines = listOf(
            "mpg data set info:",
            "15.00 (mpg)",
            "US",
            "dodge car (1996)",
            "x/y: 1.600 x 160.0"
        )

        for (index in lines.indices) {
            assertEquals(expectedLines[index], lines[index].line, "Wrong line #$index in the tooltip")
        }
    }

    @Test
    fun emptyTooltips() {
        val geomLayer = buildGeomLayer(null)
        val lines = geomLayer.contextualMapping.generateLines(0, emptyList())
        assertEquals(0, lines.size, "Wrong lines count in the tooltip")
    }

    private fun buildGeomLayer(formatterProvider: DataPointFormatterProvider?): GeomLayer {
        val X = DataFrame.Variable("x")
        val Y = DataFrame.Variable("y")
        val mpg = DataFrame.Variable("mpg")
        val shape = DataFrame.Variable("origin")
        val name = DataFrame.Variable("name")
        val year = DataFrame.Variable("year")
        val dataFrame = DataFrame.Builder()
            .putNumeric(X, listOf(1.6))
            .putNumeric(Y, listOf(160.0))
            .putNumeric(mpg, listOf(15.0))
            .put(shape, listOf("US"))
            .put(name, listOf("dodge"))
            .put(year, listOf("1996"))
            .build()

        val bindings = listOf(
            VarBinding(X, Aes.X, Scales.continuousDomainNumericRange(X.name)),
            VarBinding(Y, Aes.Y, Scales.continuousDomainNumericRange(Y.name)),
            VarBinding(mpg, Aes.COLOR, Scales.continuousDomainNumericRange(mpg.name)),
            VarBinding(shape, Aes.SHAPE, ScaleProviderHelper.createDefault(Aes.SHAPE).createScale(dataFrame, shape))
        )
        return GeomLayerBuilder()
            .stat(Stats.IDENTITY)
            .geom(GeomProvider.point())
            .pos(PosProvider.wrap(PositionAdjustments.identity()))
            .also { bindings.forEach { binding -> it.addBinding(binding) } }
            .also {
                if (formatterProvider != null) {
                    it.dataPointFormatterProvider(formatterProvider)
                }
            }
            .build(dataFrame)
    }
}