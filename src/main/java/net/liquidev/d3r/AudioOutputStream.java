package net.liquidev.d3r;

public interface AudioOutputStream {
    float[] getOutputBuffer(int sampleCount, int channelCount);

    void error(String message);
}
