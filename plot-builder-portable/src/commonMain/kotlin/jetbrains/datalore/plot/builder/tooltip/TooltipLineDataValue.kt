/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

import jetbrains.datalore.base.gcommon.base.Preconditions.checkArgument
import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.interact.AbstractDataValue
import jetbrains.datalore.plot.base.interact.AbstractDataValueVisitor
import jetbrains.datalore.plot.base.interact.MappedDataAccess.MappedData
import jetbrains.datalore.plot.base.interact.TooltipContext
import jetbrains.datalore.plot.builder.VarBinding


// Aes
open class AesValue(protected val aes: Aes<*>) : AbstractDataValue {

    open fun getValue(data: DataFrame, index: Int, bindings: List<VarBinding>): MappedData {
        checkArgument(isMapped(bindings), "Not mapped: $aes")

        val binding = bindings.find { it.aes == aes }
        val scale = binding?.scale!!
        val originalValue = getOriginalValue(data, index, binding)
        return MappedData(
            label = scale.name,
            value = originalValue.toString(), //todo default formatter
            isContinuous = scale.isContinuous
        )
    }

    protected fun getOriginalValue(data: DataFrame, index: Int, binding: VarBinding?): Any? {
        val scale = binding?.scale!!

        return binding.variable
            .let { variable -> data.getNumeric(variable)[index] }
            .let { value -> scale.transform.applyInverse(value) }
    }

    protected fun isMapped(bindings: List<VarBinding>): Boolean {
        return bindings.map { it.aes }.contains((aes))
    }
}

// Aes with predefined label and formatter
class AesData(
    aes: Aes<*>,
    private val label: String,
    private val format: String
) : AesValue(aes) {

    override fun getValue(data: DataFrame, index: Int, bindings: List<VarBinding>): MappedData {
        checkArgument(isMapped(bindings), "Not mapped: $aes")

        val binding = bindings.find { it.aes == aes }
        val scale = binding?.scale!!

        val originalValue = getOriginalValue(data, index, binding)

        return MappedData(
            label = label,
            value = originalValue.toString(), //todo use formatter
            isContinuous = scale.isContinuous
        )
    }
}

// Variable
class VariableValue(private val name: String) : AbstractDataValue {

    fun getValue(data: DataFrame, index: Int): MappedData {
        val variable = data.variables().find { it.name == name }
        val value = if (variable != null) {
            data[variable][index].toString()
        } else {
            ""
        }
        return MappedData(
            label = name,
            value = value,
            isContinuous = false
        )
    }
}

// Static text
class StaticValue(private val text: String) : AbstractDataValue {

    fun getValue(): MappedData {
        return MappedData(
            label = "",
            value = text,
            isContinuous = false
        )
    }
}


class DataValueGetter : AbstractDataValueVisitor {

    override fun getValue(dataValue: AbstractDataValue, tooltipContext: TooltipContext): MappedData {
        return when (dataValue) {
            is AesValue -> dataValue.getValue(
                tooltipContext.data,
                tooltipContext.index,
                emptyList()     // add bindings
            )
            is VariableValue -> dataValue.getValue(tooltipContext.data, tooltipContext.index)
            is StaticValue -> dataValue.getValue()
            else -> MappedData("", "", false)
        }
    }
}