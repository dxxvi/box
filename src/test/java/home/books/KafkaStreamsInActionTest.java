package home.books;

import static java.nio.charset.StandardCharsets.UTF_8;

import home.Tuple2;
import home.Utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

class KafkaStreamsInActionTest {
  private static final String FILENAME = "kafka-streams-in-action.txt";
  private static final Path PATH_TXT = Path.of("src/test/resources", FILENAME);
  private static final Path PATH_HTML =
      Path.of("src/test/resources", FILENAME.replace(".txt", ".html"));
  private static final String CSS =
      """
      p, li { line-height: 1.45 }
      h1, h2, h3, h4, h5, h6 { font-family: "Noto Sans", sans-serif }
      h3 { font-size: 1.09rem; font-weight: 600; color: #f0811a }
      h2 { font-size: 1.27rem; font-weight: 500; color: #4b1aeb }
      h1 { font-size: 1.9rem; font-weight: 400; color: #555 }
      h5 { font-size: 1em; margin-bottom: .5rem; color: #069799; font-weight: 500 }
      h5 strong { font-weight: 600 }
      .num-string { margin-right: 1em }
      div.introduction-summary:has( > h3) { display: flex }
      div.introduction-summary > * { margin-top: 0 }
      div.introduction-summary > h3 { color: #444; font-size: 1rem; font-weight: 400 }
      table { border-collapse: collapse } td { border: 1px solid #999; padding: .4em .8em }
      p > code { font-size: .97rem; background: #f1f2f1; padding: .1em .2em }
      div.print-book-callout.note, div.print-book-callout.tip {
        margin: 1rem 1.7rem 1rem 2rem; padding: .2rem .8rem; border-radius: .2rem }
      div.print-book-callout.note { background-color: #f7f7f7 }
      div.print-book-callout.tip { background-color: #f7fff7 }
      .print-book-callout-head.tip, .print-book-callout-head.note {
        font-weight: 700; text-transform: uppercase; margin-right: 1em;
        font-family: "Noto Sans", sans-serif }
      div.flex { display: flex }
      div.flex > h5:first-child { margin-top: 1em; margin-right: 1em }
      div.flex > p { margin-right: 1em }
      p:has(+ div.flex > h5:first-child + div), p:has(+ div.flex > p) { margin-bottom: 0 }
      h3 + div.flex > * { margin-top: 0 }
      pre[data-code-area-highlighted="true"] { background-color: #f5f6f7; padding: .5rem }
      code.hljs span.hljs-comment { font-style: italic; color: #888 }
      code.hljs span.hljs-string { color: #3e4287 }
      h1:has(span.chapter-title-numbering) { margin-top: 4em }""";

  @Test
  void test() throws Throwable {
    Tuple2<String, Map<Integer, String>> tuple =
        Utils.extractPres(Files.readString(PATH_TXT, UTF_8));
    var document = Jsoup.parse(tuple._1());

    Utils.addStuff(
        document,
        Utils.decodeThenDecryptThenDecode(
            "x1uJZe34UVgU5Ex3q3kyIOy0B8Wre7c0AIFnUyar1Rjq-9x96oYjLGQFTiPSj-SGH2ZDVdjz0ptZo_CYCQ9MMg"),
        CSS);

    // specific for each book
    document.select("img").forEach(img -> img.removeAttr("style"));
    var image2width =
        Map.ofEntries(
            Map.entry("1-1.png", 59),
            Map.entry("1-2.png", 43),
            Map.entry("1-3.png", 44),
            Map.entry("1-4.png", 45),
            Map.entry("1-5.png", 56),
            Map.entry("1-6.png", 45),
            Map.entry("1-7.png", 52),
            Map.entry("1-8.png", 42),
            Map.entry("1-9.png", 28),
            Map.entry("1-10.png", 47),
            Map.entry("1-11.png", 63),
            Map.entry("1-12.png", 61),
            Map.entry("2-1.png", 33),
            Map.entry("2-2.png", 44),
            Map.entry("2-3.png", 45),
            Map.entry("2-4.png", 20),
            Map.entry("2-5.png", 27),
            Map.entry("2-6.png", 39),
            Map.entry("2-7.png", 44),
            Map.entry("2-8.png", 27),
            Map.entry("2-9.png", 12),
            Map.entry("2-10.png", 50),
            Map.entry("2-11.png", 44),
            Map.entry("2-12.png", 34),
            Map.entry("2-13.png", 51),
            Map.entry("2-14.png", 36),
            Map.entry("2-15.png", 37),
            Map.entry("2-16.png", 44),
            Map.entry("2-17.png", 44),
            Map.entry("3-1.png", 43),
            Map.entry("3-2.png", 43),
            Map.entry("3-3.png", 47),
            Map.entry("3-4.png", 48),
            Map.entry("3-5.png", 48),
            Map.entry("3-6.png", 45),
            Map.entry("3-7.png", 54),
            Map.entry("3-8.png", 57),
            Map.entry("3-9.png", 44),
            Map.entry("3-10.png", 44),
            Map.entry("3-11.png", 44),
            Map.entry("4-1.png", 31),
            Map.entry("4-2.png", 31),
            Map.entry("4-3.png", 31),
            Map.entry("4-4.png", 44),
            Map.entry("4-5.png", 44),
            Map.entry("4-6.png", 40),
            Map.entry("4-7.png", 30),
            Map.entry("4-8.png", 36),
            Map.entry("4-9.png", 29),
            Map.entry("4-10.png", 29),
            Map.entry("4-11.png", 44),
            Map.entry("4-12.png", 44),
            Map.entry("4-13.png", 44),
            Map.entry("4-14.png", 44),
            Map.entry("4-15.png", 29),
            Map.entry("4-16.png", 52),
            Map.entry("4-17.png", 44),
            Map.entry("4-18.png", 44),
            Map.entry("4-19.png", 44),
            Map.entry("4-20.png", 44),
            Map.entry("4-21.png", 27),
            Map.entry("4-22.png", 44),
            Map.entry("4-23.png", 47),
            Map.entry("4-24.png", 44),
            Map.entry("5-1.png", 44),
            Map.entry("5-2.png", 44),
            Map.entry("5-3.png", 44),
            Map.entry("5-4.png", 44),
            Map.entry("5-5.png", 44),
            Map.entry("5-6.png", 25),
            Map.entry("6-1.png", 24),
            Map.entry("6-2.png", 48),
            Map.entry("6-3.png", 46),
            Map.entry("6-4.png", 48),
            Map.entry("6-5.png", 44),
            Map.entry("6-6.png", 44),
            Map.entry("6-7.png", 44),
            Map.entry("6-8.png", 42),
            Map.entry("7-2.png", 43),
            Map.entry("7-3.png", 43),
            Map.entry("7-4.png", 43),
            Map.entry("7-5.png", 43),
            Map.entry("7-6.png", 43),
            Map.entry("7-7.png", 43),
            Map.entry("7-8.png", 43),
            Map.entry("7-9.png", 43),
            Map.entry("7-10.png", 43),
            Map.entry("7-11.png", 43),
            Map.entry("7-12.png", 43),
            Map.entry("7-13.png", 43),
            Map.entry("7-14.png", 43),
            Map.entry("7-15.png", 43),
            Map.entry("7-16.png", 43),
            Map.entry("7-17.png", 43),
            Map.entry("7-18.png", 43),
            Map.entry("7-19.png", 43),
            Map.entry("7-20.png", 43),
            Map.entry("8-1.png", 43),
            Map.entry("8-2.png", 43),
            Map.entry("8-3.png", 43),
            Map.entry("8-4.png", 43),
            Map.entry("8-5.png", 43),
            Map.entry("8-6.png", 43),
            Map.entry("8-7.png", 43),
            Map.entry("8-8.png", 43),
            Map.entry("8-9.png", 43),
            Map.entry("8-10.png", 43),
            Map.entry("8-11.png", 43),
            Map.entry("8-12.png", 43),
            Map.entry("8-13.png", 43),
            Map.entry("8-14.png", 43),
            Map.entry("8-15.png", 43),
            Map.entry("8-16.png", 43),
            Map.entry("8-17.png", 43),
            Map.entry("8-18.png", 43),
            Map.entry("8-19.png", 43),
            Map.entry("8-20.png", 43),
            Map.entry("8-21.png", 43),
            Map.entry("8-22.png", 43),
            Map.entry("8-23.png", 43),
            Map.entry("8-24.png", 43),
            Map.entry("8-25.png", 43),
            Map.entry("8-26.png", 43),
            Map.entry("8-27.png", 43),
            Map.entry("8-28.png", 43),
            Map.entry("8-29.png", 43),
            Map.entry("8-30.png", 43),
            Map.entry("8-31.png", 43),
            Map.entry("9-1.png", 43),
            Map.entry("9-2.png", 43),
            Map.entry("9-3.png", 43),
            Map.entry("9-4.png", 43),
            Map.entry("9-5.png", 43),
            Map.entry("9-6.png", 43),
            Map.entry("9-7.png", 43),
            Map.entry("9-8.png", 43),
            Map.entry("9-9.png", 43),
            Map.entry("9-10.png", 43),
            Map.entry("9-11.png", 43),
            Map.entry("9-12.png", 43),
            Map.entry("9-13.png", 43),
            Map.entry("9-14.png", 43),
            Map.entry("9-15.png", 43),
            Map.entry("9-16.png", 43),
            Map.entry("9-17.png", 43),
            Map.entry("9-18.png", 43),
            Map.entry("9-19.png", 43),
            Map.entry("9-20.png", 43),
            Map.entry("9-21.png", 43),
            Map.entry("9-22.png", 43),
            Map.entry("9-23.png", 43),
            Map.entry("9-24.png", 43),
            Map.entry("9-25.png", 43),
            Map.entry("9-26.png", 43),
            Map.entry("9-27.png", 43),
            Map.entry("9-28.png", 43),
            Map.entry("9-29.png", 43),
            Map.entry("9-30.png", 43),
            Map.entry("9-31.png", 43),
            Map.entry("9-32.png", 43),
            Map.entry("9-33.png", 43),
            Map.entry("9-34.png", 43),
            Map.entry("9-35.png", 43),
            Map.entry("9-36.png", 43),
            Map.entry("9-37.png", 43),
            Map.entry("10-1.png", 43),
            Map.entry("10-2.png", 43),
            Map.entry("10-3.png", 43),
            Map.entry("10-4.png", 43),
            Map.entry("10-5.png", 43),
            Map.entry("10-6.png", 43),
            Map.entry("10-7.png", 43),
            Map.entry("11-1.png", 43),
            Map.entry("11-2.png", 43),
            Map.entry("11-3.png", 43),
            Map.entry("11-4.png", 43),
            Map.entry("11-5.png", 43),
            Map.entry("12-1.png", 43),
            Map.entry("13-1.png", 43),
            Map.entry("13-2.png", 43),
            Map.entry("13-3.png", 43),
            Map.entry("13-4.png", 43),
            Map.entry("13-5.png", 43),
            Map.entry("13-6.png", 43),
            Map.entry("14-1.png", 43),
            Map.entry("14-2.png", 43),
            Map.entry("14-3.png", 43),
            Map.entry("14-4.png", 43),
            Map.entry("14-5.png", 43));
    image2width.forEach(
        (filename, width) -> {
          document
              .select("img[src]")
              .forEach(
                  img -> {
                    if (img.attr("src").endsWith("/" + filename)) {
                      img.attr("style", "width: " + width + "rem");
                    }
                  });
        });
    List.of(
            "data-annotation-counter",
            "data-annotation-type",
            "data-aframe-id",
            "data-frame-type",
            "data-y")
        .forEach(
            unneededAttr -> {
              document.select("[" + unneededAttr + "]").forEach(el -> el.removeAttr(unneededAttr));
            });
    document
        .select("p + ul")
        .forEach(
            ul -> {
              Element p = ul.previousElementSibling();
              if (p.text().length() < 82 && p.text().endsWith(":")) {
                p.before("<div class=\"flex\">" + p.outerHtml() + ul.outerHtml() + "</div>");
                p.remove();
                ul.remove();
              }
            });
    document
        .select("p + ol")
        .forEach(
            ol -> {
              Element p = ol.previousElementSibling();
              if (p.text().length() < 82 && p.text().endsWith(":")) {
                p.before("<div class=\"flex\">" + p.outerHtml() + ol.outerHtml() + "</div>");
                p.remove();
                ol.remove();
              }
            });
    document
        .select("div.print-book-callout > p > span.print-book-callout-head")
        .forEach(
            span -> {
              Element p = span.parent();
              Element div = p.parent();
              if (span.text().equals("Tip")) {
                span.addClass("tip");
                div.addClass("tip");
              } else if (span.text().equals("Note")) {
                span.addClass("note");
                div.addClass("note");
              }
            });
    document // the Listing
        .select(
            "div > h5:first-child + div.code-area-container:last-child, div > h5:first-child + img:last-child")
        .forEach(div -> div.parent().addClass("flex"));
    document
        .select("p + div > div:first-child:last-child > i[id]:last-child:first-child")
        .forEach(
            pre -> {
              Element div = pre.parent().parent();
              Element p = div.previousElementSibling();
              Element newDiv = document.createElement("div").addClass("flex");
              p.before(newDiv);
              newDiv
                  .appendChild(p)
                  .appendChild(pre); // p, pre are re-parented, so no need to remove them
              div.remove();
            });

    // insert the pre back and write to file
    Utils.insertPreBackThenWriteToFile(document, tuple._2(), PATH_HTML);
  }
}
