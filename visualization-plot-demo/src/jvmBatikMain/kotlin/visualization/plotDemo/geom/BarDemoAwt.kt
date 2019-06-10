package jetbrains.datalore.visualization.plotDemo.geom

import jetbrains.datalore.visualization.base.swing.SvgMapperDemoFrame
import jetbrains.datalore.visualization.plotDemo.model.geom.BarDemo

class BarDemoAwt : BarDemo() {

    private fun show() {
        val demoModels = createModels()
        val svgRoots = createSvgRoots(demoModels)
        SvgMapperDemoFrame.showSvg(svgRoots, demoComponentSize, "Bar geom")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            BarDemoAwt().show()
        }
    }
}