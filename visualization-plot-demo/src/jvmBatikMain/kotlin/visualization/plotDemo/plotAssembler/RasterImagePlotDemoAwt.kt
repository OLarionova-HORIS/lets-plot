package jetbrains.datalore.visualization.plotDemo.plotAssembler

import jetbrains.datalore.visualization.base.swing.SvgMapperDemoFrame
import jetbrains.datalore.visualization.plotDemo.model.plotAssembler.RasterImagePlotDemo

class RasterImagePlotDemoAwt : RasterImagePlotDemo() {

    private fun show() {
        val plots = createPlots()
        val svgRoots = createSvgRootsFromPlots(plots)
        SvgMapperDemoFrame.showSvg(svgRoots, demoComponentSize, "Raster image plot")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            RasterImagePlotDemoAwt().show()
        }
    }
}