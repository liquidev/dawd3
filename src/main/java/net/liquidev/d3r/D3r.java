package net.liquidev.d3r;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class D3r {
    private static final Logger LOGGER = LoggerFactory.getLogger("dawd3/d3r");
    private static Path tempDir = null;

    public static void load() throws D3rException, IOException {
        LOGGER.info("unpacking native library");

        var dynlibName = System.mapLibraryName("dawd3_d3r");
        var dynlibClassPath = "/d3r/" + dynlibName;
        LOGGER.debug("class path: " + dynlibClassPath);
        try (var resourceStream = D3r.class.getResourceAsStream(dynlibClassPath)) {
            if (resourceStream == null) {
                throw new D3rException("dawd3 cannot find an appropriate version of the d3r audio library for your system");
            }

            var bytes = resourceStream.readAllBytes();

            tempDir = Files.createTempDirectory("d3r_native_libs");
            var libFile = tempDir.resolve(dynlibName);

            if (Files.deleteIfExists(libFile)) {
                LOGGER.debug("deleted old unpacked library file");
            }
            try (var outputStream = Files.newOutputStream(libFile)) {
                outputStream.write(bytes);
                outputStream.flush();
            }

            LOGGER.info("loading library");
            System.load(libFile.toString());
        }

        LOGGER.info("initializing");
        initialize();
        LOGGER.info("loaded successfully");
    }

    public static void unload() {
        if (tempDir != null) {
            LOGGER.info("removing native library directory");
            try (var walker = Files.walk(tempDir)) {
                walker
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LOGGER.warn("unload() called twice or without calling load() first");
        }
    }

    private static native void initialize();

    // Host

    public static native void openDefaultHost();

    // Output device

    public static native int openDefaultOutputDevice();

    public static native void closeOutputDevice(int outputDeviceId);

    // Output stream

    public static native int openOutputStream(int outputDeviceId, int sampleRate, short channelCount, int bufferSize, AudioOutputStream generator);

    public static native void closeOutputStream(int outputStreamId);

    public static native void startPlayback(int outputStreamId);
}
