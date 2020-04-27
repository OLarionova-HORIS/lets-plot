/*
 * Copyright (c) 2020. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.datalore.plot.builder.tooltip

class TooltipConfigLine(val name: String?,                // var name
                        val label: String,                // label
                        val format: String? = null) {     //  formatting


    companion object {

        const val AES_PREFIX = "aes@"

        fun hasAesPrefix(str: String): Boolean {
            return str.startsWith(AES_PREFIX)
        }

        fun detachAesName(str: String): String {
            return str.removePrefix(AES_PREFIX)
        }
    }
}