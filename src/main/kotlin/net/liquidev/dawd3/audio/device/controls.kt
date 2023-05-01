package net.liquidev.dawd3.audio.device

import net.minecraft.util.Identifier
import java.util.concurrent.atomic.AtomicInteger

class ControlName(parent: Identifier, name: String) {
    val id = idInDevice(parent, name)

    init {
        registry[id] = this
    }

    override fun toString() = id.toString()

    companion object {
        private val registry = hashMapOf<Identifier, ControlName>()

        fun fromString(name: String): ControlName? =
            registry[Identifier(name)]
    }
}

data class ControlDescriptor(
    val name: ControlName,
    val initialValue: Float,
) {
    constructor(
        parent: Identifier,
        name: String,
        initialValue: Float,
    ) : this(ControlName(parent, name), initialValue)
}

class Control(val descriptor: ControlDescriptor) {
    private val internalValue = AtomicInteger(descriptor.initialValue.toBits())
    var value: Float
        get() = Float.fromBits(internalValue.get())
        set(value) = internalValue.set(value.toBits())
}

interface ControlSet {
    fun visitControls(visit: (ControlDescriptor, Control) -> Unit)
}

object NoControls : ControlSet {
    override fun visitControls(visit: (ControlDescriptor, Control) -> Unit) {}
}

class ControlMap(set: ControlSet) {
    private val map = hashMapOf<ControlName, Control>()

    init {
        set.visitControls { controlDescriptor, control -> map[controlDescriptor.name] = control }
    }

    operator fun get(name: ControlName): Control? = map[name]
}
