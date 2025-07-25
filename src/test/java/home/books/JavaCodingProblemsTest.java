package home.books;

import static java.nio.charset.StandardCharsets.*;

import home.Tuple2;
import home.Utils;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

class JavaCodingProblemsTest {
  private static final String FILENAME =
      Utils.decodeThenDecryptThenDecode(
          "W79oWhQqF6k9iS0kzGyqYBC3KjS3Cs2JGpX9Am6GD38cS9lvkTlwix1N4_WVmmol");
  private static final Path PATH_TXT = Path.of("src/test/resources", FILENAME);
  private static final Path PATH_HTML =
      Path.of("src/test/resources", FILENAME.replace(".txt", ".html"));
  private static final String IMG1_URL =
      Utils.aesDecryptToUrl(
              "0pQ7zRJnRa3glafQ/RMiW4NdLWkV7CDCFaQF+BtxlpPVcz8sT9a9NKDtgLt6K96kDVkGzibs7faRh8IfyL8t64mrlpgwZ0iTk3E+1VfQcajmKGrc3xGKFbRSvvS8c92b")
          .toString();
  private static final String CSS =
      """
      a { text-decoration: none }
      p, li { line-height: 1.45 }
      div.note {
        padding-left: 6rem;
        background-image: url(%s);
        background-repeat: no-repeat;
        background-size: 90px auto;
        background-position: 0 center;
      }
      pre {
        font-family: 'Fira Code'; font-optical-sizing: auto; color: #3D3B49;
        background-color: #eef2f6; padding: .5rem 1rem .5rem 1.5rem
      }
      pre.programlisting.code { border-left: .35rem solid #e1e1e1 }
      code { background-color: #EEF2F6; font-family: 'Fira Code'; font-size: .91rem }
      .hljs-code { color: #383a42 }
      .hljs-keyword { color: #a626a4 }
      .hljs-type { color: #986801 }
      .hljs-variable { color: #996902 }
      .hljs-string { color: #50a14f }
      .hljs-title { color: #4078f2 }
      .hljs-comment { color: #a0a1a7; font-style: italic }
      h1.chapterNumber { font: 200 1.8rem "Noto Sans"; font-optical-sizing: auto; color: #666 }
      h1.heading-1 { font-size: 1.6rem; font-weight: 400; color: #1782cf }
      h2.heading-2 { font-size: 1.4rem; font-weight: 500; color: #cf6617 }
      h3.heading-3 { font: 500 1.25rem 'Noto Sans'; color: #c114c4 }
      h4.heading-4 { font-size: 1.1rem; color: #0e9933 }
      div#toc { position: fixed; top: 0; right: 3rem; background-color: rgba(255, 255, 255, .9);
        max-height: 82vh; overflow: auto; z-index: 9; padding: 1rem; padding-top: .1rem;
        padding-bottom: .5rem; border: 1px solid #ccc; border-top: 0
      }
      #toc-chkbox + div { display: none } #toc-chkbox:checked + div { display: unset }
      div#toc a { display: block; line-height: 1.45; color: #333 }
      div#toc a:hover { color: #26f }
      div#toc a.toc-1 { padding-top: 1rem }
      div#toc a.toc-2 { padding-left: 1rem }
      div#toc a.toc-3 { padding-left: 2rem }
      div#toc a.toc-4 { padding-left: 3rem }
      div.flex { display: flex } div.flex > *:not(:first-child) { margin-left: 1rem }"""
          .formatted(IMG1_URL);

  @Test
  void test() throws Throwable {
    var messageDigest = MessageDigest.getInstance("MD5");

    Tuple2<String, Map<Integer, String>> tuple =
        Utils.extractPres(Files.readString(PATH_TXT, UTF_8));
    var document = Jsoup.parse(tuple._1());

    Utils.addStuff(
        document,
        Utils.decodeThenDecryptThenDecode(
            "B7KCzK20RUkxUkgxHUsMv5t5CKCYhlFfEfpDL1fwjxDRF5lLpEFy8clkE2Xy7Ik9"),
        CSS);

    // specific for each book
    document
        .select("h1.chapterNumber + h1.chapterTitle")
        .forEach(
            title -> {
              Element number = title.previousElementSibling();
              number.text(number.text() + ". " + title.text());
              title.remove();
            });
    // put a <p> ending with ':' followed by a div.orm-ChapterReader-codeSnippetContainer in flex
    document
        .select("p + div.orm-ChapterReader-codeSnippetContainer")
        .forEach(
            div -> {
              Element p = div.previousElementSibling();
              if (p.text().endsWith(":")) {
                Element flexDiv = document.createElement("div").addClass("flex");
                p.before(flexDiv);
                flexDiv.appendChild(p).appendChild(div);
                byte[] bytes = messageDigest.digest(flexDiv.html().getBytes(UTF_8));
                flexDiv.attr("md5", new BigInteger(1, bytes).toString(36).toLowerCase());
              }
            });
    // remove the flex class on div's with these md5's
    String[] needToHaveFlexRemovedMd5s = {"36dfkes9nvfpk10xo2zqjut75", "5gax8quit2haymxku5q3n7cmr"};
    for (String md5 : needToHaveFlexRemovedMd5s) {
      document
          .select("div.flex[md5]")
          .forEach(
              div -> {
                if (div.attr("md5").equals(md5)) div.removeClass("flex");
              });
    }

    // build the TOC
    Element tocDiv = document.createElement("div").attr("id", "toc");
    tocDiv.append(
        """
        <label for="toc-chkbox" style="cursor: pointer">Table of Contents</label>
        <input type="checkbox" id="toc-chkbox" style="visibility: hidden">""");
    Element innerTocDiv = document.createElement("div");
    AtomicInteger ai = new AtomicInteger();
    document
        .select("h1.chapterNumber, h1.heading-1, h2.heading-2, h3.heading-3")
        .forEach(
            h -> {
              String id = h.attr("id");
              if (id.isEmpty()) {
                id = "_id" + ai.incrementAndGet();
                h.attr("id", id);
              }
              Set<String> classNames = h.classNames();
              String cssClass = "toc-4";
              if (classNames.contains("chapterNumber")) {
                cssClass = "toc-1";
              } else if (classNames.contains("heading-1")) {
                cssClass = "toc-2";
              } else if (classNames.contains("heading-2")) {
                cssClass = "toc-3";
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
