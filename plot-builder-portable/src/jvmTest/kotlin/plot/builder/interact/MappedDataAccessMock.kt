/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.interact

import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.DataAccess
import jetbrains.datalore.plot.base.interact.DataAccess.ValueData
import jetbrains.datalore.plot.builder.interact.mockito.eq
import jetbrains.datalore.plot.builder.tooltip.AesValue
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class MappedDataAccessMock {

    private val mappedAes = HashSet<Aes<*>>()
    val dataAccess: DataAccess = mock(DataAccess::class.java)

    init {
        `when`(dataAccess.mappedAes)
                .thenReturn(getMappedAes())
    }

    fun <T> add(mapping: Mapping<T>): MappedDataAccessMock {
        return add(mapping, null)
    }

    fun <T> add(mapping: Mapping<T>, index: Int?): MappedDataAccessMock {
        val aes = mapping.aes

        if (index == null) {
            `when`(dataAccess.getValueData(eq(AesValue(aes)), anyInt()))
                    .thenReturn(mapping.createMappedData())
        } else {
            `when`(dataAccess.getValueData(eq(AesValue(aes)), eq(index)))
                    .thenReturn(mapping.createMappedData())
        }

        `when`(dataAccess.isAesMapped(eq(aes)))
                .thenReturn(true)

        getMappedAes().add(aes)

        return this
    }

    internal fun remove(aes: Aes<*>) {
        getMappedAes().remove(aes)

        `when`<ValueData>(dataAccess.getValueData(eq(AesValue(aes)), anyInt()))
                .thenReturn(null)

        `when`(dataAccess.isAesMapped(eq(aes)))
                .thenReturn(false)
    }

    fun getMappedAes(): MutableSet<Aes<*>> {
        return mappedAes
    }

    class Mapping<T> internal constructor(internal val aes: Aes<T>,
                                          private val label: String,
                                          private val value: String,
                                          private val isContinuous: Boolean) {
        fun longTooltipText(): String {
            return "$label: $value"
        }

        fun shortTooltipText(): String {
            return value
        }

        internal fun createMappedData(): ValueData {
            return ValueData(label, value, isContinuous)
        }
    }

    class Variable {
        private var name = ""
        private var value = ""
        private var isContinuous: Boolean = false

        fun name(v: String): Variable {
            this.name = v
            return this
        }

        fun value(v: String): Variable {
            this.value = v
            return this
        }

        fun isContinuous(v: Boolean): Variable {
            this.isContinuous = v
            return this
        }

        fun <T> mapping(aes: Aes<T>): Mapping<T> {
            return Mapping(aes, name, value, isContinuous)
        }

    }

    companion object {

        fun variable(): Variable {
            return Variable()
        }
    }
}
