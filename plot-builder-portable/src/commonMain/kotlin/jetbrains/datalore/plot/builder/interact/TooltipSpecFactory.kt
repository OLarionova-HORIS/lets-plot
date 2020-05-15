/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.interact

import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.base.values.Pair
import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.*
import jetbrains.datalore.plot.base.interact.MappedDataAccess.MappedData
import jetbrains.datalore.plot.builder.presentation.Defaults.Common.Tooltip.AXIS_RADIUS
import jetbrains.datalore.plot.builder.presentation.Defaults.Common.Tooltip.AXIS_TOOLTIP_COLOR

class TooltipSpecFactory(
    private val contextualMapping: ContextualMapping,
    private val axisOrigin: DoubleVector
) {
    fun create(geomTarget: GeomTarget): List<TooltipSpec> {
        return ArrayList(Helper(geomTarget).tooltipSpecs)
    }

    private inner class Helper(private val myGeomTarget: GeomTarget) {
        internal val tooltipSpecs = ArrayList<TooltipSpec>()
        private val myDataAccess: MappedDataAccess = contextualMapping.dataContext.mappedDataAccess

        init {
            initTooltipSpecs()
        }

        private fun initTooltipSpecs() {
            addHintTooltipSpec()

            addGeneralTooltipSpec()

            addAxisTooltipSpec()
        }

        private fun hitIndex(): Int {
            return myGeomTarget.hitIndex
        }

        private fun tipLayoutHint(): TipLayoutHint {
            return myGeomTarget.tipLayoutHint
        }

        private fun aesTipLayoutHints(): Map<Aes<*>, TipLayoutHint> {
            return myGeomTarget.aesTipLayoutHints
        }

        private fun outlierLines(): List<ValueSource.DataPoint> {
            return contextualMapping.getOutlierDataLines(
                hitIndex(),
                aesTipLayoutHints().map { it.key }
            )
        }

        private fun addHintTooltipSpec() {
            val outlierLines = outlierLines()
            aesTipLayoutHints().forEach { (aes, hint) ->
                val hintLines = outlierLines.filter { aes == it.aes }.map { it.line }
                applyTipLayoutHint(
                    text = hintLines,
                    layoutHint = hint,
                    isOutlier = true
                )
            }
        }

        private fun addAxisTooltipSpec() {
            val axisDataPoints = contextualMapping.getAxisDataLines(hitIndex())

            val axis = mapOf(
                Aes.X to axisDataPoints.filter { Aes.X == it.aes }.map { it.value },
                Aes.Y to axisDataPoints.filter { Aes.Y == it.aes }.map { it.value }
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

        private fun addGeneralTooltipSpec() {
            val generalDataPoints = contextualMapping.getGeneralDataLines(hitIndex())

            val aesList = generalDataPoints.mapNotNull { it.aes }
            val hint = outlierLines().mapNotNull { it.aes }
            val aesListForTooltip = removeDiscreteDuplicatedMappings(aesList - hint)

            val lines = mutableListOf<String>()
            generalDataPoints.forEach { dataPoint ->
                val isAesForTooltips = aesListForTooltip.contains(dataPoint.aes)
                if (dataPoint.aes == null || isAesForTooltips) {
                    lines.add(dataPoint.line)
                }
            }
            applyTipLayoutHint(text = lines, layoutHint = tipLayoutHint(), isOutlier = false)
        }

        private fun removeDiscreteDuplicatedMappings(aesWithoutHint: List<Aes<*>>): List<Aes<*>> {
            if (aesWithoutHint.isEmpty()) {
                return emptyList()
            }

            val mappingsToShow = HashMap<String, Pair<Aes<*>, MappedData<*>>>()
            for (aes in aesWithoutHint) {
                if (!isMapped(aes)) {
                    continue
                }

                val mappingToCheck = getMappedData(aes)
                if (!mappingsToShow.containsKey(mappingToCheck.label)) {
                    mappingsToShow[mappingToCheck.label] = Pair(aes, mappingToCheck)
                    continue
                }

                val mappingToShow = mappingsToShow[mappingToCheck.label]?.second
                if (!mappingToShow!!.isContinuous && mappingToCheck.isContinuous) {
                    mappingsToShow[mappingToCheck.label] = Pair(aes, mappingToCheck)
                }
            }
            return mappingsToShow.values.map { pair -> pair.first }
        }

        private fun createHintForAxis(aes: Aes<*>): TipLayoutHint {
            return when(aes) {
                 Aes.X -> TipLayoutHint.xAxisTooltip(
                     coord = DoubleVector(tipLayoutHint().coord!!.x, axisOrigin.y),
                     color = AXIS_TOOLTIP_COLOR,
                     axisRadius = AXIS_RADIUS
                 )
                Aes.Y -> TipLayoutHint.yAxisTooltip(
                    coord = DoubleVector(axisOrigin.x, tipLayoutHint().coord!!.y),
                    color = AXIS_TOOLTIP_COLOR,
                    axisRadius = AXIS_RADIUS
                )
                else -> error("Not an axis aes: $aes")
            }
        }

        private fun applyTipLayoutHint(text: List<String>, layoutHint: TipLayoutHint, isOutlier: Boolean) {
            if (text.isEmpty()) {
                return
            }
            val fill = layoutHint.color ?: tipLayoutHint().color!!
            tooltipSpecs.add(TooltipSpec(layoutHint, text, fill, isOutlier))
        }

        private fun isMapped(aes: Aes<*>): Boolean {
            return myDataAccess.isMapped(aes)
        }

        private fun <T> getMappedData(aes: Aes<T>): MappedData<T> {
            return myDataAccess.getMappedData(aes, hitIndex())
        }
    }
}
