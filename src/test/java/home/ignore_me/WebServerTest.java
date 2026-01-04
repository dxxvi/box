package home.ignore_me;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

class WebServerTest {
  @Test
  void testToBinary() throws Exception {
    byte[] bytes = Base64.getUrlDecoder().decode(Files.readAllBytes(Path.of("/dev/shm/file.txt")));
    Files.write(Path.of("/dev/shm/src.7z"), bytes, CREATE, TRUNCATE_EXISTING);
  }

  @Test
  void test() throws Exception {
    final AtomicInteger total = new AtomicInteger(-1);
    final Map<Integer, String> map = new ConcurrentHashMap<>();

    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

    server.createContext("/", new RootHandler());
    server.createContext("/img.png", new ImageHandler(total, map));
    server.createContext("/map", new MapHandler(total, map));
    server.createContext("/dump", new DumpHandler(total, map));

    server.start();
    System.out.println("Server started on port 8080");
    System.out.println("Visit http://localhost:8080 and http://localhost:8080/img.png");
    Thread.sleep(999_999_999);
  }
}

class RootHandler implements HttpHandler {
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");

    String cookieValue = "1";
    String cookieHeader = "a=" + cookieValue + "; Path=/; HttpOnly=false; Max-Age=3600";
    exchange.getResponseHeaders().add("Set-Cookie", cookieHeader);

    byte[] responseBytes = getBytes(cookieValue);
    exchange.sendResponseHeaders(200, responseBytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(responseBytes);
    }
  }

  private static byte[] getBytes(String cookieValue) {
    String htmlResponse = """
          <!DOCTYPE html>
          <html lang="en">
          <head>
              <title></title>
              <style>
                #ta { width: 95% }
              </style>
              <script>
                  async function run() {
                    const textarea = document.getElementById('ta');
                    let lines = textarea.value
                      .split('\\n')
                      .map(line => line.trimEnd())  // remove trailing \\r if present on Windows
                      .filter(line => line !== ''); // remove empty lines
        
                    const total = lines.length;
        
                    for (let i = 0; i < total; i++) {
                      const currentLineNumber = i + 1;
                      const currentContent = lines[0];

                      document.cookie = `_=${currentLineNumber}~${total}:${currentContent}; path=/; max-age=99`;
                      document.getElementById('img').src = '/a/img.png?_=' + Date.now();

                      // Wait 2 seconds before processing the next line (but not after the very last one if you prefer)
                      if (i < total - 1) {
                        await new Promise(resolve => setTimeout(resolve, Date.now() % 1000));
                      }
                      // Update the textarea: remove the first line (the one we just processed)
                      lines.shift(); // remove the processed line from our array
                      textarea.value = lines.join('\\n');
                    }
                  }
              </script>
          </head>
          <body>
              <img id="img" src="img.png">
              <span style="display: inline-block; width: 2rem"></span>
              <button onclick="run()">Run</button>
              <br>
              <textarea id="ta"></textarea>
              <pre>document.cookie = '_=valuable data; path=/; max-age=99'; document.getElementById('img').src = '/a/img.png?a=1'</pre>
          </body>
          </html>
          """;

    return htmlResponse.getBytes(UTF_8);
  }
}

class MapHandler implements HttpHandler {
  private final AtomicInteger total;
  private final Map<Integer, String> map;

  public MapHandler(AtomicInteger total, Map<Integer, String> map) {
    this.total = total;
    this.map = map;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    StringBuilder sb = new StringBuilder();
    sb.append("""
        {"total": %d,""".formatted(total.get()));
    String str = IntStream.rangeClosed(1, total.get())
        .filter(i -> !map.containsKey(i))
        .mapToObj(Integer::toString)
        .collect(Collectors.joining(",", "\"missing\":[", "]}"));
    sb.append(str);
    byte[] responseBytes = sb.toString().getBytes(UTF_8);
    exchange.sendResponseHeaders(200, responseBytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(responseBytes);
    }
  }
}

class DumpHandler implements HttpHandler {
  private final AtomicInteger total;
  private final Map<Integer, String> map;

  public DumpHandler(AtomicInteger total, Map<Integer, String> map) {
    this.total = total;
    this.map = map;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    System.out.println("Total: " + total.get());
    try (OutputStream os = Files.newOutputStream(Path.of("dump.txt"), CREATE, TRUNCATE_EXISTING)) {
      for (Map.Entry<Integer, String> entry : map.entrySet()) {
        os.write(entry.getKey().toString().getBytes(UTF_8));
        os.write(new byte[] { '~' });
        os.write(entry.getValue().getBytes(UTF_8));
        os.write(new byte[] { '\n' });
      }
    }

    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
    byte[] responseBytes = ("Total: " + total.get()).getBytes(UTF_8);
    exchange.sendResponseHeaders(200, responseBytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(responseBytes);
    }
  }
}

class ImageHandler implements HttpHandler {
  private static final Random RANDOM = new Random();
  private static final int SQUARE_SIZE = 20;

  private final AtomicInteger total;
  private final Map<Integer, String> map;

  public ImageHandler(AtomicInteger total, Map<Integer, String> map) {
    this.total = total;
    this.map = map;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String cookiesValue = exchange.getRequestHeaders().getFirst("Cookie").substring(2);
    String[] array = cookiesValue.split(":");
    String data = array[1];
    array = array[0].split("~");
    int currentId = Integer.parseInt(array[0]);
    int currentTotal = Integer.parseInt(array[1]);

    if (total.get() == -1) {
      total.set(currentTotal);
      System.out.println("This is the 1st item of " + total);;
    } else if (currentTotal != total.get()) {
      System.err.println("Stop now! There's a bug.");
      return;
    }

    map.put(currentId, data);
    System.out.printf("Received item %d of %d%n", currentId, total.get());

    if (hasAllData()) {
      try (OutputStream os = Files.newOutputStream(Path.of("file.txt"), CREATE, TRUNCATE_EXISTING)) {
        for (int i = 1; i <= total.get(); i++) {
          os.write(map.get(i).getBytes(UTF_8));
        }
      }
      total.set(-1);
      map.clear();
      System.out.println("\n\n\nGot 1 file.\n\n\n");
    }

    BufferedImage image = createRandomImage();

    exchange.getResponseHeaders().set("Content-Type", "image/png");
    exchange.getResponseHeaders().set("Cache-Control", "no-cache");

    try (OutputStream os = exchange.getResponseBody()) {
      exchange.sendResponseHeaders(200, 0); // 0 means chunked transfer
      ImageIO.write(image, "png", os);
    }
  }

  private boolean hasAllData() {
    for (int i = 1; i <= total.get(); i++) {
      if (!map.containsKey(i)) {
        return false;
      }
    }
    return true;
  }

  private BufferedImage createRandomImage() {
    var color = new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
    var bufferedImage = new BufferedImage(SQUARE_SIZE, SQUARE_SIZE, BufferedImage.TYPE_INT_RGB);
    for (var x = 0; x < SQUARE_SIZE; x++) {
      for (var y = 0; y < SQUARE_SIZE; y++) {
        bufferedImage.setRGB(x, y, color.getRGB());
      }
    }
    return bufferedImage;
  }
}