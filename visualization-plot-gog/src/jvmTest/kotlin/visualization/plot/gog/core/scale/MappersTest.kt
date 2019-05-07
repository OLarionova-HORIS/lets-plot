package jetbrains.datalore.visualization.plot.gog.core.scale

import jetbrains.datalore.base.gcommon.collect.ClosedRange
import org.junit.Assert
import org.junit.Test

class MappersTest {
    private fun checkWithZeroDomain(rangeLow: Double, rangeHigh: Double) {
        val zeroDomain = ClosedRange.closed(10.0, 10.0)
        val mapper = Mappers.linear(zeroDomain, rangeLow, rangeHigh, Double.NaN)
        // The range's midpoint in expected
        Assert.assertEquals(1.5, mapper(10.0), 0.0)
        Assert.assertEquals(1.5, mapper(9.0), 0.0)
        Assert.assertEquals(1.5, mapper(11.0), 0.0)
    }

    @Test
    fun linearWithPositiveInfiniteSlop() {
        checkWithZeroDomain(1.0, 2.0)
    }

    @Test
    fun linearWithNegativeInfiniteSlop() {
        checkWithZeroDomain(2.0, 1.0)
    }

    @Test
    fun linearWithNaInput() {
        val naValue = 888.0
        val mapper = Mappers.linear(ClosedRange.closed(0.0, 1.0), 0.0, 1.0, naValue)
//        Assert.assertEquals(naValue, mapper(null), 0.0)
        Assert.assertEquals(naValue, mapper(Double.NaN), 0.0)
        Assert.assertEquals(naValue, mapper(Double.NEGATIVE_INFINITY), 0.0)
        Assert.assertEquals(naValue, mapper(Double.POSITIVE_INFINITY), 0.0)
    }

    @Test
    fun nullable() {
        val expected = Any()
        val notNullable = { n: Double? ->
            if (n == null) {
                Assert.fail("null argument not expected")
            }
            n
        }

        val result = Mappers.nullable(
                notNullable,
                expected)(null)
        Assert.assertEquals(expected, result)
    }
}