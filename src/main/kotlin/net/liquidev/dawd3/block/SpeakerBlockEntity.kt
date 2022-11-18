package net.liquidev.dawd3.block

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos

class SpeakerBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(Blocks.speakerEntity, pos, state) {
    init {
        println("Speaker block entity created")

    }

    fun deinit() {
        println("Speaker block entity deinitialized")
    }

    private fun finalize() {
        deinit()
    }
}