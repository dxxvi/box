package home.ignore_me;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

class QRCodeTest {
  @Test
  void contextLoads() throws Exception {
    final Map<String /*1u~2s*/, String> DATA = new HashMap<>(999_000);

    final int SIZE = 950;
    final Set<String> KNOWN_IMAGES = new HashSet<>(999_000);
    final Point[] POINTS =
        new Point[] {
          new Point(1895, 760),
          new Point(2839, 760)
        };

    FilenameFilter filenameFilter =
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            if (!name.endsWith(".png")) {
              return false;
            }
            String shortName = name.substring(0, name.length() - 4);
            if (KNOWN_IMAGES.contains(shortName)) {
              return false;
            }
            for (char c : shortName.toCharArray()) {
              if (c < '0' || c > '9') {
                return false;
              }
            }
            return true;
          }
        };

    long lastTimeNoNewFile = -1;
    while (true) {
      File[] files = new File("/dev/shm").listFiles(filenameFilter);
      if (files == null || files.length == 0) {
        if (lastTimeNoNewFile == -1) {
          lastTimeNoNewFile = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - lastTimeNoNewFile > 300_000) {
          break;
        }
        Thread.sleep(200);
        continue;
      }
      lastTimeNoNewFile = System.currentTimeMillis();

      for (File file : files) {
        if (file.length() == 0) { // this file is corrupted
          file.delete(); // who cares if we can delete it or not
          continue;
        }
        var bufferedImage = read(file);
        if (bufferedImage == null) {
          file.delete();
          continue;
        }
        KNOWN_IMAGES.add(file.getName().substring(0, file.getName().length() - 4));
        boolean allGood = true;
        for (int i = 0; i < POINTS.length; i += 1) {
          var point = POINTS[i];
          if (point.x + SIZE > bufferedImage.getWidth()
              || point.y + SIZE > bufferedImage.getHeight()) {
            System.err.println(file.getName() + " is a wrong file?");
            allGood = false;
            continue;
          }
          BufferedImage subBi = bufferedImage.getSubimage(point.x, point.y, SIZE, SIZE);
          String text = decodeQr(subBi);
          if (text == null) {
            allGood = false;
            System.out.println("Item " + (i + 1) + " in " + file.getName() + " is bad");
            continue;
          }
          int j = text.indexOf('~');
          if (j < 0) {
            allGood = false;
            System.out.println(
                "Text in item " + (i + 1) + " in " + file.getName() + " doesn't have ~");
            continue;
          }
          int k = text.indexOf('~', j + 1);
          if (k < 0) {
            allGood = false;
            System.out.println(
                "Text in item " + (i + 1) + " in " + file.getName() + " doesn't have 2 ~'s");
            continue;
          }
          DATA.put(text.substring(0, k), text.substring(k + 1));
        }
        if (allGood) {
          if (!file.delete()) {
            System.err.println("Failed to delete " + file.getName());
          }
        }
      }
    }

    try (var outputStream = Files.newOutputStream(Path.of("data.txt"), TRUNCATE_EXISTING)) {
      for (var entry : DATA.entrySet()) {
        outputStream.write(entry.getKey().getBytes(UTF_8));
        outputStream.write('!');
        outputStream.write(entry.getValue().getBytes(UTF_8));
        outputStream.write('\n');
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private BufferedImage read(File file) {
    try {
      return ImageIO.read(file);
    } catch (IOException e) {
      System.err.println("ImageIO cannot read " + file.getName() + ": " + e.getMessage());
      return null;
    }
  }

  private String decodeQr(BufferedImage bi) { // returns null when issue
    try {
      var luminanceSource = new BufferedImageLuminanceSource(bi);
      var binaryBitMap = new BinaryBitmap(new HybridBinarizer(luminanceSource));
      var multiFormatReader = new MultiFormatReader();
      return multiFormatReader.decode(binaryBitMap).getText();
    } catch (Exception e) {
      String filename = "e-" + LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmssSSS"));
      try {
        ImageIO.write(bi, "png", new File("/dev/shm/" + filename + ".png"));
      } catch (Exception e1) {
        System.err.println("ImageIO.write failed: " + e1.getMessage());
      }
      System.err.println(
          "Unable to read this QR " + e.getMessage() + ". I write it to " + filename);
    }
    return null;
  }

  @Test
  void to7zfile() throws Exception {
    try (var stream = Files.lines(Path.of("data.txt"))) {
      String base64Str = stream
          .map(line -> {
            int i = line.indexOf('!');
            String[] array = line.substring(0, i).split("~");
            return new PageItemValue(Integer.parseInt(array[0], 36), Integer.parseInt(array[1], 36), line.substring(i + 1));
          })
          .filter(piv -> !"H".equals(piv.value))
          .sorted(new Comparator<PageItemValue>() {
            @Override
            public int compare(PageItemValue p1, PageItemValue p2) {
              if (p1.page != p2.page) {
                return p1.page - p2.page;
              }
              return p1.item - p2.item;
            }
          })
          .map(PageItemValue::value)
          .collect(Collectors.joining());
      Files.write(Path.of("file.7z"), Base64.getDecoder().decode(base64Str), CREATE, TRUNCATE_EXISTING);
    }
  }

  record PageItemValue(int page, int item, String value) {}
}
