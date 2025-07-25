package home.books;

import static java.nio.charset.StandardCharsets.UTF_8;

import home.Tuple2;
import home.Utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.Test;

class ScalaForTheImpatient {
  private static final String FILENAME = "scala-for-the-impatient.txt";
  private static final Path PATH_TXT = Path.of("src/test/resources", FILENAME);
  private static final Path PATH_HTML =
      Path.of("src/test/resources", FILENAME.replace(".txt", ".html"));
  private static final String CSS =
      """
      ol#nav li.not-ordered, ul.square li { list-style-type: none }
      ol#nav > li.not-ordered { display: flex }
      li > p { margin: 0 }
      p span.bg-c { color: #fff; background: #000; padding: 1px 2px }
      div.flex { display: flex }
      p + div.flex > p, p + div.flex > ul { margin-top: 0; margin-bottom: 0 }
      div.tip > p.tip-title { text-transform: uppercase }
      div.tip > p.tip-title:before { display: inline-block; content: '✎'; padding-right: 1rem }
      """;

  @Test
  void test() throws Throwable {
    Tuple2<String, Map<Integer, String>> tuple =
        Utils.extractPres(Files.readString(PATH_TXT, UTF_8));
    var document = Jsoup.parse(tuple._1());

    Utils.addStuff(
        document,
        Utils.decodeThenDecryptThenDecode(
            "T5CG-czPozyOFEeZL1WAp0_Flcyb-2-L31IoP84A3H4cS9lvkTlwix1N4_WVmmol"),
        CSS);

    // specific for each book
    var pattern = Pattern.compile("^ch\\d\\d.xhtml#(.+)$");
    document
        .select("a[href^=\"ch\"]")
        .forEach(
            a -> {
              var matcher = pattern.matcher(a.attr("href"));
              if (matcher.matches()) {
                var id = matcher.group(1);
                var elementCount = document.select("[id=\"" + id + "\"]").size();
                switch (elementCount) {
                  case 1 -> a.attr("href", "#" + id);
                  case 0 -> System.out.printf("No element with id %s (it might be in <pre>)\n", id);
                  default ->
                      System.out.printf("There are %d elements with id %s\n", elementCount, id);
                }
              }
            });
    document
        .select("p.chap-toc + ul.square, p.noindent + ul.bull")
        .forEach(
            ul -> {
              Element p = ul.previousElementSibling();
              if (p.hasClass("chap-toc") || p.text().endsWith(":")) {
                Element div = document.createElement("div").addClass("flex");
                p.before(div);
                div.appendChild(p).appendChild(ul);
              }
            });
    document.select("div.tip > p.tip-title > img").forEach(Node::remove);

    // insert the pre back and write to file
    Utils.insertPreBackThenWriteToFile(document, tuple._2(), PATH_HTML);
  }
}
