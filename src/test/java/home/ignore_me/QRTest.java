package home.ignore_me;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.junit.jupiter.api.Test;

class QRTest {
  @Test
  void test() throws FrameGrabber.Exception {
    try (var frameGrabber = new FFmpegFrameGrabber("/dev/shm/video.mp4");
        var java2DFrameConverter = new Java2DFrameConverter();
        var executorService = Executors.newFixedThreadPool(9); ) {
      frameGrabber.start();

      final ConcurrentHashMap<String, Boolean> filenames = new ConcurrentHashMap<>();

      for (int i = 0; i < frameGrabber.getLengthInFrames(); i++) {
        var frame = frameGrabber.grab();
        if (frame == null || frame.image == null) {
          continue;
        }

        var bufferedImage = java2DFrameConverter.convert(frame);
        if (bufferedImage != null) {
          var bi = bufferedImage.getSubimage(1, 80, 999, 980);
          executorService.submit(new MyTask(bi, filenames));
        }
      }

      frameGrabber.stop();
    }
  }

  private void writeBufferedImageToFile(BufferedImage bi, File file) {
    try {
      ImageIO.write(bi, "png", file);
    } catch (IOException ioe) {
      System.err.println("IOImage cannot write to " + file.getAbsolutePath());
    }
  }

  private static class MyTask implements Runnable {
    private final BufferedImage bi;
    private final ConcurrentHashMap<String, Boolean> filenames; // the values are ignored

    public MyTask(BufferedImage bi, ConcurrentHashMap<String, Boolean> filenames) {
      this.bi = bi;
      this.filenames = filenames;
    }

    @Override
    public void run() {
      var binaryBitmap =
          new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bi)));
      try {
        Result result = new MultiFormatReader().decode(binaryBitmap);
        String qrCodeData = result.getText();
        String[] parts = qrCodeData.split(":", 2);
        String fileName = parts[0];
        if (parts.length > 1 && !filenames.containsKey(fileName)) {
          byte[] fileContent = Base64.getDecoder().decode(parts[1]);
          Files.write(Path.of("/dev/shm", fileName), fileContent);
          filenames.put(fileName, Boolean.TRUE);
        }
      } catch (NotFoundException nfe) {
        /*
                    var file = new File("/dev/shm/bad-qr-" + i + ".png");
                    writeBufferedImageToFile(bi, file);
        */
      } catch (IOException ioe) {
        System.err.println("Should not see this");
      }
    }
  }
}
