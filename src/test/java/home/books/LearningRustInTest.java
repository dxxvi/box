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
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

class LearningRustInTest {
  private static final String FILENAME =
      Utils.decodeThenDecryptThenDecode(
          "iVAjZOE2mLRT5AxOqLzbKl4tbMH0PYEIXapClpWiPMLCCMvfdC33DeN68tloPe0G4_Iep4wyigwqc5MKFeFMNA");
  private static final Path PATH_TXT = Path.of("src/test/resources", FILENAME);
  private static final Path PATH_HTML =
      Path.of("src/test/resources", FILENAME.replace(".txt", ".html"));
  private static final String CSS =
      """
      :root {
        --pre-background: #f1f6fa;
      }
      .smaller-font-size { font-size: 82% }
      a { text-decoration: none }
      li > p { margin: 0 }
      h1, h2, h3 { font-family: 'Noto Sans JP' }
      h1 { font-size: 1.67rem; font-weight: 300; color: #666; margin-top: 9rem }
      h2 { font-size: 1.32rem; font-weight: 400; color: #26f }
      h3 { font-size: 1.15rem; font-weight: 400; color: #f26 }
      body code[class*=language-], body pre[class*=language-] { font-family: 'Fira Code', monospace; font-size: .9em }
      body .token.comment { font-style: italic }
      body :not(pre)>code[class*=language-], body pre[class*=language-] {
        background: linear-gradient(90deg, #f5f2f0, #fcf9f7, #f5f2f0) }
      pre.programlisting:not(.language-rust) {
        margin-block: 0; padding: .5em 1em; background: linear-gradient(90deg, #f7f7f7, #fafafa, #f7f7f7) }
      div.multi-column { display: flex }
      div.multi-column > div.orm-ChapterReader-codeSnippetContainer { margin-left: 1rem; margin-right: 1rem }
      div.multi-column > div.orm-ChapterReader-codeSnippetContainer:last-child { margin-right: 0 }
      p.fm-callout { margin-left: 2rem }
      .fm-callout-head { font: 700 1em "Noto Sans JP", sans-serif; color: #12de1e }
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
      div#toc a.toc-4 { padding-left: 3rem }
      span.fm-combinumeral { font-family: "Chiron Sung HK",serif; font-weight: bold }""";

  @Test
  void test() throws Throwable {
    Tuple2<String, Map<Integer, String /*pre elements*/>> tuple =
        Utils.extractPres(Files.readString(PATH_TXT, UTF_8));
    var document = Jsoup.parse(tuple._1());

    Utils.addStuff(
        document,
        Utils.decodeThenDecryptThenDecode(
            "I-UppN2YhY3PJna2uTeDPKnBqOpm4Fe7QbH7dtQq25u4V9jRMRRbG03oA-OmlHXqHEvZb5E5cIsdTeP1lZpqJQ"),
        CSS);

    // remove the <span class="fm-combinumeral"> and </span> in the values in tuple._2
    Map<Integer, String> map =
        tuple._2().entrySet().stream()
            .peek(
                e -> {
                  if (!e.getValue().startsWith("<pre>"))
                    System.out.println("Check this pre: " + e.getValue());
                })
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry ->
                        entry
                            .getValue()
                            .replace("<span class=\"fm-combinumeral\">", "")
                            .replace("</span>", "")));
    tuple._2().putAll(map);

    document
        .head()
        .append(
            """
            <script>document.documentElement.style.fontSize = '18px';</script>
            <link href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.30.0/themes/prism.min.css" rel="stylesheet" />
            <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.30.0/prism.min.js"></script>
            <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.30.0/components/prism-rust.min.js"></script>
            <link href="https://fonts.googleapis.com/css2?family=Chiron+Sung+HK:ital,wght@0,200..900;1,200..900&display=swap" rel="stylesheet">""")
        .append(
            """
            <script>
              function hideTOC(event) {
                if (event.target === event.currentTarget) {
                  document.getElementById('toc-chkbox').checked = false;
                }
              }
            </script>""");

    // align the chapter summary head
    for (Element ul : document.select("p.co-summary-head + ul")) {
      Element p = ul.previousElementSibling();
      Element div = document.createElement("div").attr("style", "display: flex");
      p.before(div);
      div.appendChildren(List.of(p, ul));
    }

    // arrange p (with content ending with `:`), code and code annotations (we don't have the pre el
    // here but the i)
    for (Element iel : document.select("p + div.orm-ChapterReader-codeSnippetContainer > i[id]")) {
      Element div = iel.parent();
      Element p = div.previousElementSibling();
      if (!p.text().endsWith(":") && !p.hasClass("pseudo-semicolon")) continue;

      List<Element> codeAnnotationEls = new ArrayList<>();
      Element e = div;
      while (true) {
        Element needToCheckEl = e.nextElementSibling();
        if (needToCheckEl != null && needToCheckEl.hasClass("fm-code-annotation")) {
          codeAnnotationEls.add(needToCheckEl);
          e = needToCheckEl;
          continue;
        }
        break;
      }

      Element codeAnnotationDiv = document.createElement("div").appendChildren(codeAnnotationEls);

      Element flexDiv = document.createElement("div").addClass("multi-column");
      p.before(flexDiv);
      flexDiv.appendChildren(List.of(p, div, codeAnnotationDiv));
    }

    for (Element p : document.select("p.combine-next")) {
      Element nextEl = p.nextElementSibling();
      Element div = document.createElement("div").addClass("multi-column");
      p.before(div);
      div.appendChildren(List.of(p, nextEl));
    }

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

    // format Rust code with Prism
    document
        .body()
        .append(
            """
            <script>
              document.querySelectorAll('.language-rust').forEach(el => Prism.highlightElement(el));
            </script>""");

    // insert the pre back and write to file
    Utils.insertPreBackThenWriteToFile(document, tuple._2(), PATH_HTML);
  }
}
