package net.liquidev.dawd3.common

fun <T> moveElement(from: MutableList<T>, fromIndex: Int, to: MutableList<T>, toIndex: Int) {
    if (from == to && fromIndex != toIndex) {
        val element = from.removeAt(fromIndex)
        to.add(
            if (fromIndex < toIndex) toIndex - 1
            else toIndex,
            element,
        )
    } else {
        val element = from.removeAt(fromIndex)
        to.add(toIndex, element)
    }
}