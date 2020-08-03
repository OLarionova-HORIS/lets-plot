/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.GeomKind
import jetbrains.datalore.plot.base.geom.BoxplotGeom
import jetbrains.datalore.plot.base.interact.GeomTargetLocator
import jetbrains.datalore.plot.base.interact.ValueSource
import jetbrains.datalore.plot.base.pos.PositionAdjustments
import jetbrains.datalore.plot.base.scale.Scales
import jetbrains.datalore.plot.base.stat.BoxplotStat
import jetbrains.datalore.plot.base.stat.Stats
import jetbrains.datalore.plot.builder.GeomLayer
import jetbrains.datalore.plot.builder.VarBinding
import jetbrains.datalore.plot.builder.assemble.GeomLayerBuilder
import jetbrains.datalore.plot.builder.assemble.GeomLayerBuilder.Companion.demoAndTest
import jetbrains.datalore.plot.builder.assemble.PosProvider
import jetbrains.datalore.plot.builder.assemble.geom.GeomProvider
import jetbrains.datalore.plot.builder.interact.GeomInteractionBuilder
import jetbrains.datalore.plot.builder.scale.ScaleProviderHelper
import jetbrains.datalore.plot.config.GeomInteractionUtil
import kotlin.test.Test
import kotlin.test.assertEquals

class TooltipContentGeneratorTest {

    @Test
    fun userTooltips() {
        val tooltipValueSourcesProvider = TooltipValueSourcesProvider()
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
            .addTooltipLine(ConstantValue(value = 3, format = "constant = {.3f}"))

        val geomLayer = buildGeomLayer(tooltipValueSourcesProvider)

        val lines = getGeneralTooltipLines(geomLayer)

        val expectedLines = listOf(
            "mpg data set info:",
            "15.00 (mpg)",
            "US",
            "dodge car (1996)",
            "x/y: 1.600 x 160.0",
            "constant = 3.000"
        )

        assertEquals(expectedLines.size, lines.size, "Wrong lines count in the tooltip")

        for (index in lines.indices) {
            assertEquals(expectedLines[index], lines[index], "Wrong line #$index in the general tooltip")
        }

        val axisTooltips = getAxisTooltips(geomLayer)
        assertEquals(2, axisTooltips.size, "Wrong count of axis tooltips")
    }

    @Test
    fun emptyTooltips() {
        val geomLayer = buildGeomLayer(TooltipValueSourcesProvider())
        val lines = getGeneralTooltipLines(geomLayer)
        assertEquals(0, lines.size, "Wrong lines count in the general tooltip")
    }

    @Test
    fun defaultTooltips() {
        val geomLayer = buildGeomLayer(null)
        val lines = getGeneralTooltipLines(geomLayer)
        //default tooltips: listOf(Aes.COLOR, Aes.SHAPE)
        assertEquals(2, lines.size, "Wrong lines count in the general tooltip")
    }

    @Test
    fun defaultOutlierTooltips() {
        val geomLayer = buildBoxplotLayer(null)

        val lines = getOutlierLines(geomLayer)
        val expectedLines = mapOf(
            Aes.YMAX to "y max: 11.50",
            Aes.UPPER to "upper: 8.65",
            Aes.MIDDLE to "middle: 6.85",
            Aes.LOWER to "lower: 6.10",
            Aes.YMIN to "y min: 4.20"
        )

        assertEquals(expectedLines.size, lines.size, "Wrong count of outlier tooltips")

        for (aes in lines.keys) {
            assertEquals(expectedLines[aes], lines[aes], "Wrong line for ${aes.name} in the outliers")
        }
    }

    @Test
    fun userOutlierTooltips() {
        val tooltipValueSourcesProvider = TooltipValueSourcesProvider()
            .addTooltipLine(MappedAes(aes = Aes.YMIN, label = "min", format = "{.1f}"))
            .addTooltipLine(MappedAes(aes = Aes.MIDDLE, label = "", format = "{.4f}"))
            .addTooltipLine(MappedAes(aes = Aes.YMAX, label = "max", format = "{.1f}"))

        val geomLayer = buildBoxplotLayer(tooltipValueSourcesProvider)

        val lines = getOutlierLines(geomLayer)

        val expectedLines = mapOf(
            Aes.YMAX to "max: 11.5",
            Aes.UPPER to "upper: 8.65",
            Aes.MIDDLE to "6.8500",
            Aes.LOWER to "lower: 6.10",
            Aes.YMIN to "min: 4.2"
        )

        assertEquals(expectedLines.size, lines.size, "Wrong count of outlier tooltips")

        for (aes in lines.keys) {
            assertEquals(expectedLines[aes], lines[aes], "Wrong line for ${aes.name} in the outliers")
        }
    }

    private fun getGeneralTooltipLines(geomLayer: GeomLayer): List<String> {
        val dataPoints = geomLayer.contextualMapping.getDataPoints(index = 0)
        return dataPoints.filterNot(ValueSource.DataPoint::isOutlier).map(ValueSource.DataPoint::line)
    }

    private fun getAxisTooltips(geomLayer: GeomLayer): List<ValueSource.DataPoint> {
        val dataPoints = geomLayer.contextualMapping.getDataPoints(index = 0)
        return dataPoints.filter(ValueSource.DataPoint::isAxis)
    }

    private fun getOutlierLines(geomLayer: GeomLayer): Map<Aes<*>, String> {
        val dataPoints = geomLayer.contextualMapping.getDataPoints(index = 0)
        return dataPoints.filter { it.isOutlier && !it.isAxis }.associateBy({ it.aes!! }, { it.line })
    }

    private fun buildGeomLayer(tooltipValueSourcesProvider: TooltipValueSourcesProvider?): GeomLayer {
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

        val geomInteraction = GeomInteractionBuilder(Aes.values())
            .bivariateFunction(false)
            .tooltipValueSources(tooltipValueSourcesProvider?.tooltipValueSourceList)
            .build()

        return GeomLayerBuilder()
            .stat(Stats.IDENTITY)
            .geom(GeomProvider.point())
            .pos(PosProvider.wrap(PositionAdjustments.identity()))
            .also { bindings.forEach { binding -> it.addBinding(binding) } }
            .contextualMappingProvider(geomInteraction)
            .build(dataFrame)
    }

    private fun buildBoxplotLayer(tooltipValueSourcesProvider: TooltipValueSourcesProvider?): GeomLayer {
        val varX = DataFrame.Variable("x")
        val varY = DataFrame.Variable("y")
        val df = DataFrame.Builder()
            .putNumeric(varX, List(6) { 0.5 })
            .putNumeric(varY, listOf(4.2, 11.5, 7.3, 5.8, 6.4, 10.0))
            .build()

        val outlierAesList = GeomInteractionUtil.createOutlierAesList(GeomKind.BOX_PLOT)

        val geomInteraction = GeomInteractionBuilder(Aes.values())
            .univariateFunction(GeomTargetLocator.LookupStrategy.HOVER)
            .tooltipValueSources(tooltipValueSourcesProvider?.tooltipValueSourceList)
            .tooltipOutliers(outlierAesList)
            .build()

        return demoAndTest()
            .stat(BoxplotStat())
            .geom(GeomProvider.boxplot { BoxplotGeom() })
            .pos(PosProvider.dodge())
            .addBinding(
                VarBinding(
                    varX,
                    Aes.X,
                    Scales.continuousDomainNumericRange(varX.name)
                )
            )
            .addBinding(
                VarBinding(
                    varY,
                    Aes.Y,
                    Scales.continuousDomainNumericRange(varY.name)
                )
            )
            .contextualMappingProvider(geomInteraction)
            .build(df)
    }

    private inner class TooltipValueSourcesProvider {
        val tooltipValueSourceList = mutableListOf<ValueSource>()

        fun addTooltipLine(dataValues: List<ValueSource>, label: String, format: String): TooltipValueSourcesProvider {
            tooltipValueSourceList.add(CompositeValue(dataValues, label, format))
            return this
        }

        fun addTooltipLine(dataValue: ValueSource, label: String, format: String): TooltipValueSourcesProvider {
            addTooltipLine(listOf(dataValue), label, format)
            return this
        }

        fun addTooltipLine(dataValue: ValueSource): TooltipValueSourcesProvider {
            tooltipValueSourceList.add(dataValue)
            return this
        }
    }
}