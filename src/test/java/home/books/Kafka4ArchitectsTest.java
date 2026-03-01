package home.books;

import static java.nio.charset.StandardCharsets.UTF_8;

import home.Tuple2;
import home.Utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

class Kafka4ArchitectsTest {
  private static final Path PATH_TXT = Path.of("src/test/resources/kafka-4-architects.txt");
  private static final Path PATH_HTML = Path.of("src/test/resources/kafka-4-architects.html");
  private static final String CSS =
      """
      a { text-decoration: none }
      div.ProgramCode { font-family: "Fira Code", "Chiron Sung HK", monospace }
      div#toc { position: fixed; top: 0; right: 3rem; background-color: rgba(255, 255, 255, .9);
        max-height: 82vh; overflow: auto; z-index: 9; padding: 1rem; padding-top: .1rem;
        padding-bottom: .5rem; border: 1px solid #ccc; border-top: 0
      }
      #toc-chkbox + div { display: none }
      #toc-chkbox:checked + div { display: flex }
      div#toc a { display: block; line-height: 1.45; color: #333 }
      div#toc a:hover { color: #26f }
      div#toc a.toc-1 { padding-top: 1rem }
      div#toc a.toc-2 { padding-left: 1rem }
      div#toc a.toc-3 { padding-left: 2rem }
      div#toc a.toc-4 { padding-left: 3rem }""";

  @Test
  void test() throws Throwable {
    Tuple2<String, Map<Integer, String /*pre elements*/>> tuple =
        Utils.extractPres(Files.readString(PATH_TXT, UTF_8));
    var document = Jsoup.parse(tuple._1());

    Utils.addStuff(document, "Kafka for Architects", CSS);

    document
        .head()
        .append(
            """
            <script>document.documentElement.style.fontSize = '18px';</script>
            <link href="https://fonts.googleapis.com/css2?family=Chiron+Sung+HK:ital,wght@0,200..900;1,200..900&display=swap" rel="stylesheet">
            <script>
              function hideTOC(event) {
                if (event.target === event.currentTarget) {
                  document.getElementById('toc-chkbox').checked = false;
                }
              }
            </script>""");

    // div:has(> p ending with :) + ul: if all li's are not very long, align the p and ul
    final int MAX_LI_LENGTH = 130;
    for (Element ul : document.select("div + ul")) {
      Element div = ul.previousElementSibling();
      if (div.childrenSize() == 1
          && div.child(0).tagName().equalsIgnoreCase("p")
          && div.child(0).text().endsWith(":")) {
        if (ul.children().stream().allMatch(li -> li.text().length() < MAX_LI_LENGTH)) {
          int pWidth =
              (int) ((double) div.child(0).text().length() / (double) ul.childrenSize() * 1.2);
          div.child(0).attr("style", "max-width:" + pWidth + "ch");
          div.appendChild(ul);
          div.addClass("display-flex").addClass("p").addClass("ul");
        }
      }
    }

    // make TOC
    Element tocDiv = document.createElement("div").attr("id", "toc");
    tocDiv.append(
        """
        <label for="toc-chkbox" style="cursor: pointer">Table of Contents</label>
        <input type="checkbox" id="toc-chkbox" style="visibility: hidden">""");
    Element innerTocDiv =
        document
            .createElement("div")
            .attr("style", "flex-direction: column; align-items: flex-start")
            .attr("onclick", "hideTOC(event)");
    AtomicInteger ai = new AtomicInteger();
    document
        .select("div > h1, div > h2, div > h3:not(.introduction-header)")
        .forEach(
            h -> {
              String id = "_id" + ai.incrementAndGet();
              h.attr("id", id);
              String cssClass = "toc-3";
              if (h.nameIs("h1")) {
                cssClass = "toc-1";
              } else if (h.nameIs("h2")) {
                cssClass = "toc-2";
              }
              innerTocDiv.append(
                  """
                  <a href="#%s" class="%s">%s</a>"""
                      .formatted(id, cssClass, h.text()));
            });
    tocDiv.appendChild(innerTocDiv);
    document.body().appendChild(tocDiv);

    // insert the pre back and write to file
    Utils.insertPreBackThenWriteToFile(document, tuple._2(), PATH_HTML);
  }
}
