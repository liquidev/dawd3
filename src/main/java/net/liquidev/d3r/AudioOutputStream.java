package net.liquidev.d3r;

public interface AudioOutputStream {
    float[] getOutputBuffer(int sampleCount, int channels);

    void error(String message);
}
