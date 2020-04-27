/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plotDemo.model.plotConfig

import jetbrains.datalore.plot.parsePlotSpec
import jetbrains.datalore.plotDemo.model.AutoMpg
import jetbrains.datalore.plotDemo.model.Iris
import jetbrains.datalore.plotDemo.model.PlotConfigDemoBase

class TooltipAesList: PlotConfigDemoBase()  {

    fun plotSpecList(): List<Map<String, Any>> {
        return listOf(
            mpg(),
            basic(),
            tooltipAesList(),
            tooltipEmptyList()
        )
    }

    private fun mpg(): Map<String, Any> {

        val spec = """
        {
           'kind': 'plot',
           'ggtitle': {'text' : 'Tooltip list with variable'},
           'mapping': {
                         'x': 'engine displacement (cu. inches)',
                         'y':  'engine horsepower',
                         'color': 'miles per gallon',
                         'shape': 'origin of car'
                      },
           'layers': [
                        {
                           'geom': 'point',
                            'tooltips': { 
                                'lines': [
                                           { 'value':'aes@x', 'label' : 'engine (x)' },
                                           { 'value':'aes@y', 'label' : 'horsepower (y)' },
                                           { 'value':'aes@color', 'label' : 'miles per gallon' },
                                            'vehicle name',
                                           { 'label' : 'Static text' }
                                ]
                            }
                        }
                     ]
        }
        """.trimIndent()
        val plotSpec = HashMap(parsePlotSpec(spec))
        plotSpec["data"] = AutoMpg.df
        return plotSpec
    }

    private fun basic(): Map<String, Any> {
        val spec = """
        {
           'kind': 'plot',
           'ggtitle': {'text' : 'No tooltip list'},
           'mapping': {
                         'x': 'sepal length (cm)',
                         'color': 'sepal width (cm)',
                         'fill': 'target'
                      },
           'layers': [
                        {
                           'geom': 'area',
                           'stat': 'density'
                        }
                     ]
        }
        """.trimIndent()

        val plotSpec = HashMap(parsePlotSpec(spec))
        plotSpec["data"] = Iris.df
        return plotSpec
    }

    private fun tooltipAesList(): Map<String, Any> {
        val spec = """
        {
           'kind': 'plot',
           'ggtitle': {'text' : 'Tooltip aes list'},
           'mapping': {
                         'x': 'sepal length (cm)',
                         'color': 'sepal width (cm)',
                         'fill': 'target'
                      },
           'layers': [
                        {
                           'geom': 'area',
                           'tooltips': {
                                         'lines': [
                                                     'aes@fill', 
                                                     { 'value':'aes@x', 'label' : 'length (x)' },
                                                     { 'value':'aes@y', 'label' : 'density (y)' },
                                                     { 'value':'aes@color', 'label' : '' }
                                                  ]
                                       },
                           'stat': 'density'
                        }
                     ]
        }
        """.trimIndent()

        val plotSpec = HashMap(parsePlotSpec(spec))
        plotSpec["data"] = Iris.df
        return plotSpec
    }

    private fun tooltipEmptyList(): Map<String, Any> {
        val spec = """
        {
           'kind': 'plot',
           'ggtitle': {'text' : 'Tooltip list = []'},
           'mapping': {
                         'x': 'sepal length (cm)',
                         'color': 'sepal width (cm)',
                         'fill': 'target'
                      },
           'layers': [
                        {
                           'geom': { 
                               'name': 'area',
                               'tooltips': { 'lines': []}
                            },
                           'stat': 'density'
                        }
                     ]
        }
        """.trimIndent()

        val plotSpec = HashMap(parsePlotSpec(spec))
        plotSpec["data"] = Iris.df
        return plotSpec
    }
}