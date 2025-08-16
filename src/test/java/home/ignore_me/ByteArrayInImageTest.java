package home.ignore_me;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ByteArrayInImageTest {
  private static final Random RANDOM = new Random();

  @Test
  void test() throws Exception {
    String filename = "test.png";
    for (int i = 1; i <= 5; i++) {
      byte[] bytes = Files.readAllBytes(Path.of("ps.7z.00" + i));
      writeToPng((byte) i, bytes, "h" + i + ".png");
    }

    for (int i = 0; i < 100; i++) {
      var file = new File(filename);
      if (file.exists()) {
        file.delete();
      }

      byte[] bytes = new byte[RANDOM.nextInt(2, 16_666_777)];
      RANDOM.nextBytes(bytes);
      byte x = (byte) RANDOM.nextInt(2, 256);
      writeToPng(x, bytes, filename);

      var mapEntry = readFromPng(filename);
      assertEquals(x, mapEntry.getKey());
      assertArrayEquals(bytes, mapEntry.getValue());
    }
  }

  static void writeToPng(byte x, byte[] arr, String filename) throws Exception {
    // 1st pixel for x and array length, rest for array (4 bytes per pixel)
    int pixelsNeeded = 1 + (int) Math.ceil(arr.length / 4.0);
    int width = (int) Math.ceil(Math.sqrt(pixelsNeeded));
    int height = (int) Math.ceil((double) pixelsNeeded / width);

    var bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

    // 1st pixel: alpha=x, red=length[0], green=length[1], blue=length[2]
    int lenR = (arr.length >> 16) & 0xff;
    int lenG = (arr.length >> 8) & 0xff;
    int lenB = arr.length & 0xff;
    bufferedImage.setRGB(0, 0, ((x & 0xff) << 24) | (lenR << 16) | (lenG << 8) | lenB);

    // fill remaining pixels with array data
    int j = 0;
    for (int i = 1; i < pixelsNeeded; i++) {
      int a = j < arr.length ? arr[j++] & 0xff : 0;
      int r = j < arr.length ? arr[j++] & 0xff : 0;
      int g = j < arr.length ? arr[j++] & 0xff : 0;
      int b = j < arr.length ? arr[j++] & 0xff : 0;
      bufferedImage.setRGB(i % width, i / width, (a << 24) | (r << 16) | (g << 8) | b);
    }
    ImageIO.write(bufferedImage, "png", new File(filename));
  }

  static Map.Entry<Byte, byte[]> readFromPng(String filename) throws Exception {
    var bufferedImage = ImageIO.read(new File(filename));
    int firstPixel = bufferedImage.getRGB(0, 0);
    byte x = (byte) ((firstPixel >> 24) & 0xff);
    int lenR = (firstPixel >> 16) & 0xff;
    int lenG = (firstPixel >> 8) & 0xff;
    int lenB = firstPixel & 0xff;
    int arrLen = (lenR << 16) | (lenG << 8) | lenB;

    byte[] arr = new byte[arrLen];
    int j = 0;
    int width = bufferedImage.getWidth();
    int height = bufferedImage.getHeight();
    for (int i = 1; j < arrLen & i < width * height; i++) {
      int argb = bufferedImage.getRGB(i % width, i / width);
      arr[j++] = (byte) ((argb >> 24) & 0xff);
      if (j < arrLen) arr[j++] = (byte) ((argb >> 16) & 0xff);
      if (j < arrLen) arr[j++] = (byte) ((argb >> 8) & 0xff);
      if (j < arrLen) arr[j++] = (byte) (argb & 0xff);
    }
    return new AbstractMap.SimpleImmutableEntry<>(x, arr);
  }

  // new DecimalFormat("#,###.##")
  @Test
  void test2() {
    var decimalFormat = new DecimalFormat("#,###.##");
    decimalFormat.setMinimumFractionDigits(2);
    var totalProfilt = BigDecimal.ZERO;
    var fee = BigDecimal.valueOf(1.81);
    var step = BigDecimal.valueOf(0.01);
    var sharesPerTransaction = BigDecimal.valueOf(1000);
    var stockName = "QMMM";
    var stockDesc = "QMMM HLDGS LTD A";

    String sell =
        """
        <tr><td>{f}</td><td><span>Sell</span><div><sdps-button variation="quaternary" class="sdps-display-inline-block hydrated"><button class="sdps-button sdps-button--quaternary" type="button"> Trade Details </button></sdps-button></div></td><td><span>{e}</span><span class="sdps-display-block">{g}</span></td><td class="sdps-text-right"><span class="sdps-display-block">{d}</span></td><td class="sdps-text-right"><span>${a}</span></td><td class="sdps-text-right">${c}</td><td class="sdps-text-right">${b}</td></tr>""";
    /*
        for (var price = BigDecimal.valueOf(12.22); price.compareTo(BigDecimal.valueOf(12.17)) >= 0; price = price.subtract(step)) {
             BigDecimal profit = price.multiply(sharesPerTransaction).subtract(fee);
             totalProfilt = totalProfilt.add(profit);
             System.out.println(sell
                 .replace("{a}", decimalFormat.format(price))
                 .replace("{b}", decimalFormat.format(profit))
                 .replace("{c}", decimalFormat.format(fee))
                 .replace("{d}", sharesPerTransaction.toString())
                 .replace("{f}", "08/13/2025")
                 .replace("{e}", stockName)
                 .replace("{g}", stockDesc));
        }
    */

    String buy =
        """
        <tr><td>{f}</td><td><span>Buy</span><div><sdps-button variation="quaternary" class="sdps-display-inline-block hydrated"><button class="sdps-button sdps-button--quaternary" type="button"> Trade Details </button></sdps-button></div></td><td><span>{e}</span><span class="sdps-display-block">{g}</span></td><td class="sdps-text-right"><span class="sdps-display-block">{d}</span></td><td class="sdps-text-right"><span>${a}</span></td><td class="sdps-text-right"></td><td class="sdps-text-right">-${b}</td></tr>""";
    for (var price = BigDecimal.valueOf(2.67);
        price.compareTo(BigDecimal.valueOf(2.69)) <= 0;
        price = price.add(step)) {
      for (int i = 0; i < 16; i++) {
        BigDecimal profit = price.multiply(sharesPerTransaction);
        totalProfilt = totalProfilt.subtract(profit);
        System.out.println(
            buy.replace("{a}", decimalFormat.format(price))
                .replace("{b}", decimalFormat.format(profit))
                .replace("{d}", sharesPerTransaction.toString())
                .replace("{f}", "08/14/2025")
                .replace("{e}", stockName)
                .replace("{g}", stockDesc));
      }
    }

    System.out.println("Profit: " + decimalFormat.format(totalProfilt));
  }
}
