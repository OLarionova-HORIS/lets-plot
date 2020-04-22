/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.config

import jetbrains.datalore.base.gcommon.base.Preconditions.checkArgument
import jetbrains.datalore.base.gcommon.base.Preconditions.checkState
import jetbrains.datalore.plot.base.Aes
import jetbrains.datalore.plot.base.DataFrame
import jetbrains.datalore.plot.base.Scale
import jetbrains.datalore.plot.base.Stat
import jetbrains.datalore.plot.base.data.DataFrameUtil
import jetbrains.datalore.plot.base.data.DataFrameUtil.variables
import jetbrains.datalore.plot.builder.VarBinding
import jetbrains.datalore.plot.builder.assemble.PosProvider
import jetbrains.datalore.plot.builder.assemble.TypedScaleProviderMap
import jetbrains.datalore.plot.builder.assemble.geom.DefaultAesAutoMapper
import jetbrains.datalore.plot.builder.sampling.Sampling
import jetbrains.datalore.plot.config.DataMetaUtil.createDataFrame
import jetbrains.datalore.plot.config.Option.Layer.GEOM
import jetbrains.datalore.plot.config.Option.Layer.SHOW_LEGEND
import jetbrains.datalore.plot.config.Option.Layer.STAT
import jetbrains.datalore.plot.config.Option.Layer.TOOLTIPS
import jetbrains.datalore.plot.config.Option.LayerTooltips.LINES
import jetbrains.datalore.plot.config.Option.PlotBase.DATA
import jetbrains.datalore.plot.config.Option.PlotBase.MAPPING

class LayerConfig(
    layerOptions: Map<*, *>,
    sharedData: DataFrame,
    plotMappings: Map<*, *>,
    plotDiscreteAes: Set<*>,
    val geomProto: GeomProto,
    statProto: StatProto,
    scaleProviderByAes: TypedScaleProviderMap,
    private val myClientSide: Boolean
) : OptionsAccessor(layerOptions, initDefaultOptions(layerOptions, geomProto, statProto)) {

    //    val geomProvider: GeomProvider
    val stat: Stat
    val explicitGroupingVarName: String?
    val posProvider: PosProvider
    private val myCombinedData: DataFrame
    val varBindings: List<VarBinding>
    val constantsMap: Map<Aes<*>, Any>
    val statKind: StatKind
    private val mySamplings: List<Sampling>?
    private val tooltips: List<TooltipLine>?

    var ownData: DataFrame? = null
        private set
    private var myOwnDataUpdated = false

    val combinedData: DataFrame
        get() {
            checkState(!myOwnDataUpdated)
            return myCombinedData
        }

    val isLegendDisabled: Boolean
        get() = if (hasOwn(SHOW_LEGEND)) {
            !getBoolean(SHOW_LEGEND, true)
        } else false

    val samplings: List<Sampling>?
        get() {
            checkState(!myClientSide)
            return mySamplings
        }

    val tooltipAes: List<Aes<*>>?
        get() = tooltips
            ?.filterNot { it.value.isNullOrEmpty() }
            ?.map { getAesByName(it.value!!) }

    val tooltipLabels: Map<Aes<*>, String>
        get() = tooltips
            ?.filterNot { it.value.isNullOrEmpty() }
            ?.associateBy({ getAesByName(it.value!!) }, { it.label })
            ?: emptyMap()

    init {
        val (layerMappings, layerData) = createDataFrame(
            options = this,
            commonData = sharedData,
            commonDiscreteAes = plotDiscreteAes,
            commonMappings = plotMappings,
            isClientSide = myClientSide
        )

        if (!myClientSide) {
            update(MAPPING, layerMappings)
        }

        // mapping (inherit from plot) + 'layer' mapping
        val combinedMappings = plotMappings + layerMappings

        var combinedData: DataFrame
        if (!(sharedData.isEmpty || layerData.isEmpty) && sharedData.rowCount() == layerData.rowCount()) {
            combinedData = DataFrameUtil.appendReplace(sharedData, layerData)
        } else if (!layerData.isEmpty) {
            combinedData = layerData
        } else {
            combinedData = sharedData
        }


        var aesMappings: Map<Aes<*>, DataFrame.Variable>?
        if (GeoPositionsDataUtil.hasGeoPositionsData(this) && myClientSide) {
            // join dataset and geo-positions data
            val dataAndMapping = GeoPositionsDataUtil.initDataAndMappingForGeoPositions(
                geomProto.geomKind,
                combinedData,
                GeoPositionsDataUtil.getGeoPositionsData(this),
                combinedMappings
            )
            combinedData = dataAndMapping.first
            aesMappings = dataAndMapping.second
        } else {
            aesMappings = ConfigUtil.createAesMapping(combinedData, combinedMappings)
        }

        // auto-map variables if necessary
        if (aesMappings.isEmpty()) {
            aesMappings = DefaultAesAutoMapper.forGeom(geomProto.geomKind).createMapping(combinedData)
            if (!myClientSide) {
                // store used mapping options to pass to client.
                val autoMappingOptions = HashMap<String, Any>()
                for (aes in aesMappings.keys) {
                    val option = Option.Mapping.toOption(aes)
                    val variable = aesMappings[aes]!!.name
                    autoMappingOptions[option] = variable
                }
                update(MAPPING, autoMappingOptions)
            }
        }

        // exclude constant aes from mapping
        val constants = LayerConfigUtil.initConstants(this)
        if (constants.isNotEmpty()) {
            aesMappings = HashMap(aesMappings)
            for (aes in constants.keys) {
                aesMappings.remove(aes)
            }
        }

        // grouping
        explicitGroupingVarName = initGroupingVarName(combinedData, combinedMappings)

        statKind = StatKind.safeValueOf(getString(STAT)!!)
        stat = statProto.createStat(statKind, mergedOptions)
        posProvider = LayerConfigUtil.initPositionAdjustments(
            this,
            geomProto.preferredPositionAdjustments(this)
        )
        constantsMap = constants

        val consumedAesSet = HashSet(geomProto.renders())
        if (!myClientSide) {
            consumedAesSet.addAll(stat.consumes())
        }

        // tooltip aes list
        this.tooltips = getTooltips()

        val varBindings = LayerConfigUtil.createBindings(
            combinedData,
            aesMappings,
            scaleProviderByAes,
            consumedAesSet
        )

        this.varBindings = varBindings
        ownData = layerData
        myCombinedData = combinedData

        mySamplings = if (myClientSide)
            null
        else
            LayerConfigUtil.initSampling(this, geomProto.preferredSampling())
    }

    private fun initGroupingVarName(data: DataFrame, mappingOptions: Map<*, *>): String? {
        val groupBy = mappingOptions[Option.Mapping.GROUP]
        var fieldName: String? = if (groupBy is String)
            groupBy
        else
            null

        if (fieldName == null && GeoPositionsDataUtil.hasGeoPositionsData(this)) {
            // 'default' group is important for 'geom_map'
            val groupVar = variables(data)["group"]
            if (groupVar != null) {
                fieldName = groupVar.name
            }
        }
        return fieldName
    }

    fun hasVarBinding(varName: String): Boolean {
        for (binding in varBindings) {
            if (binding.variable.name == varName) {
                return true
            }
        }
        return false
    }

    fun replaceOwnData(dataFrame: DataFrame?) {
        checkState(!myClientSide)   // This class is immutable on client-side
        checkArgument(dataFrame != null)
        update(DATA, DataFrameUtil.toMap(dataFrame!!))
        ownData = dataFrame
        myOwnDataUpdated = true
    }

    fun hasExplicitGrouping(): Boolean {
        return explicitGroupingVarName != null
    }

    fun isExplicitGrouping(varName: String): Boolean {
        return explicitGroupingVarName != null && explicitGroupingVarName == varName
    }

    fun getVariableForAes(aes: Aes<*>): DataFrame.Variable? {
        return varBindings.find { it.aes == aes }?.variable
    }

    fun getScaleForAes(aes: Aes<*>): Scale<*>? {
        return varBindings.find { it.aes == aes }?.scale
    }

    private fun getAesByName(aesName: String): Aes<*> {
        // find aes and check if it is aes
        return Aes.values().find { it.name == aesName } ?: error("$aesName is not aes name ")
    }

    class TooltipLine(val value: String?, val label: String, val format: String? = null)

    private fun parseTooltipLine(tooltipLine: Map<*, *>): TooltipLine {
        val src = tooltipLine.getString(Option.TooltipLine.VALUE)
        val label = tooltipLine.getString(Option.TooltipLine.LABEL) ?: ""
        val format = tooltipLine.getString(Option.TooltipLine.FORMAT)
        return TooltipLine(value = src, label = label, format = format)
    }

    private fun parseLines(tooltipLines: List<*>): MutableList<TooltipLine> {
        val result = mutableListOf<TooltipLine>()
        tooltipLines.forEach { tooltipLine ->
            if (tooltipLine is String) {
                result.add(TooltipLine(value = tooltipLine, label = "", format = null))
            } else if (tooltipLine is Map<*, *>) {
                result.add( parseTooltipLine(tooltipLine))
            }
        }
        return result
    }

    private fun getTooltips(): List<TooltipLine>? {
        // tooltip list is not defined - will be used default tooltips
        if (!has(TOOLTIPS)) {
            return null
        }

        val layerTooltips = getMap(TOOLTIPS)
        if (layerTooltips.isEmpty() || !layerTooltips.containsKey(LINES)) {
            return null
        }

        var result = mutableListOf<TooltipLine>()
        val lines = layerTooltips.get(LINES)
        if (lines is List<*>) {
            result = parseLines(lines)
        }
        return result
    }

    private companion object {
        private fun initDefaultOptions(layerOptions: Map<*, *>, geomProto: GeomProto, statProto: StatProto): Map<*, *> {
            checkArgument(
                layerOptions.containsKey(GEOM) || layerOptions.containsKey(STAT),
                "Either 'geom' or 'stat' must be specified"
            )

            val defaults = HashMap<String, Any>()
            defaults.putAll(geomProto.defaultOptions())

            var statName: String? = layerOptions[STAT] as String?
            if (statName == null) {
                statName = defaults[STAT] as String
            }
            defaults.putAll(statProto.defaultOptions(statName))

            return defaults
        }
    }
}
