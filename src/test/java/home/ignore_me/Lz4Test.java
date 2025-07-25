package home.ignore_me;

import static java.nio.file.StandardOpenOption.*;
import static org.junit.jupiter.api.Assertions.*;

import home.Utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Lz4Test {
  private static final Logger LOG = LoggerFactory.getLogger(Lz4Test.class);

  @Test
  void testLz4Usage() throws Throwable {
    byte[] data = Files.readAllBytes(Path.of("src/main/resources", "logback.xml"));

    // compress
    LZ4Compressor compressor = LZ4Factory.fastestJavaInstance().highCompressor();
    byte[] compressedData = compressor.compress(data);
    LOG.info("{} bytes of text -> {} bytes in LZ4", data.length, compressedData.length);

    SecretKey aesSecretKey = Utils.getAESSecretKey();

    // encrypt
    Cipher aesEncryptor = Cipher.getInstance(Utils.ALGORITHM);
    aesEncryptor.init(Cipher.ENCRYPT_MODE, aesSecretKey);
    byte[] encryptedData = aesEncryptor.doFinal(compressedData);
    assertNotEquals(compressedData.length, encryptedData.length);

    // decrypt
    Cipher aesDecryptor = Cipher.getInstance(Utils.ALGORITHM);
    aesDecryptor.init(Cipher.DECRYPT_MODE, aesSecretKey);
    byte[] decryptedData = aesDecryptor.doFinal(encryptedData);
    assertEquals(compressedData.length, decryptedData.length);
    for (int i = 0; i < compressedData.length; i++) {
      if (compressedData[i] != decryptedData[i]) {
        fail("decrypted data is something different");
      }
    }

    // decompress
    byte[] restoredData = new byte[compressedData.length * 9];
    LZ4SafeDecompressor decompressor = LZ4Factory.safeInstance().safeDecompressor();
    int restoredDataLength = decompressor.decompress(compressedData, restoredData);
    assertEquals(data.length, restoredDataLength);
    for (int i = 0; i < data.length; i++) {
      if (data[i] != restoredData[i]) {
        fail("the restored data is different");
      }
    }
  }

  // Run this when there's a new src/test/resources/newthing.txt
  @Test
  void compressThenEncryptAll() throws Throwable {
    final LZ4Compressor compressor = LZ4Factory.fastestJavaInstance().highCompressor(99);
    final Cipher aesEncryptor = Cipher.getInstance(Utils.ALGORITHM);
    aesEncryptor.init(Cipher.ENCRYPT_MODE, Utils.getAESSecretKey());

    try (Stream<Path> stream = Files.walk(Path.of("src/test/resources"))) {
      for (Path path : stream.filter(p -> p.toFile().getPath().endsWith(".txt")).toList()) {
        byte[] compressedBytes = compressor.compress(Files.readAllBytes(path));
        byte[] encryptedBytes = Utils.aesEncrypt(compressedBytes);
        Files.write(
            Path.of(path.toFile().getPath() + ".bin"), encryptedBytes, CREATE, TRUNCATE_EXISTING);
      }
    }
  }

  // Run this to decrypt then decompress src/test/resources/*.txt.bin
  @Test
  void decryptThenDecompressAll() throws Throwable {
    final LZ4SafeDecompressor decompressor = LZ4Factory.safeInstance().safeDecompressor();
    final Cipher aesDecryptor = Cipher.getInstance(Utils.ALGORITHM);
    aesDecryptor.init(Cipher.DECRYPT_MODE, Utils.getAESSecretKey());

    try (Stream<Path> stream =
        Files.walk(Path.of("src/test/resources"))
            /*.filter(p -> p.toFile().getName().contains("learning-rust"))*/) {
      for (Path path : stream.filter(p -> p.toFile().getPath().endsWith(".txt.bin")).toList()) {
        byte[] encryptedBytes = Files.readAllBytes(path);
        byte[] compressedBytes = aesDecryptor.doFinal(encryptedBytes);
        byte[] buffer = new byte[9_999_999];
        int restoredDataLength = decompressor.decompress(compressedBytes, buffer);
        byte[] restoredBytes = new byte[restoredDataLength];
        System.arraycopy(buffer, 0, restoredBytes, 0, restoredDataLength);
        Files.write(
            Path.of(path.toFile().getPath().replace(".txt.bin", ".txt")),
            restoredBytes,
            CREATE,
            TRUNCATE_EXISTING);
      }
    }
  }
}
