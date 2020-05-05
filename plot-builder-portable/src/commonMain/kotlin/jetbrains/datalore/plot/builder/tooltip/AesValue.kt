/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.base.gcommon.base.Preconditions.checkArgument
import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.Scale
import jetbrains.datalore.plot.base.interact.AbstractDataValue
import jetbrains.datalore.plot.base.interact.DataAccess
import jetbrains.datalore.plot.base.scale.ScaleUtil
import jetbrains.datalore.plot.common.data.SeriesUtil

open class AesValue(private val aes: Aes<*>) : AbstractDataValue {

    override fun getValueName(): String {
        return aes.name
    }

    override fun getValue(context: AbstractDataValue.TooltipContext): DataAccess.ValueData {
        checkArgument(isMapped(context.variables, context.scales), "Not mapped: $aes")

        val binding = context.variables.getValue(aes)
        val scale = context.scales.getValue(aes)!!

        return getValue(context.data, context.index, binding, scale)
    }

    protected open fun getValue(
        data: DataFrame,
        index: Int,
        variable: DataFrame.Variable,
        scale: Scale<*>
    ): DataAccess.ValueData {
        val originalValue = getOriginalValue(data, index, variable, scale)
        return DataAccess.ValueData(
            label = scale.name,
            value = formatter(data, variable, scale).invoke(originalValue),
            isContinuous = scale.isContinuous
        )
    }

    protected fun getOriginalValue(data: DataFrame, index: Int, variable: DataFrame.Variable, scale: Scale<*>): Any? {
        return variable
            .let { data.getNumeric(variable)[index] }
            .let { value -> scale.transform.applyInverse(value) }
    }

    fun isMapped(variables: Map<Aes<*>, DataFrame.Variable>, scales: Map<Aes<*>, Scale<*>?>): Boolean {
        return variables.containsKey(aes) && scales.containsKey(aes)
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
        return if (other !is AesValue) false else aes == other.aes
    }
}
