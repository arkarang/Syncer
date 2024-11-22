package com.minepalm.syncer.player.bukkit.serialize;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdException;

import java.nio.charset.StandardCharsets;

public class ZstdCompressor {

    // Compresses the input JSON string and returns the compressed byte array
    public static byte[] compress(String jsonString) {
        try {
            byte[] data = jsonString.getBytes(StandardCharsets.UTF_8);
            return Zstd.compress(data);
        } catch (ZstdException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decompress(byte[] compressedData) {
        try {
            long decompressedSize = Zstd.getFrameContentSize(compressedData);
            if (decompressedSize == 0) {
                throw new RuntimeException("Original size of the compressed buffer is unknown.");
            }

            byte[] decompressedData = new byte[(int) decompressedSize];
            long actualDecompressedSize = Zstd.decompress(decompressedData, compressedData);

            if (actualDecompressedSize != decompressedSize) {
                throw new RuntimeException("Decompressed data size does not match expected size.");
            }

            return new String(decompressedData, StandardCharsets.UTF_8);
        } catch (ZstdException e) {
            e.printStackTrace();
            return null;
        }
    }
}
