package jetbrains.datalore.visualization.base.svgMapper.dom.domExtensions

import org.w3c.dom.Node
import org.w3c.dom.get

val Node.childCount: Int
    get() = childNodes.length

inline fun Node.getChild(index: Int): Node? = childNodes[index]

fun Node.insertFirst(child: Node) = insertBefore(child, firstChild)

fun Node.insertAfter(newChild: Node, refChild: Node?) {
    val next = refChild?.nextSibling
    if (next == null) {
        appendChild(newChild)
    } else {
        insertBefore(newChild, next)
    }
}