package jetbrains.datalore.visualization.plot.base.scale.transform

import jetbrains.datalore.base.gcommon.collect.ClosedRange
import jetbrains.datalore.visualization.plot.base.Transform
import jetbrains.datalore.visualization.plot.base.scale.BreaksGenerator
import jetbrains.datalore.visualization.plot.base.scale.MapperUtil
import jetbrains.datalore.visualization.plot.base.scale.ScaleBreaks

open class FunTransform(
        private val myFun: (Double?) -> Double?,
        private val myInverse: (Double?) -> Double?) :
        Transform, BreaksGenerator {

    override fun apply(rawData: List<*>): List<Double?> {
        return rawData.map { myFun(it as Double) }
    }

    override fun applyInverse(v: Double?): Any? {
        return myInverse(v)
    }

    override fun generateBreaks(domainAfterTransform: ClosedRange<Double>, targetCount: Int): ScaleBreaks {
        val domainBeforeTransform = MapperUtil.map(domainAfterTransform) { myInverse(it) }
        val originalBreaks = LinearBreaksGen().generateBreaks(domainBeforeTransform, targetCount)
        val domainValues = originalBreaks.domainValues
        val transformValues = ArrayList<Double>()
        for (domainValue in domainValues) {
            val transformed = myFun(domainValue)
            transformValues.add(transformed!!)
        }

        return ScaleBreaks(domainValues, transformValues, originalBreaks.labels)
    }
}
