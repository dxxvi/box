package home.ignore_me;

import static org.junit.jupiter.api.Assertions.*;

import home.Tuple2;
import home.Utils;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UtilsTest {
  private static final Logger LOG = LoggerFactory.getLogger(UtilsTest.class);

  @Test
  void testExtractPres() {
    String html =
        """
        a<pre>b</pre>c
        <pre class="d">e</pre>""";
    Tuple2<String, Map<Integer, String>> tuple = Utils.extractPres(html);
    assertEquals(
        """
        a<i id="2"></i>c
        <i id="1"></i>""",
        tuple._1());
    assertTrue(tuple._2().containsKey(1), "no id of 1 in map");
    assertEquals(
        """
        <pre class="d">e</pre>""",
        tuple._2().get(1));
    assertTrue(tuple._2().containsKey(2), "no id of 2 in map");
    assertEquals("<pre>b</pre>", tuple._2().get(2));
  }

  @Test
  void testEncryptDecryptFilename() throws Throwable {
    for (String directory : new String[] {"src/test/resources", "src/test/java/home/books"}) {
      try (var stream = Files.walk(Path.of(directory))) {
        List<String> filenames =
            stream.map(Path::toFile).filter(File::isFile).map(File::getName).toList();
        for (String filename : filenames) {
          String newFilename = Utils.encodeThenEncryptThenEncode(filename);
          LOG.debug("{} -> {}", filename, newFilename);
          String s = Utils.decodeThenDecryptThenDecode(newFilename);
          assertEquals(filename, s);
        }
      }
    }
  }

  @Test
  void testUrlEncryptDecrypt() throws Throwable {
    String s = "https://learning.at.home.com";
    String encrypted = Utils.aesEncrypt(new URI(s).toURL());
    System.out.println(encrypted);
    URL url = Utils.aesDecryptToUrl(encrypted);
    assertEquals(s, url.toString());
  }
}
