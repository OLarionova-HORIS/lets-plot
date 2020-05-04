/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.assemble

import jetbrains.datalore.base.gcommon.base.Preconditions.checkArgument
import jetbrains.datalore.base.typedKey.TypedKeyHashMap
import jetbrains.datalore.plot.base.*
import jetbrains.datalore.plot.base.aes.AestheticsDefaults
import jetbrains.datalore.plot.base.data.DataFrameUtil
import jetbrains.datalore.plot.base.data.TransformVar
import jetbrains.datalore.plot.base.geom.LiveMapGeom
import jetbrains.datalore.plot.base.geom.LiveMapProvider
import jetbrains.datalore.plot.base.interact.AbstractDataValue
import jetbrains.datalore.plot.base.interact.DataAccess
import jetbrains.datalore.plot.base.interact.GeomTargetLocator.LookupSpec
import jetbrains.datalore.plot.base.interact.TooltipContent
import jetbrains.datalore.plot.base.render.LegendKeyElementFactory
import jetbrains.datalore.plot.base.stat.SimpleStatContext
import jetbrains.datalore.plot.base.stat.Stats
import jetbrains.datalore.plot.builder.GeomLayer
import jetbrains.datalore.plot.builder.PosProviderContext
import jetbrains.datalore.plot.builder.VarBinding
import jetbrains.datalore.plot.builder.assemble.geom.GeomProvider
import jetbrains.datalore.plot.builder.assemble.geom.PointDataAccess
import jetbrains.datalore.plot.builder.data.DataProcessing
import jetbrains.datalore.plot.builder.data.GroupingContext
import jetbrains.datalore.plot.builder.interact.ContextualMappingProvider
import jetbrains.datalore.plot.builder.scale.ScaleProvider
import jetbrains.datalore.plot.builder.tooltip.AesData
import jetbrains.datalore.plot.builder.tooltip.DataPointFormatterProvider
import jetbrains.datalore.plot.builder.tooltip.TooltipContentGenerator

class GeomLayerBuilder {
    private val myBindings = ArrayList<VarBinding>()
    private val myConstantByAes = TypedKeyHashMap()
    private lateinit var myStat: Stat
    private lateinit var myPosProvider: PosProvider
    private lateinit var myGeomProvider: GeomProvider
    private var myGroupingVarName: String? = null
    private val myScaleProviderByAes = HashMap<Aes<*>, ScaleProvider<*>>()

    private var myDataPreprocessor: ((DataFrame) -> DataFrame)? = null
    private var myLocatorLookupSpec: LookupSpec = LookupSpec.NONE
    private var myContextualMappingProvider: ContextualMappingProvider = ContextualMappingProvider.NONE
    private var myDataPointFormatterProvider: DataPointFormatterProvider = DataPointFormatterProvider.NONE

    private var myIsLegendDisabled: Boolean = false

    fun stat(v: Stat): GeomLayerBuilder {
        myStat = v
        return this
    }

    fun pos(v: PosProvider): GeomLayerBuilder {
        myPosProvider = v
        return this
    }

    fun geom(v: GeomProvider): GeomLayerBuilder {
        myGeomProvider = v
        return this
    }

    fun addBinding(v: VarBinding): GeomLayerBuilder {
        myBindings.add(v)
        return this
    }

    fun groupingVar(v: DataFrame.Variable): GeomLayerBuilder {
        myGroupingVarName = v.name
        return this
    }

    fun groupingVarName(v: String): GeomLayerBuilder {
        myGroupingVarName = v
        return this
    }

    fun <T> addConstantAes(aes: Aes<T>, v: T): GeomLayerBuilder {
        myConstantByAes.put(aes, v)
        return this
    }

    fun <T> addScaleProvider(aes: Aes<T>, scaleProvider: ScaleProvider<T>): GeomLayerBuilder {
        myScaleProviderByAes[aes] = scaleProvider
        return this
    }

    fun locatorLookupSpec(v: LookupSpec): GeomLayerBuilder {
        myLocatorLookupSpec = v
        return this
    }

    fun contextualMappingProvider(v: ContextualMappingProvider): GeomLayerBuilder {
        myContextualMappingProvider = v
        return this
    }

    fun disableLegend(v: Boolean): GeomLayerBuilder {
        myIsLegendDisabled = v
        return this
    }

    private fun dataPointFormatterProvider(): DataPointFormatterProvider {
        return createDataPointFormatterProvider().myDataPointFormatterProvider
    }

    fun createDataPointFormatterProvider(): GeomLayerBuilder {
        if (myDataPointFormatterProvider == DataPointFormatterProvider.NONE)
            myDataPointFormatterProvider = DataPointFormatterProvider(ArrayList())
        return this
    }

    fun addTooltipLine(dataValues: List<AbstractDataValue>, label: String, format: String): GeomLayerBuilder {
        dataPointFormatterProvider().addFormatter(dataValues, label, format)
        return this
    }

    fun addTooltipLine(dataValue: AbstractDataValue, label: String, format: String): GeomLayerBuilder {
        addTooltipLine(listOf(dataValue), label, format)
        return this
    }

    fun addTooltipLine(aesData: AesData): GeomLayerBuilder {
        addTooltipLine(listOf(aesData), "", "")
        return this
    }

    fun build(data: DataFrame): GeomLayer {
        @Suppress("NAME_SHADOWING")
        var data = data
        if (myDataPreprocessor != null) {
            data = myDataPreprocessor!!(data)
        }

        // make sure 'original' series are transformed
        data = DataProcessing.transformOriginals(data, myBindings)

        // create missing bindings for 'stat' variables
        // and other adjustments in bindings.
        val replacementBindings =
            GeomLayerBuilderUtil.rewireBindingsAfterStat(
                data,
                myStat,
                myBindings,
                TypedScaleProviderMap(myScaleProviderByAes)
            )

        // add 'transform' variable for each 'stat' variable
        val bindingsToPut = ArrayList<VarBinding>()
        for (binding in replacementBindings.values) {
            val `var` = binding.variable
            if (`var`.isStat) {
                val aes = binding.aes
                val scale = binding.scale
                data = DataFrameUtil.applyTransform(data, `var`, aes, scale!!)

                bindingsToPut.add(VarBinding(TransformVar.forAes(aes), aes, scale))
            }
        }

        // replace 'stat' vars with 'transform' vars in bindings
        for (binding in bindingsToPut) {
            replacementBindings[binding.aes] = binding
        }

        // (!) Positional aes scales have undefined `mapper` at this time because
        // dimensions of plot are not yet known.
        // Data Access shouldn't use aes mapper (!)
        val dataAccess = PointDataAccess(data,
            aesVariables = replacementBindings.map { it.key to it.value.variable }.toMap(),
            aesScales = replacementBindings.map { it.key to it.value.scale }.toMap()
        )

        val tooltipGenerator = TooltipContentGenerator(
            myContextualMappingProvider.createContextualMapping(dataAccess),
            myDataPointFormatterProvider.dataFormatters
        )

        return MyGeomLayer(
            data,
            myGeomProvider,
            myPosProvider,
//            handledAes(),
            myGeomProvider.renders(),
            GroupingContext(data, myBindings, myGroupingVarName, handlesGroups()).groupMapper,
            replacementBindings.values,
            myConstantByAes,
            dataAccess,
            myLocatorLookupSpec,
            tooltipGenerator,
            myIsLegendDisabled
        )
    }

    private fun handlesGroups(): Boolean {
//        return DataProcessing.groupsHandled(myGeomProvider, myPosProvider)
        return myGeomProvider.handlesGroups() || myPosProvider.handlesGroups()
    }

//    private fun handledAes(): List<Aes<*>> {
//        return GeomLayerBuilderUtil.handledAes(myGeomProvider, myStat)
//    }


    private class MyGeomLayer(
        override val dataFrame: DataFrame,
        geomProvider: GeomProvider,
        private val myPosProvider: PosProvider,
//        handledAes: List<Aes<*>>,
        renderedAes: List<Aes<*>>,
        override val group: (Int) -> Int,
        varBindings: Collection<VarBinding>,
        constantByAes: TypedKeyHashMap,
        override val dataAccess: DataAccess,
        override val locatorLookupSpec: LookupSpec,
        override val tooltipGenerator: TooltipContent,
        override val isLegendDisabled: Boolean
    ) : GeomLayer {

        override val geom: Geom = geomProvider.createGeom()
        override val geomKind: GeomKind = geomProvider.geomKind
        override val aestheticsDefaults: AestheticsDefaults

        //        private val myHandledAes: List<Aes<*>>
        private val myRenderedAes: List<Aes<*>>
        private val myConstantByAes: TypedKeyHashMap
        private val myVarBindingsByAes = HashMap<Aes<*>, VarBinding>()

        override val legendKeyElementFactory: LegendKeyElementFactory
            get() = geom.legendKeyElementFactory

        override val isLiveMap: Boolean
            get() = geom is LiveMapGeom

        init {
//            myHandledAes = ArrayList(handledAes)
            myRenderedAes = ArrayList(renderedAes)

            // constant value by aes (default + specified)
            aestheticsDefaults = geomProvider.aestheticsDefaults()
            myConstantByAes = TypedKeyHashMap()

            for (key in constantByAes.keys<Any>()) {
                myConstantByAes.put(key, constantByAes[key])
            }

            for (varBinding in varBindings) {
                myVarBindingsByAes[varBinding.aes] = varBinding
            }
        }

//        override fun handledAes(): List<Aes<*>> {
//            return myHandledAes
//        }

        override fun renderedAes(): List<Aes<*>> {
            return myRenderedAes
        }

        override fun createPos(ctx: PosProviderContext): PositionAdjustment {
            return myPosProvider.createPos(ctx)
        }

        override fun hasBinding(aes: Aes<*>): Boolean {
            return myVarBindingsByAes.containsKey(aes)
        }

        override fun <T> getBinding(aes: Aes<T>): VarBinding {
            return myVarBindingsByAes[aes]!!
        }

        override fun hasConstant(aes: Aes<*>): Boolean {
            return myConstantByAes.containsKey(aes)
        }

        override fun <T> getConstant(aes: Aes<T>): T {
            checkArgument(hasConstant(aes), "Constant value is not defined for aes $aes")
            return myConstantByAes[aes]
        }

        override fun <T> getDefault(aes: Aes<T>): T {
            return aestheticsDefaults.defaultValue(aes)
        }

        override fun rangeIncludesZero(aes: Aes<*>): Boolean {
            return aestheticsDefaults.rangeIncludesZero(aes)
        }

        override fun setLiveMapProvider(liveMapProvider: LiveMapProvider) {
            if (geom is LiveMapGeom) {
                geom.setLiveMapProvider(liveMapProvider)
            } else {
                throw IllegalStateException("Not Livemap: " + geom::class.simpleName)
            }
        }
    }

    companion object {

        fun demoAndTest(): GeomLayerBuilder {
            val builder = GeomLayerBuilder()
            builder.myDataPreprocessor = { data ->
                val transformedData = DataProcessing.transformOriginals(data, builder.myBindings)
                when (val stat = builder.myStat) {
                    Stats.IDENTITY -> transformedData
                    else -> {
                        val statCtx = SimpleStatContext(transformedData)
                        val groupingContext =
                            GroupingContext(
                                transformedData,
                                builder.myBindings,
                                builder.myGroupingVarName,
                                true
                            )
                        val dataAndGroupingContext = DataProcessing.buildStatData(
                            transformedData,
                            stat,
                            builder.myBindings,
                            groupingContext, null, null, statCtx
                        )

                        dataAndGroupingContext.data
                    }
                }
            }

            return builder
        }
    }
}
