package home.books;

import static java.nio.charset.StandardCharsets.UTF_8;

import home.Tuple2;
import home.Utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

class ReactQuickly2ndTest {
  private static final String FILENAME = "react-quickly-2nd.txt";
  private static final Path PATH_TXT = Path.of("src/test/resources", FILENAME);
  private static final Path PATH_HTML =
      Path.of("src/test/resources", FILENAME.replace(".txt", ".html"));
  private static final String CSS =
      """
      .float-left { float: left }
      .clear-both { clear: both }
      .flex { display: flex }
      h1, h2, h3 { font-family: 'Noto Sans' }
      h1 { font-size: 1.67rem; font-weight: 300; color: #666 }
      h2 { font-size: 1.32rem; font-weight: 400; color: #26f }
      h3 { font-size: 1.15rem; font-weight: 400; color: #f26 }
      div.chapter:not(:first-child) { margin-top: 10rem }
      p.fm-callout { margin-left: 3em; margin-right: 2em }
      p > span.fm-callout-head {
        display: inline-block; padding: .1rem .5rem; font-family: 'Noto Sans', sans-serif;
        background-color: #afa; font-weight: 700; text-transform: uppercase
      }
      p span.fm-code-in-text {
        font: .9rem 'Fira Code', monospace; background: #f3f3f3; display: inline-block;
        padding-left: .2rem; padding-right: .2rem
      }
      .fm-head2 { font-variant: small-caps; color: #81f }
      div.flex > p.body,
      div.flex > p.fm-code-listing-caption,
      div.flex > p.fm-sidebar-text,
      div.flex > div.orm-ChapterReader-codeSnippetContainer > pre.programlisting,
      div.flex > div.annotation > p.fm-code-annotation-mob:first-child {
        margin-top: 0; margin-bottom: 0;
      }
      div.flex > div.orm-ChapterReader-codeSnippetContainer, div.flex > div.annotation {
        margin-left: 1rem
      }
      pre.programlisting {
        background: linear-gradient(90deg, #f4f4f4, #fff, #f4f4f4); padding: .2rem .4rem;
        border-radius: .4rem
      }
      p.fm-figure-caption { margin-top: 0; text-align: right; font-style: italic }
      div.fm-sidebar-block {
        margin-left: 4rem; padding-left: 1rem; border-radius: .5rem; border-top: 0; border-right: 0;
        border-left: 2px solid; border-bottom: 2px solid; border-color: #eee
      }
      div.fm-sidebar-block > p.fm-sidebar-title { font-variant: small-caps }
      table { border-collapse: collapse; }
      th, td { border: 1px solid #ccc }
      th p, td p { margin: 0 }""";

  @Test
  void test() throws Throwable {
    Tuple2<String, Map<Integer, String>> tuple =
        Utils.extractPres(Files.readString(PATH_TXT, UTF_8));
    var document = Jsoup.parse(tuple._1());

    Utils.addStuff(document, "React Quickly, 2nd Edition", CSS);

    // specific for each book
    document.select("img").forEach(img -> img.attr("loading", "lazy"));

    document
        .select("p.co-summary-head + ul + *")
        .forEach(
            el -> {
              el.addClass("clear-both");
              Element ul = el.previousElementSibling();
              ul.addClass("float-left");
              Element p = ul.previousElementSibling();
              p.addClass("float-left");
            });

    document
        .select("p + div.orm-ChapterReader-codeSnippetContainer")
        .forEach(
            div -> {
              Element p = div.previousElementSibling();
              if (!p.text().endsWith(":") && !p.hasClass("fm-code-listing-caption")) {
                return;
              }
              List<Element> annotationElements = new ArrayList<>();
              Element maybeAnnotation = div.nextElementSibling();
              while (maybeAnnotation != null
                  && maybeAnnotation.hasClass("fm-code-annotation-mob")) {
                annotationElements.add(maybeAnnotation);
                maybeAnnotation = maybeAnnotation.nextElementSibling();
              }
              Element newDiv = document.createElement("div").addClass("flex");
              p.before(newDiv);
              newDiv.appendChild(p);
              newDiv.appendChild(div);
              if (!annotationElements.isEmpty()) {
                Element annotationDiv = document.createElement("div").addClass("annotation");
                for (Element annotationElement : annotationElements) {
                  annotationDiv.appendChild(annotationElement);
                }
                newDiv.appendChild(annotationDiv);
              }
            });

    document
        .body()
        .append(
            """
        <script>
          (function() {
            document.head.parentElement.style.fontSize = '20px';
          })();
        </script>""");

    // build the TOC
    Element tocDiv = document.createElement("div").attr("id", "toc");
    tocDiv.append(
        """
        <label for="toc-chkbox" style="cursor: pointer">Table of Contents</label>
        <input type="checkbox" id="toc-chkbox" style="visibility: hidden">""");
    Element innerTocDiv = document.createElement("div");
    AtomicInteger ai = new AtomicInteger();
    document
        .select("h1, h2, h3")
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
