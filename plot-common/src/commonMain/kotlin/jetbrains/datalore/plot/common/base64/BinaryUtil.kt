package jetbrains.datalore.plot.common.base64

expect object BinaryUtil {
    fun encodeList(l: List<Double?>): String
    fun decodeList(s: String): List<Double>
}