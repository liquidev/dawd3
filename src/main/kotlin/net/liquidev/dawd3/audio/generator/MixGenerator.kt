package net.liquidev.dawd3.audio.generator

import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.audio.unit.Decibels
import net.liquidev.dawd3.common.TaskQueue
import java.lang.ref.WeakReference

/** Audio mixer. Mixes multiple channels into a single output stream. */
class MixGenerator : AudioGenerator() {
    private companion object {
        val logger = Mod.logger<MixGenerator>()
    }

    private val taskQueue = TaskQueue<Unit, Unit>()
    private val channels = arrayListOf<WeakReference<Channel<AudioGenerator>>>()

    fun <T : AudioGenerator> createChannel(generator: T): Channel<T> {
        val channel = Channel(taskQueue, generator)
        val weak = WeakReference(channel as Channel<AudioGenerator>)
        taskQueue.enqueue { channels.add(weak) }
        return channel
    }

    override fun generate(output: FloatArray, sampleCount: Int, channelCount: Int) {
        // Flush task queue as soon as possible to reduce latency.
        taskQueue.flush(Unit)
        reapStoppedChannels()

        for (i in 0 until sampleCount) {
            output[i] = 0.0f
        }

        // NOTE: These loops are separated for better efficiency.
        // The processor is better at performing the same task many times, and interleaving types of tasks
        // is slower than doing them in batches.
        for (weak in channels) {
            val channel = weak.get()!!
            channel.audioBuffer = channel.generator.getOutputBuffer(sampleCount, channelCount)
        }
        for (weak in channels) {
            val channel = weak.get()!!
            for (i in 0 until sampleCount) {
                output[i] += channel.audioBuffer[i] * channel.volume.value
            }
        }
    }

    private fun reapStoppedChannels() {
        var i = 0
        while (i < channels.size) {
            val channel = channels[i].get()
            val reap = channel == null || !channel.playing
            if (reap) {
                logger.info("reaping channel at index $i in queue $channels")
                channels[i] = channels[channels.size - 1]
                channels.removeAt(channels.size - 1)
            } else {
                ++i
            }
        }
    }

    fun appendDebugInfo(outDebugInfo: MutableList<String>) {
        outDebugInfo.add("[d³] MixGenerator device graph profile:")
        channels.mapNotNull { it.get() }.forEachIndexed { index, channel ->
            val generator = channel.generator
            if (generator is DeviceGraphGenerator) {
                outDebugInfo.add("[d³]    - DeviceGraph #$index: ${generator.profileResult} ns")
            }
        }
    }

    class Channel<out T : AudioGenerator>(
        private val taskQueue: TaskQueue<Unit, Unit>,
        val generator: T,
    ) {
        private companion object {
            val logger = Mod.logger<Channel<AudioGenerator>>()
        }

        var volume = Decibels(-12.0f).toAmplitude()
        var playing = true
            private set
        lateinit var audioBuffer: FloatArray

        /** Shuts down the channel on the next audio generation request. */
        fun stop() {
            taskQueue.enqueue { playing = false }
        }

        fun finalize() {
            if (playing) {
                logger.warn("mixer channel stopped in finalizer; this might cause a ConcurrentModificationException")
            }
            stop()
        }

    }
}