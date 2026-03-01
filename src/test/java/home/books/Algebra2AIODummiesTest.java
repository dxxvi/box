package home.books;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import home.Utils;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jsoup.Jsoup;
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
      h2 { font: 400 1.44rem "Noto Sans JP", sans-serif; color: #ff7452 }
      h3 { font: 500 1.2rem "Noto Sans JP", sans-serif; color: #14ab75 }
      h4 { font: 700 1rem "Noto Sans JP", sans-serif; color: #9225e9 }
      p.Normal-w-icon > img:first-child, p.Exam-Questiont > img:first-child, p.Exam-Question > img:first-child {
        float: left; margin-right: 1rem; margin-bottom: .5rem }
      p.Normal-w-icon:has(> img:first-child) + *,
      p.Exam-Questiont:has(> img:first-child) + *,
      p.Exam-Question:has(> img:first-child) + * { clear: both }
      """;

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
            </script>""");

    Files.writeString(PATH_HTML, "<!DOCTYPE html>" + document, CREATE, TRUNCATE_EXISTING);
  }
}
