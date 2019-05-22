package jetbrains.datalore.visualization.plot.base.render

import jetbrains.datalore.visualization.plot.base.Aes
import kotlin.reflect.KProperty1
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.test.Test
import kotlin.test.assertEquals

class AesReflectTest {
    @Test
    fun checkName() {
        val aesType = Aes::class.createType(listOf(KTypeProjection.STAR))
        val companion = Aes::class.companionObject!!
        val properties = companion.declaredMemberProperties
        var count = 0
        for (p in properties) {
            val pType = p.returnType
            if (pType.isSubtypeOf(aesType)) {
                count++
                @Suppress("UNCHECKED_CAST")
                val p1 = p as KProperty1<Aes.Companion, Aes<*>>
                val value = p1.get(Aes)

                // check that the name passed to constructor corresponds to the field name
                assertEquals(p.name.toLowerCase(), value.name)
            }
        }

        assertEquals(count, Aes.values().size)
    }
}