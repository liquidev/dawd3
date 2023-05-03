package net.liquidev.dawd3.audio.device

import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtFloat
import net.minecraft.util.Identifier
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger

class ControlName(parent: Identifier, name: String) {
    val id = idInDevice(parent, name)

    init {
        registry[id] = this
    }

    override fun toString() = id.toString()

    fun toTranslationKey(): String {
        val namespace = id.namespace
        val key = id.path.replace('/', '.')
        return "dawd3.control.${namespace}.${key}"
    }

    companion object {
        private val registry = hashMapOf<Identifier, ControlName>()

        fun fromString(name: String): ControlName? =
            registry[Identifier(name)]
    }
}

data class ControlDescriptor<T>(
    val name: ControlName,
    val initialValue: T,
) {
    constructor(
        parent: Identifier,
        name: String,
        initialValue: T,
    ) : this(ControlName(parent, name), initialValue)
}

sealed interface Control {
    fun valueToNBT(): NbtElement
    fun valueFromNBT(element: NbtElement)

    fun valueToBytes(): ByteArray
    fun valueFromBytes(buffer: ByteArray)
}

class FloatControl(val descriptor: ControlDescriptor<Float>) : Control {
    private val internalValue = AtomicInteger(descriptor.initialValue.toBits())
    var value: Float
        get() = Float.fromBits(internalValue.get())
        set(value) = internalValue.set(value.toBits())

    override fun valueToNBT(): NbtElement = NbtFloat.of(value)

    override fun valueFromNBT(element: NbtElement) {
        value = (element as? NbtFloat)?.floatValue() ?: 0f
    }

    override fun valueToBytes(): ByteArray =
        ByteBuffer.allocate(4).putFloat(value).array()

    override fun valueFromBytes(buffer: ByteArray) {
        value = ByteBuffer.wrap(buffer).getFloat()
    }
}

interface ControlSet {
    fun visitControls(visit: (ControlName, Control) -> Unit)
}

object NoControls : ControlSet {
    override fun visitControls(visit: (ControlName, Control) -> Unit) {}
}

class ControlMap(set: ControlSet) {
    private val map = hashMapOf<ControlName, Control>()

    init {
        set.visitControls { controlName, control -> map[controlName] = control }
    }

    operator fun get(name: ControlName): Control? = map[name]
}
