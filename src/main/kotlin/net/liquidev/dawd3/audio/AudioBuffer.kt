package net.liquidev.dawd3.audio

import net.liquidev.dawd3.Mod

/** Audio buffer that reallocates itself when needed. */
class AudioBuffer {
    private companion object {
        val logger = Mod.logger<AudioBuffer>()
    }

    var array: FloatArray? = null
        private set

    fun getOrReallocate(minSampleCount: Int): FloatArray {
        val inArray = array
        if (inArray == null || inArray.size < minSampleCount) {
            logger.info("reallocating output buffer for $this; this might cause stuttering")
            // Allocate a little more so that reallocations don't trigger too often.
            array = FloatArray(minSampleCount)
        }
        return array!!
    }
}