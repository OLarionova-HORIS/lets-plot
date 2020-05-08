/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.Scale
import jetbrains.datalore.plot.base.interact.MappedDataAccess
import jetbrains.datalore.plot.base.interact.ValueSource
import jetbrains.datalore.plot.base.interact.ValueSource.InteractContext
import jetbrains.datalore.plot.base.scale.ScaleUtil
import jetbrains.datalore.plot.common.data.SeriesUtil

open class MappedAes(private val aes: Aes<*>) : ValueSource {

    override fun getValueName(): String {
        return aes.name
    }

    override fun getMappedData(index: Int, context: InteractContext): MappedDataAccess.MappedData? {
        if (!isMapped(context)) {
            return null
        }
        val binding = context.variables.getValue(aes)
        val scale = context.scales.getValue(aes)!!
        return getValue(context.data, index, binding, scale)
    }

    protected open fun getValue(
        data: DataFrame,
        index: Int,
        variable: DataFrame.Variable,
        scale: Scale<*>
    ): MappedDataAccess.MappedData {
        val originalValue = getOriginalValue(data, index, variable, scale)
        return MappedDataAccess.MappedData(
            label = scale.name,
            value = formatter(data, variable, scale).invoke(originalValue),
            isContinuous = scale.isContinuous
        )
    }

    private fun getOriginalValue(data: DataFrame, index: Int, variable: DataFrame.Variable, scale: Scale<*>): Any? {
        return variable
            .let { data.getNumeric(variable)[index] }
            .let { value -> scale.transform.applyInverse(value) }
    }

    private fun isMapped(context: InteractContext): Boolean {
        return context.variables.containsKey(aes) && context.scales.containsKey(aes)
    }

    private fun formatter(data: DataFrame, variable: DataFrame.Variable, scale: Scale<*>):  (Any?) -> String {
        if (scale.isContinuousDomain) {
            // only 'stat' or 'transform' vars here
            val domain = variable
                .run(data::range)
                .run(SeriesUtil::ensureNotZeroRange)

            val formatter = ScaleUtil.getBreaksGenerator(scale).labelFormatter(domain, 100)
            return { value -> value?.let { formatter.invoke(it) } ?: "n/a" }
        } else {
            val labelsMap = ScaleUtil.labelByBreak(scale)
            return { value -> value?.let { labelsMap.getValue(it) } ?: "n/a" }
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is MappedAes) false else aes == other.aes
    }

    override fun hashCode(): Int {
        return aes.hashCode()
    }
}
