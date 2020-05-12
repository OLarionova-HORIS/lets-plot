/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.interact

import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.base.values.Color
import jetbrains.datalore.base.values.Pair
import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.interact.*
import jetbrains.datalore.plot.base.interact.MappedDataAccess.MappedData

class TooltipSpecFactory(
    private val contextualMapping: ContextualMapping,
    private val axisOrigin: DoubleVector
) {
    fun create(geomTarget: GeomTarget): List<TooltipSpec> {
        return ArrayList(Helper(geomTarget).tooltipSpecs)
    }

    private inner class Helper(private val myGeomTarget: GeomTarget) {
        internal val tooltipSpecs = ArrayList<TooltipSpec>()
        private val myDataAccess: MappedDataAccess = contextualMapping.dataAccessor.mappedDataAccess
        private val generatedTooltipLines = contextualMapping.generateLines(
            myGeomTarget.hitIndex,
            myGeomTarget.aesTipLayoutHints.map { it.key }
        )

        init {
            initTooltipSpecs()
        }

        private fun initTooltipSpecs() {
            addHintTooltipSpec()

            addAesTooltipSpec()

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

        private fun outlierLines(): List<TooltipContent.TooltipLine> {
            return generatedTooltipLines.filter { it.isOutlier() && !it.isAxis() }
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
            val axisLines = generatedTooltipLines.filter { it.isAxis() }
            val axis = mapOf(
                Aes.X to axisLines.filter { Aes.X == it.aes }.map { it.line },
                Aes.Y to axisLines.filter { Aes.Y == it.aes }.map { it.line }
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

        private fun addAesTooltipSpec() {

            val aesList = generatedTooltipLines.mapNotNull { it.aes }
            val hint = outlierLines().mapNotNull { it.aes }
            val aesListForTooltip = removeDiscreteDuplicatedMappings(aesList - hint)

            val lines = mutableListOf<String>()

            generatedTooltipLines
                .filter { !it.isOutlier() }
                .forEach { tooltipLine ->
                    if (!tooltipLine.isAes() || aesListForTooltip.contains(tooltipLine.aes)) {
                        lines.add(tooltipLine.line)
                    }
                }
            applyTipLayoutHint(text = lines, layoutHint = tipLayoutHint(), isOutlier = false)
        }

        // todo use ValueSourceInfo
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
            if (aes === Aes.X) {
                return TipLayoutHint.xAxisTooltip(
                    DoubleVector(tipLayoutHint().coord!!.x, axisOrigin.y),
                    AXIS_TOOLTIP_COLOR
                )
            }

            if (aes === Aes.Y) {
                return TipLayoutHint.yAxisTooltip(
                    DoubleVector(axisOrigin.x, tipLayoutHint().coord!!.y),
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

        //todo remove it
        private fun isMapped(aes: Aes<*>): Boolean {
            return myDataAccess.isMapped(aes)
        }
        //todo remove it
        private fun <T> getMappedData(aes: Aes<T>): MappedData<T> {
            return myDataAccess.getMappedData(aes, hitIndex())
        }
    }

    companion object {
        val AXIS_TOOLTIP_COLOR = Color.GRAY
    }
}
