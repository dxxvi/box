package home;

import static java.nio.charset.StandardCharsets.*;
import static java.nio.file.StandardOpenOption.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The reason I don't use Spring Boot is that sometimes I don't have IntelliJ Ultimate.
 * This class can receive http POST requests (meta.ai helps me)
 */
public class Main {
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws Throwable {
    final int SERVER_PORT = 8080;
    final String ENDPOINT = "/";

    var httpServer = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
    httpServer.createContext(ENDPOINT, new MyHandler());
    httpServer.setExecutor(null);
    httpServer.start();
    LOG.info("Server started on port {}", SERVER_PORT);
  }

  static class MyHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      final String POST_METHOD = "POST";
      final String FILENAME = "algebra-I-all-in-ones-for-dummies.txt";
      final Path PATH = Path.of("src/test/resources", FILENAME);

      if (exchange.getRequestMethod().equals(POST_METHOD)) {
        byte[] bytes = exchange.getRequestBody().readAllBytes();
        LOG.info("Received POST request of {} bytes", bytes.length);

        Files.write(PATH, bytes, CREATE, APPEND);
        Files.writeString(PATH, "\n", CREATE, APPEND);

        final String RESPONSE = "OK";
        exchange.sendResponseHeaders(200, RESPONSE.length());
        var outputStream = exchange.getResponseBody();
        outputStream.write(RESPONSE.getBytes(UTF_8));
        outputStream.close();
      } else {
        LOG.warn("Http method {} is not accepted", exchange.getRequestMethod());
      }
    }
  }
}
