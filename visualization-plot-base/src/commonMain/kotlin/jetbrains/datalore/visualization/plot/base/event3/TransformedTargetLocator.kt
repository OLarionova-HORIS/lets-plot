package jetbrains.datalore.visualization.plot.base.event3

import jetbrains.datalore.base.gcommon.collect.Lists
import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.visualization.plot.base.Aes

abstract class TransformedTargetLocator(private val myTargetLocator: GeomTargetLocator) : GeomTargetLocator {

    override fun findTargets(coord: DoubleVector): GeomTargetLocator.LocatedTargets? {
        val targetCoord = convertToTargetCoord(coord)
        val locatedTargets = myTargetLocator.findTargets(targetCoord) ?: return null
        return convertLocatedTargets(locatedTargets)
    }

    private fun convertLocatedTargets(locatedTargets: GeomTargetLocator.LocatedTargets): GeomTargetLocator.LocatedTargets {
        return GeomTargetLocator.LocatedTargets(
                convertGeomTargets(locatedTargets.geomTargets),
                convertToPlotDistance(locatedTargets.distance),
                locatedTargets.geomKind,
                locatedTargets.contextualMapping
        )
    }

    private fun convertGeomTargets(geomTargets: List<GeomTarget>): List<GeomTarget> {
        return ArrayList(Lists.transform(geomTargets) { geomTarget ->
            GeomTarget(
                    geomTarget.hitIndex,
                    convertTipLayoutHint(geomTarget.tipLayoutHint),
                    convertTipLayoutHints(geomTarget.aesTipLayoutHints)
            )
        })
    }

    private fun convertTipLayoutHint(hint: TipLayoutHint): TipLayoutHint {
        return TipLayoutHint(
                hint.kind,
                safeConvertToPlotCoord(hint.coord)!!,
                convertToPlotDistance(hint.objectRadius),
                hint.color
        )
    }

    private fun convertTipLayoutHints(tipLayoutHints: Map<Aes<*>, TipLayoutHint>): Map<Aes<*>, TipLayoutHint> {
        val result = HashMap<Aes<*>, TipLayoutHint>()
        tipLayoutHints.forEach { (aes, hint) -> result[aes] = convertTipLayoutHint(hint) }
        return result
    }

    private fun safeConvertToPlotCoord(coord: DoubleVector?): DoubleVector? {
        return if (coord == null) null else convertToPlotCoord(coord)
    }

    protected abstract fun convertToTargetCoord(coord: DoubleVector): DoubleVector

    protected abstract fun convertToPlotCoord(coord: DoubleVector): DoubleVector

    protected abstract fun convertToPlotDistance(distance: Double): Double
}
