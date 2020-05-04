/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.base.gcommon.base.Preconditions.checkArgument
import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.interact.AbstractDataValue
import jetbrains.datalore.plot.base.interact.DataAccess
import jetbrains.datalore.plot.base.scale.ScaleUtil
import jetbrains.datalore.plot.builder.VarBinding
import jetbrains.datalore.plot.common.data.SeriesUtil

open class AesValue(protected val aes: Aes<*>) : AbstractDataValue {

    open fun getValue(data: DataFrame, index: Int, bindings: List<VarBinding>): DataAccess.ValueData {
        checkArgument(isMapped(bindings), "Not mapped: $aes")

        val binding = bindings.find { it.aes == aes }!!
        val scale = binding.scale!!
        val originalValue = getOriginalValue(data, index, binding)
        return DataAccess.ValueData(
            label = scale.name,
            value = formatter(data, binding).invoke(originalValue),
            isContinuous = scale.isContinuous
        )
    }

    protected fun getOriginalValue(data: DataFrame, index: Int, binding: VarBinding): Any? {
        val scale = binding.scale!!
        return binding.variable
            .let { variable -> data.getNumeric(variable)[index] }
            .let { value -> scale.transform.applyInverse(value) }
    }

    fun isMapped(bindings: List<VarBinding>): Boolean {
        return bindings.map { it.aes }.contains((aes))
    }

    private fun formatter(data: DataFrame, binding: VarBinding):  (Any?) -> String {
        val scale = binding.scale!!
        if (scale.isContinuousDomain) {
            // only 'stat' or 'transform' vars here
            val domain = binding
                .variable
                .run(data::range)
                .run(SeriesUtil::ensureNotZeroRange)

            val formatter = ScaleUtil.getBreaksGenerator(scale).labelFormatter(domain, 100)
            return { value -> value?.let { formatter.invoke(it) } ?: "n/a" }
        } else {
            val labelsMap = ScaleUtil.labelByBreak(scale)
            return { value -> value?.let { labelsMap.getValue(it) } ?: "n/a" }
        }
    }
}
