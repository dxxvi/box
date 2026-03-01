package home.books;

import static java.nio.charset.StandardCharsets.UTF_8;

import home.Tuple2;
import home.Utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

class PracticalDataEngineeringApacheProjectsTest {
  private static final Path PATH_TXT =
      Path.of("src/test/resources/practical-dataengineering-apache-projects.txt");
  private static final Path PATH_HTML =
      Path.of("src/test/resources/practical-dataengineering-apache-projects.html");
  private static final String CSS =
      """
      a { text-decoration: none }
      h1 { color: #999; font-weight: 300; font-size: 1.7rem; margin-top: 4rem }
      h2 { color: #ff9759; font-weight: 400; font-size: 1.44rem }
      h3 { color: #23b1be; font-weight: 500; font-size: 1.2rem }
      div.AuthorGroup, div.ChapterContextInformation { display: none }
      div.ProgramCode {
        padding: .5rem 1rem; background: linear-gradient(90deg, #f3fef3, #fff, #f3fef3);
        font-family: "Fira Code", "Chiron Sung HK", monospace; font-size: .9rem }
      figure.Figure figcaption.Caption div.CaptionContent {
        display: flex; font-family: "Noto Sans JP", sans-serif; color: #8629cd; font-size: .9rem }
      figure.Figure figcaption.Caption div.CaptionContent p { margin: 0 0 0 1rem }
      ol { list-style-type: none }
      li.ListItem { display: flex }
      li.ListItem div.ItemNumber { flex-shrink: 0 }
      li.ListItem div.ItemContent { flex: 1 }
      li.ListItem div.ItemContent p { margin: 0 }
      th > p, td > p { margin: 0 }
      .EmphasisFontCategoryNonProportional { font: 400 .88rem "Noto Sans JP", sans-serif }
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

    Utils.addStuff(document, "Practical Data Engineering with Apache Projects", CSS);

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
            </script>""");

    // insert the pre back and write to file
    Utils.insertPreBackThenWriteToFile(document, tuple._2(), PATH_HTML);
  }

  @Test
  void test2() throws Throwable {
    var pattern = Pattern.compile(" id=\"[a-zA-Z\\d]+\"");
    String text = Files.readString(PATH_TXT, UTF_8);
    pattern
        .matcher(text)
        .results()
        .map(MatchResult::group)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .forEach((subString, count) -> System.out.printf("%s: %s%n", subString, count));
  }
}
