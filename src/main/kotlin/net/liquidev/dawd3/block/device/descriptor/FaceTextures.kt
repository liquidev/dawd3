package net.liquidev.dawd3.block.device.descriptor

import net.minecraft.util.Identifier

interface FaceTextures {
    val front: Identifier
    val back: Identifier
    val left: Identifier
    val right: Identifier
    val top: Identifier
    val bottom: Identifier

    val particle: Identifier

    companion object {
        fun withFrontAndSide(lazyId: () -> Identifier): FaceTextures = object : FaceTextures {
            private val id get() = lazyId()
            private val side = Identifier(id.namespace, "block/${id.path}_side")

            override val front = Identifier(id.namespace, "block/${id.path}_front")
            override val back = side
            override val left = side
            override val right = side
            override val top = side
            override val bottom = side
            override val particle = side
        }

        fun withTopSideAndBottom(lazyId: () -> Identifier): FaceTextures = object : FaceTextures {
            private val id get() = lazyId()
            private val side = Identifier(id.namespace, "block/${id.path}_side")

            override val front = side
            override val back = side
            override val left = side
            override val right = side
            override val top = Identifier(id.namespace, "block/${id.path}_top")
            override val bottom = Identifier(id.namespace, "block/${id.path}_bottom")
            override val particle = side
        }
    }
}