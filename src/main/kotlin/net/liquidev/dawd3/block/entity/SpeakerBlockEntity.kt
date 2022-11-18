package net.liquidev.dawd3.block

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.Audio
import net.liquidev.dawd3.audio.MixGenerator
import net.liquidev.dawd3.audio.SineOscGenerator
import net.liquidev.dawd3.audio.unit.Amplitude
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos

class SpeakerBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(Blocks.speakerEntity, pos, state) {
    private companion object {
        val logger = Mod.logger<SpeakerBlockEntity>()
    }

    private val channel: MixGenerator.Channel<SineOscGenerator>?

    init {
        logger.info("created")
        val generator = SineOscGenerator(
            frequency = 440.0f,
            amplitude = Amplitude(1.0f),
        )
        channel = Audio.mixer.createChannel(generator)
    }

    override fun onSyncedBlockEvent(type: Int, data: Int): Boolean {
        return super.onSyncedBlockEvent(type, data)
    }

    fun deinit() {
        logger.info("stopping channel")
        channel.stop()
    }

    protected fun finalize() {
        logger.warn("speaker block entity had to be deinitialized from finalizer")
        deinit()
    }
}