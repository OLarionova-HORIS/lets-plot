package jetbrains.datalore.visualization.plot.gog.plot.coord

import jetbrains.datalore.base.gcommon.collect.ClosedRange
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.base.values.Pair
import jetbrains.datalore.visualization.plot.gog.core.render.CoordinateSystem
import jetbrains.datalore.visualization.plot.gog.core.scale.Scale2
import jetbrains.datalore.visualization.plot.gog.plot.layout.axis.GuideBreaks

interface CoordProvider {
    fun createCoordinateSystem(xDomain: ClosedRange<Double>, xAxisLength: Double, yDomain: ClosedRange<Double>, yAxisLength: Double): CoordinateSystem

    fun buildAxisScaleX(scaleProto: Scale2<Double>, domain: ClosedRange<Double>, axisLength: Double, breaks: GuideBreaks): Scale2<Double>

    fun buildAxisScaleY(scaleProto: Scale2<Double>, domain: ClosedRange<Double>, axisLength: Double, breaks: GuideBreaks): Scale2<Double>

    fun adjustDomains(xDomain: ClosedRange<Double>, yDomain: ClosedRange<Double>, displaySize: DoubleVector): Pair<ClosedRange<Double>, ClosedRange<Double>>
}