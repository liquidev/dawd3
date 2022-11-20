package net.liquidev.dawd3

import net.minecraft.util.Identifier

abstract class D3Registry<T> {
    abstract fun doRegister(identifier: Identifier, item: T)

    var registered = arrayListOf<Registered<T>>()

    fun add(id: Identifier, item: T): Registered<T> {
        val entry = Registered(id, item)
        registered.add(entry)
        return entry
    }

    fun registerAll() {
        registered.forEach { reg ->
            doRegister(reg.identifier, reg.item)
        }
    }

    data class Registered<T>(val identifier: Identifier, val item: T)
}