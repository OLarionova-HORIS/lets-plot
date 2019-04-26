package jetbrains.datalore.visualization.plot.gog.core.scale.breaks

import jetbrains.datalore.base.gcommon.base.Preconditions.checkArgument
import jetbrains.datalore.base.gcommon.base.Preconditions.checkState
import jetbrains.datalore.base.gcommon.collect.ClosedRange
import java.util.*

class QuantizeScale<T> : WithFiniteOrderedOutput<T> {
    private var myHasDomain: Boolean = false
    private var myDomainStart: Double = 0.toDouble()
    private var myDomainEnd: Double = 0.toDouble()
    private var myOutputValues: List<T>? = null

    override val outputValues: List<T>
        get() = Collections.unmodifiableList(myOutputValues!!)

    //return Arrays.asList(ClosedRange.closedOpen(myDomainStart, myDomainEnd));
    //    double error = bucketSize / 10;   // prevent creating of 1 extra bucket
    /*
    double upperBound = myDomainStart;
    while (upperBound < myDomainEnd - error) {
      double lowerBound = upperBound;
      upperBound = lowerBound + bucketSize;
      ClosedRange<Double> bucket = ClosedRange.closedOpen(lowerBound, upperBound);
      list.add(bucket);
    }
*///ClosedRange<Double> bucket = ClosedRange.closedOpen(myDomainStart + bucketSize * i, myDomainStart + bucketSize * (i + 1));
    // ToDo: move inside the cycle
    // last bucket - closed
    val domainQuantized: List<ClosedRange<Double>>
        get() {
            if (myDomainStart == myDomainEnd) {
                return Arrays.asList(ClosedRange.closed(myDomainStart, myDomainEnd))
            }

            val list = ArrayList<ClosedRange<Double>>()
            val numBuckets = myOutputValues!!.size
            val bucketSize = bucketSize()
            for (i in 0 until numBuckets - 1) {
                val bucket = ClosedRange.closed(myDomainStart + bucketSize * i, myDomainStart + bucketSize * (i + 1))
                list.add(bucket)
            }
            val bucket = ClosedRange.closed(myDomainStart + bucketSize * (numBuckets - 1), myDomainEnd)
            list.add(bucket)
            return list
        }

    /**
     * Set the scale's input domain.
     */
    fun domain(start: Double, end: Double): QuantizeScale<T> {
        checkArgument(start <= end, "Domain start must be less then domain end: $start > $end")
        myHasDomain = true
        myDomainStart = start
        myDomainEnd = end
        return this
    }

    /**
     * Scale's output 'quantized' values
     */
    fun range(values: Collection<T>): QuantizeScale<T> {
        myOutputValues = ArrayList(values)
        return this
    }

    fun quantize(v: Double): T {
        return myOutputValues!![outputIndex(v)]
    }

    private fun outputIndex(v: Double): Int {
        checkState(myHasDomain, "Domain not defined.")
        checkState(myOutputValues != null && !myOutputValues!!.isEmpty(), "Output values are not defined.")
        val bucketSize = bucketSize()
        val index = ((v - myDomainStart) / bucketSize).toInt()
        val maxIndex = myOutputValues!!.size - 1
        return Math.max(0, Math.min(maxIndex, index))
    }

    override fun getOutputValueIndex(domainValue: Any): Int {
        return if (domainValue is Number) {
            outputIndex(domainValue.toDouble())
        } else -1
    }

    override fun getOutputValue(domainValue: Any): T? {
        return if (domainValue is Number) {
            quantize(domainValue.toDouble())
        } else null
    }

    private fun bucketSize(): Double {
        return (myDomainEnd - myDomainStart) / myOutputValues!!.size
    }
}