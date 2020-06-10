/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plotDemo.model.plotConfig

import jetbrains.datalore.plot.parsePlotSpec
import jetbrains.datalore.plotDemo.model.AutoMpg
import jetbrains.datalore.plotDemo.model.Iris
import jetbrains.datalore.plotDemo.model.PlotConfigDemoBase

class TooltipConfig: PlotConfigDemoBase()  {

    fun plotSpecList(): List<Map<String, Any>> {
        return listOf(
            mpg(),
            basic(),
            tooltipAesList(),
            tooltipEmptyList()
        )
    }

    private fun mpg(): Map<String, Any> {
        val aesX =  "\$aes@x"
        val aesY =  "\$aes@y"
        val aesColor =  "\$aes@color"
        val vehicleName = "\${vehicle name}"
        val modelYear =  "\${model year}"
        val originCar = "\${origin of car}"
        val spec = """
        {
           'kind': 'plot',
           'ggtitle': {'text' : 'Tooltip configuration'},
           'mapping': {
                         'x': 'engine displacement (cu. inches)',
                         'y':  'engine horsepower',
                         'color': 'miles per gallon',
                         'shape': 'origin of car'
                      },
           'layers': [
                        {
                           'geom': 'point',
                           'tooltip_lines': [  
                                'x/y|$aesX x $aesY', 
                                '$aesColor (miles per gallon)',
                                'car \'$vehicleName\' (origin of car)',
                                '19$modelYear',
                                '$originCar',
                                '#mpg data set'
                           ],
                           'tooltip_formats': {
                                'aes@x': '.1f', 
                                'aes@y': '.2f', 
                                'aes@color': '.2f'
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
           'ggtitle': {'text' : 'No tooltip list (default)'},
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
        val aesX =  "\$aes@x"
        val aesY =  "\$aes@y"
        val aesColor =  "\$aes@color"
        val aesFill =  "\$aes@fill"
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
                           'tooltip_lines': [  
                                '@|$aesFill',   
                                'length (x)|$aesX',
                                'density (y)|$aesY',
                                '$aesColor' 
                            ],
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
                               'tooltip_lines': []
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