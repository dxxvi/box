package home.books;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.randomUUID;

import home.Tuple2;
import home.Utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

class AlgebraIAIOForDummies {
  private static final String FILENAME = "algebra-I-all-in-ones-for-dummies.txt";
  private static final Path PATH_TXT = Path.of("src/test/resources", FILENAME);
  private static final Path PATH_HTML =
      Path.of("src/test/resources", FILENAME.replace(".txt", ".html"));
  private static final String CSS =
      """
      p.Normal-w-icon > img { display: block; float: left; margin-right: 1em }
      p.Normal-w-icon:has(> img):after { content: ' '; display: block; clear: both }
      p.Part--, p.Part-- + h1.Part-Title { display: inline-block }
      p.Chap--, p.Chap-- + h1.Chap-Title { display: inline-block }
      nav#toc ol > li, section[aria-label="chapter opening"] ol > li { list-style-type: none }
      nav#toc a[href] { color: #338 } nav#toc a[href]:hover { color: #24f }
      figure table { border-collapse: collapse }
      figure table th, figure table td { border: 1px solid #ccc; padding: .4em .8em }
      figure table th p, figure table td p { margin: 0 }
      h1.Chap-Title { color: #f0811a; font-family: "Noto Sans", sans-serif }
      span.Answer-Section-Question--0:after, span.Answer-Section-Question--:after {
        content: '.'; color: #8817eb; display: inline-block; margin-right: .5em
      }
      """;

  @Test
  void test() throws Throwable {
    Tuple2<String, Map<Integer, String>> tuple =
        Utils.extractPres(Files.readString(PATH_TXT, UTF_8));
    var document = Jsoup.parse(tuple._1());

    Utils.addStuff(
        document,
        Utils.decodeThenDecryptThenDecode(
            "StqFOy2xyl1Oef0qIJjfgrMb7zj6ycHMsCyIrG5z_xa-aXgFfGGNZnk_ZZ5Mo1tD"),
        CSS);

    // specific for each book
    document.select("img").forEach(img -> img.attr("loading", "lazy"));
    document.select("a[target=\"_blank\"]").forEach(a -> a.removeAttr("target"));
    document
        .select("a[href*=\"#\"]")
        .forEach(
            a -> {
              String href = a.attr("href");
              int i = href.indexOf('#');
              String id = href.substring(i + 1);
              a.attr("href", "#" + id);

              Element el = document.getElementById(id);
              if (el != null && el.text().equals(a.text())) {
                id = "found-" + randomUUID().toString().substring(0, 8);
                a.attr("href", "#" + id);
                el.attr("id", id);
              }
            });
    document
        .select("nav a[href]:not([href*=\"#\"])")
        .forEach(
            a -> {
              if (a.text().startsWith("Book ") && a.text().contains(": ")) {
                int i = a.text().indexOf(": ");
                String bookName = a.text().substring(i + 2);
                document.select("h1.Part-Title").stream()
                    .filter(h1 -> h1.text().equals(bookName))
                    .forEach(
                        h1 -> {
                          String id = "found-" + randomUUID().toString().substring(0, 8);
                          h1.attr("id", id);
                          a.attr("href", "#" + id);
                        });
              }
            });
    document
        .select("nav a[href]:not([href*=\"#\"])")
        .forEach(
            a -> {
              if (a.text().startsWith("Chapter ") && a.text().contains(": ")) {
                int i = a.text().indexOf(": ");
                String chapterName = a.text().substring(i + 2);
                document.select("h1.Chap-Title").stream()
                    .filter(h1 -> h1.text().equals(chapterName))
                    .forEach(
                        h1 -> {
                          String id = "found-" + randomUUID().toString().substring(0, 8);
                          h1.attr("id", id);
                          a.attr("href", "#" + id);
                        });
              }
            });
    document
        .select("nav[epub:type=\"lot\"] a[href]")
        .forEach(
            a -> {
              int i = a.attr("href").indexOf('#');
              a.attr("href", a.attr("href").substring(i));
            });
    document.select("a[href$=\".xhtml\"]").stream()
        .filter(a -> a.text().startsWith("Chapter ") && a.text().contains(": "))
        .forEach(
            a -> {
              int i = a.text().indexOf(": ");
              String chapterName = a.text().substring(i + 2);
              document.select("h1.Chap-Title[id^=\"found-\"]").stream()
                  .filter(h1 -> h1.text().equals(chapterName))
                  .forEach(h1 -> a.attr("href", "#" + h1.attr("id")));
            });
    document
        .select("section[aria-label=\"chapter opening\"] li.toc-chap a:not([href^=\"#found\"])")
        .forEach(
            a -> {
              String cssQuery = "h2[id^=\"found-\"]";
              String heading = a.text();
              if (document.select(cssQuery).stream().filter(h2 -> h2.text().equals(heading)).count()
                  == 1) {
                document.select(cssQuery).stream()
                    .filter(h2 -> h2.text().equals(heading))
                    .forEach(h2 -> a.attr("href", "#" + h2.attr("id")));
              }
            });
    document.select("li.toc-chap > a[href=\"c00.xhtml\"]").forEach(a -> a.attr("href", "#c00_1"));

    // insert the pre back and write to file
    Utils.insertPreBackThenWriteToFile(document, tuple._2(), PATH_HTML);
  }
}
