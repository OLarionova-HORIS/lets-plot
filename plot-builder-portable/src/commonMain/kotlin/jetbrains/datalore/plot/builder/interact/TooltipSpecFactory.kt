/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.interact

import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.base.values.Color
import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.*

class TooltipSpecFactory(
    private val contextualMapping: ContextualMapping,
    private val axisOrigin: DoubleVector) {

    fun create(geomTarget: GeomTarget): List<TooltipSpec> {
        return ArrayList(Helper(geomTarget).tooltipSpecs)
    }

    private inner class Helper(private val myGeomTarget: GeomTarget) {
        internal val tooltipSpecs = ArrayList<TooltipSpec>()
        private val generatedTooltipLines = contextualMapping.generateLines(
            myGeomTarget.hitIndex,
            myGeomTarget.aesTipLayoutHints.map { it.key }
        )

        init {
            initTooltipSpecs()
        }

        private fun initTooltipSpecs() {

            addHintTooltipSpec()

            addCommonTooltipSpec()

            addAxisTooltipSpec()
        }

        private fun tipLayoutHint(): TipLayoutHint {
            return myGeomTarget.tipLayoutHint
        }

        private fun aesTipLayoutHints(): Map<Aes<*>, TipLayoutHint> {
            return myGeomTarget.aesTipLayoutHints
        }

        private fun addAxisTooltipSpec() {
            val axisLines = generatedTooltipLines.filter { it.isForAxis }
            val axis = mapOf(
                Aes.X to axisLines.filter { Aes.X.name == it.forAesName }.map { it.line },
                Aes.Y to axisLines.filter { Aes.Y.name == it.forAesName }.map { it.line }
            )
            axis.forEach { (aes, lines) ->
                if (lines.isNotEmpty()) {
                    val layoutHint = createHintForAxis(aes)
                    tooltipSpecs.add(
                        TooltipSpec(
                            layoutHint = layoutHint,
                            lines = lines,
                            fill = layoutHint.color!!,
                            isOutlier = true
                        )
                    )
                }
            }
        }

        private fun addHintTooltipSpec() {
            val outlierLines = generatedTooltipLines
                .filter { it.isOutlier && !it.isForAxis && it.forAesName != null }

            aesTipLayoutHints().forEach { (aes, hint) ->
                val hintLines = outlierLines
                    .filter { aes.name == it.forAesName }
                    .map { it.line }

                applyTipLayoutHint(
                    text = hintLines,
                    layoutHint = hint,
                    isOutlier = true
                )
            }
        }

        private fun addCommonTooltipSpec() {
            val oulierLines = generatedTooltipLines
                .filter { !it.isOutlier }
                .map { it.line }

            applyTipLayoutHint(text = oulierLines, layoutHint = tipLayoutHint(), isOutlier = false)
        }

        private fun createHintForAxis(aes: Aes<*>): TipLayoutHint {
            if (aes === Aes.X) {
                return TipLayoutHint.xAxisTooltip(DoubleVector(tipLayoutHint().coord!!.x, axisOrigin.y),
                    AXIS_TOOLTIP_COLOR
                )
            }

            if (aes === Aes.Y) {
                return TipLayoutHint.yAxisTooltip(DoubleVector(axisOrigin.x, tipLayoutHint().coord!!.y),
                    AXIS_TOOLTIP_COLOR
                )
            }

            throw IllegalArgumentException("Not an axis aes: $aes")
        }

        private fun applyTipLayoutHint(text: List<String>, layoutHint: TipLayoutHint, isOutlier: Boolean) {
            if (text.isEmpty()) {
                return
            }
            val fill = layoutHint.color ?: tipLayoutHint().color!!
            tooltipSpecs.add(TooltipSpec(layoutHint, text, fill, isOutlier))
        }
    }

    companion object {
        val AXIS_TOOLTIP_COLOR = Color.GRAY
    }
}
