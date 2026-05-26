package home.books;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import home.Utils;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

class Algebra2AIODummiesTest {
  private static final Path PATH_TXT = Path.of("src/test/resources/algebra2-aio-dummies.txt");
  private static final Path PATH_HTML = Path.of("src/test/resources/algebra2-aio-dummies.html");
  private static final String CSS =
      """
      a { text-decoration: none }
      th p, td p { margin: 0 }
      section[role="doc-chapter"] { margin-top: 6rem; display: flex; flex-wrap: wrap; align-items: center; gap: 1rem }
      section[role="doc-chapter"] > *:nth-child(1), section[role="doc-chapter"] > *:nth-child(2) { margin: 0 }
      section[role="doc-chapter"] > *:nth-child(n + 3) { flex: 0 0 100% }
      p.Chap--, h1.Chap-Title { font-family: "Noto Sans JP", sans-serif; font-size: 1.72rem }
      h1.Chap-Title { font-weight: 300; color: #888 }
      h1 { font: 300 1.69rem "Noto Sans JP", sans-serif; color: #777; margin-top: 2rem }
      p.Intro-Head, p.Intro-Text { margin: 0 }
      p.Intro-Text b { font-weight: 200 }
      h2 { font: 400 1.44rem "Noto Sans JP", sans-serif; color: #ff7452 }
      h3 { font: 500 1.2rem "Noto Sans JP", sans-serif; color: #14ab75 }
      h4 { font: 700 1rem "Noto Sans JP", sans-serif; color: #9225e9 }
      p.Normal-w-icon > img:first-child, p.Exam-Questiont > img:first-child, p.Exam-Question > img:first-child {
        float: left; margin-right: 1rem; margin-bottom: .5rem }
      p.Normal-w-icon:has(> img:first-child) + *,
      p.Exam-Questiont:has(> img:first-child) + *,
      p.Exam-Question:has(> img:first-child) + * { clear: both }
      aside > div.sidebar { margin-left: 3rem; border-left: .2rem solid #ccc; padding-left: 1rem }
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
    var document = Jsoup.parse(Files.readString(PATH_TXT, StandardCharsets.UTF_8));

    Utils.addStuff(document, "Algebra II All-in-one for Dummies", CSS);

    document
        .head()
        .append(
            """
            <script>document.documentElement.style.fontSize = '22px';</script>
            <link href="https://fonts.googleapis.com/css2?family=Chiron+Sung+HK:ital,wght@0,200..900;1,200..900&display=swap" rel="stylesheet">
            <script>
              function hideTOC(event) {
                if (event.target === event.currentTarget) {
                  document.getElementById('toc-chkbox').checked = false;
                }
              }
            </script>""")
        .append(
            """
            <script>
              function hideTOC(event) {
                if (event.target === event.currentTarget) {
                  document.getElementById('toc-chkbox').checked = false;
                }
              }
            </script>""");

    // build the TOC
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
    AtomicInteger chapterNumber = new AtomicInteger(0);
    document
        .select("h1:not(aside *), h2:not(aside *), h3:not(aside *)")
        .forEach(
            h -> {
              String tocText = h.text();
              String id = "_id" + ai.incrementAndGet();
              h.attr("id", id);
              String cssClass = "toc-3";
              if (h.nameIs("h1")) {
                cssClass = "toc-1";
                tocText = "Chapter " + chapterNumber.incrementAndGet() + ". " + tocText;
              } else if (h.nameIs("h2")) {
                cssClass = "toc-2";
              }
              innerTocDiv.append(
                  """
                  <a href="#%s" class="%s">%s</a>"""
                      .formatted(id, cssClass, tocText));
            });
    tocDiv.appendChild(innerTocDiv);
    document.body().appendChild(tocDiv);

    Files.writeString(PATH_HTML, "<!DOCTYPE html>" + document, CREATE, TRUNCATE_EXISTING);
  }
}
