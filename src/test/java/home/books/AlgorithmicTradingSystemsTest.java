package home.books;

import static java.nio.charset.StandardCharsets.UTF_8;

import home.Tuple2;
import home.Utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

class AlgorithmicTradingSystemsTest {
  private static final String FILENAME = "algorithmic-trading-systems.txt";
  private static final Path PATH_TXT = Path.of("src/test/resources", FILENAME);
  private static final Path PATH_HTML =
      Path.of("src/test/resources", FILENAME.replace(".txt", ".html"));
  private static final String CSS =
      """
      p { line-height: 1.45 }
      h1, h2, h3, h4, h5 { font-family: "Noto Sans", sans-serif }
      h5 { font-size: 1rem; font-weight: 700; color: #2372e8 }
      h4 { font-size: 1.1rem; font-weight: 600; color: #56ad10 }
      h3 { font-size: 1.21rem; font-weight: 500; color: #d11bde }
      h2 { font-size: 1.33rem; font-weight: 400; color: #c77410 }
      h1 { font-size: 1.8rem; font-weight: 300; color: #777; margin-top: 4rem }
      body > div:first-child > div:first-child > h1 { margin-top: unset }
      li > p { margin-top: .5em; margin-bottom: 0}
      span.CaptionNumber + span.SimplePara { display: inline-block; margin-left: 1em }
      ol > li.ListItem:has(> div.ItemNumber) { list-style-type: none; display: flex }
      ol > li.ListItem > div.ItemNumber:has(+ div.ItemContent) { margin-right: 1em }
      ol > li.ListItem > div.ItemContent:last-child > p:first-child { margin-top: 0; margin-bottom: .5em }
      div.ProgramCode { background: linear-gradient(90deg, #eef2f6, #f5f7fa, #eef2f6) }
      div.ProgramCode div.LineGroup div { font-family: 'Fira Code', monospace }""";

  @Test
  void test() throws Throwable {
    Tuple2<String, Map<Integer, String>> tuple =
        Utils.extractPres(Files.readString(PATH_TXT, UTF_8));
    var document = Jsoup.parse(tuple._1());

    Utils.addStuff(
        document,
        Utils.decodeThenDecryptThenDecode(
            "oty9vWHBM78TB4exZa98bcIIgf6eMO0rj1V-mSIGRk-u3jfk3ogy4mPMruvV4vSky_lTIzbr67EDIzQ9TF38qA"),
        CSS);

    // specific for each book
    document
        .select(
            "div.ChapterContextInformation, div.AuthorGroup, ol > li.ListItem > div.ClearBoth:last-child, img + span.TextObject")
        .forEach(Element::remove);
    document
        .select("figcaption.Caption > div.CaptionContent > span.CaptionNumber + p.SimplePara")
        .forEach(
            p -> {
              Element div = p.parent();
              Element e = document.createElement("span").addClass("SimplePara").append(p.html());
              p.remove();
              div.appendChild(e);
            });

    // insert the pre back and write to file
    Utils.insertPreBackThenWriteToFile(document, tuple._2(), PATH_HTML);
  }
}
