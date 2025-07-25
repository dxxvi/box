package home;

import static java.nio.charset.StandardCharsets.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.jsoup.nodes.Document.OutputSettings.Syntax.html;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;

public abstract class Utils {
  public static final String ALGORITHM = "AES";

  private Utils() {}

  /**
   * Replace all `&lt;pre[ &gt;].+&lt;/pre&gt;` with `&lt;i id=""&gt;&lt;/i&gt;`
   */
  public static Tuple2<String, Map<Integer /*id*/, String /*html*/>> extractPres(String html) {
    final String PRE1 = "<pre ";
    final String PRE2 = "<pre>";
    final String PRE_END = "</pre>";
    final Map<Integer, String> map = new LinkedHashMap<>();

    AtomicInteger ai = new AtomicInteger();

    for (String pre : new String[] {PRE1, PRE2}) {
      while (true) {
        int i = html.indexOf(pre);
        if (i >= 0) {
          int j = html.indexOf(PRE_END, i + pre.length());
          if (j < 0) {
            throw new IllegalArgumentException("%s at %d has no %s".formatted(pre, i, PRE_END));
          }
          int id = ai.incrementAndGet();
          String htmlPiece = html.substring(i, j + PRE_END.length());
          String placeHolder =
              """
              <i id="%d"></i>"""
                  .formatted(id);
          map.put(id, htmlPiece);
          html = html.substring(0, i) + placeHolder + html.substring(j + PRE_END.length());
          continue;
        }
        break;
      }
    }
    return new Tuple2<>(html, map);
  }

  /**
   * Add fonts to the head element of the document
   */
  public static void addFonts(Document document) {
    document
        .head()
        .append(
            """
                <link rel="preconnect" href="https://fonts.googleapis.com">
                <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                <link href="https://fonts.googleapis.com/css2?family=Fira+Code:wght@300..700&family=Noto+Sans+JP:wght@100..900&family=Noto+Serif+JP:wght@200..900&display=swap"
                      rel="stylesheet">""")
        .append(
            """
        <style>
          // font-weight: Use a value from 200 to 900
          .noto-serif {
            font-family: "Noto Serif JP", serif;
            font-optical-sizing: auto;
            font-style: normal;
            font-variation-settings: "wdth" 100;
          }
          // font-weight: Use a value from 100 to 900
          .noto-sans {
            font-family: "Noto Sans JP", sans-serif;
            font-optical-sizing: auto;
            font-style: normal;
            font-variation-settings: "wdth" 100;
          }
          // font-weight: Use a value from 300 to 700
          .fira-code {
            font-family: "Fira Code", monospace;
            font-optical-sizing: auto;
            font-style: normal;
          }
          pre, code { font-family: "Fira Code", monospace; font-size: .91rem }
        </style>""");
  }

  public static void addStuff(Document document, String title, String css) {
    addStuff(
        document,
        title,
        css,
        aesDecryptToUrl("0pQ7zRJnRa3glafQ/RMiW8Aa7AOWi13FhrHL3Fhiid0=").toString());
  }

  /**
   * Add font-size for html so that we can have the number of pixels for 1rem
   * Add !DOCTYPE html, css, make pretty print.
   * Prepend the baseUrl to image src if the image src starts with `/` or doesn't start with http
   * Remove the width, height attributes of img
   */
  public static void addStuff(Document document, String title, String css, String baseUrl) {
    document.head().parent().attr("style", "color: #333; font: 16px/1.44 'Noto Serif JP',serif");
    document.charset(UTF_8);
    document
        .head()
        .append(
            """
            <meta name="viewport" content="width=device-width, initial-scale=1">""")
        .append("<title>%s</title>".formatted(title))
        .append(
            """
            <style>
              img { max-width: 99%% }
              a { text-decoration: none }
            </style>
            <style>%s</style>"""
                .formatted(css));
    addFonts(document);
    var outputSettings =
        new OutputSettings().charset(UTF_8).indentAmount(2).prettyPrint(true).syntax(html);
    document.outputSettings(outputSettings);

    document
        .select("img[src]")
        .forEach(
            img -> {
              img.removeAttr("width").removeAttr("height");
              String src = img.attr("src");
              if (src.startsWith("/") || !src.startsWith("http")) {
                img.attr("src", baseUrl + src);
              }
            });
  }

  /**
   * Insert pre's back and write to file
   */
  public static void insertPreBackThenWriteToFile(
      Document document, Map<Integer, String> map, Path path) throws Throwable {
    String prettyHtml = document.toString();
    for (Map.Entry<Integer, String> entry : map.entrySet()) {
      String placeHolder =
          """
          <i id="%d"></i>"""
              .formatted(entry.getKey());
      prettyHtml = prettyHtml.replace(placeHolder, entry.getValue());
    }
    Files.writeString(path, "<!DOCTYPE html>" + prettyHtml, CREATE, TRUNCATE_EXISTING);
  }

  public static byte[] aesDecrypt(byte[] bytes) throws Throwable {
    final Cipher aesDecryptor = Cipher.getInstance(ALGORITHM);
    aesDecryptor.init(Cipher.DECRYPT_MODE, getAESSecretKey());
    return aesDecryptor.doFinal(bytes);
  }

  public static byte[] aesEncrypt(byte[] bytes) throws Throwable {
    final Cipher aesEncryptor = Cipher.getInstance(ALGORITHM);
    aesEncryptor.init(Cipher.ENCRYPT_MODE, getAESSecretKey());
    return aesEncryptor.doFinal(bytes);
  }

  public static String aesEncrypt(URL url) throws Throwable {
    final Cipher aesEncryptor = Cipher.getInstance(ALGORITHM);
    aesEncryptor.init(Cipher.ENCRYPT_MODE, getAESSecretKey());
    byte[] bytes = aesEncryptor.doFinal(url.toString().getBytes(US_ASCII));
    return Base64.getEncoder().encodeToString(bytes);
  }

  public static URL aesDecryptToUrl(String s) {
    try {
      final Cipher aesDecryptor = Cipher.getInstance(ALGORITHM);
      aesDecryptor.init(Cipher.DECRYPT_MODE, getAESSecretKey());
      byte[] bytes = Base64.getDecoder().decode(s.getBytes(US_ASCII));
      bytes = aesDecryptor.doFinal(bytes);
      return new URI(new String(bytes, US_ASCII)).toURL();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * UrlEncoder is used because this method is used to encrypt a file name to a string which is used
   * as a file name.
   */
  public static String encodeThenEncryptThenEncode(String filename) {
    try {
      byte[] bytes1 = Base64.getEncoder().encode(filename.getBytes(UTF_8));
      byte[] bytes2 = aesEncrypt(bytes1);
      return Base64.getUrlEncoder().encodeToString(bytes2).replace("=", "");
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  public static String decodeThenDecryptThenDecode(String filename) {
    for (int numberOfPaddingCharacters : new int[] {0, 1, 2}) {
      try {
        filename += "=".repeat(numberOfPaddingCharacters);
        byte[] bytes1 = Base64.getUrlDecoder().decode(filename.getBytes(US_ASCII));
        byte[] bytes2 = aesDecrypt(bytes1);
        return new String(Base64.getDecoder().decode(bytes2), US_ASCII);
      } catch (Throwable t) {
        // ignored because we might not get the right number of padding characters
      }
    }
    throw new RuntimeException("Unable to decode this file name " + filename);
  }

  public static SecretKey getAESSecretKey() {
    final String secretKeyString = "...";

    return new SecretKeySpec(Base64.getDecoder().decode(secretKeyString), ALGORITHM);
  }
}
