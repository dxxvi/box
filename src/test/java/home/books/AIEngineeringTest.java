package home.books;

import static java.nio.charset.StandardCharsets.UTF_8;

import home.Tuple2;
import home.Utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

class AIEngineeringTest {
  private static final String FILENAME = "AI-Engineering.txt";
  private static final Path PATH_TXT = Path.of("src/test/resources", FILENAME);
  private static final Path PATH_HTML =
      Path.of("src/test/resources", FILENAME.replace(".txt", ".html"));
  private static final String CSS =
      """
      """;

  @Test
  void test() throws Throwable {
    Tuple2<String, Map<Integer, String /*pre elements*/>> tuple =
        Utils.extractPres(Files.readString(PATH_TXT, UTF_8));
    var document = Jsoup.parse(tuple._1());

    Utils.addStuff(document, "AI Engineering", CSS);

    // specific to each book
    var figure2width = new LinkedHashMap<String, Integer /* in rem */>();
    figure2width.put("aien_0101.png", 24);
    figure2width.put("aien_0102.png", 29);
    figure2width.put("aien_0103.png", 21);
    figure2width.put("aien_0104.png", 77);
    figure2width.put("aien_0105.png", 41);
    figure2width.put("aien_0106.png", 39);
    figure2width.put("aien_0107.png", 37);
    figure2width.put("aien_0108.png", 35);
    figure2width.put("aien_0109.png", 37);
    figure2width.put("aien_0110.png", 35);
    figure2width.put("aien_0111.png", 41);
    figure2width.put("aien_0112.png", 57);
    figure2width.put("aien_0113.png", 61);
    figure2width.put("aien_0114.png", 33);
    figure2width.put("aien_0115.png", 61);
    figure2width.put("aien_0116.png", 26);
    figure2width.put("aien_0201.png", 55);
    figure2width.put("aien_0202.png", 51);
    figure2width.put("aien_0203.png", 41);
    figure2width.put("aien_0204.png", 37);
    figure2width.put("aien_0205.png", 41);
    figure2width.put("aien_0206.png", 39);
    figure2width.put("aien_0207.png", 32);
    figure2width.put("aien_0208.png", 61);
    figure2width.put("aien_0209.png", 41);
    figure2width.put("aien_0210.png", 41);
    figure2width.put("aien_0211.png", 33);
    figure2width.put("aien_0212.png", 41);
    figure2width.put("aien_0213.png", 71);
    figure2width.put("aien_0214.png", 31);
    figure2width.put("aien_0215.png", 21);
    figure2width.put("aien_0216.png", 47);
    figure2width.put("aien_0217.png", 31);
    figure2width.put("aien_0218.png", 21);
    figure2width.put("aien_0219.png", 31);
    figure2width.put("aien_0220.png", 41);
    figure2width.put("aien_0221.png", 34);
    figure2width.put("aien_0222.png", 31);
    figure2width.put("aien_0223.png", 33);
    figure2width.put("aien_0224.png", 31);
    figure2width.put("aien_0225.png", 29);
    figure2width.put("aien_0226.png", 37);
    figure2width.put("aien_0301.png", 23);
    figure2width.put("aien_0302.png", 41);
    figure2width.put("aien_0303.png", 34);
    figure2width.put("aien_0304.png", 12);
    figure2width.put("aien_0305.png", 41);
    figure2width.put("aien_0306.png", 37);
    figure2width.put("aien_0307.png", 41);
    figure2width.put("aien_0308.png", 34);
    figure2width.put("aien_0309.png", 41);
    figure2width.put("aien_0310.png", 49);
    figure2width.put("aien_0401.png", 41);
    figure2width.put("aien_0402.png", 41);
    figure2width.put("aien_0403.png", 41);
    figure2width.put("aien_0404.png", 41);
    figure2width.put("aien_0405.png", 31);
    figure2width.put("aien_0406.png", 24);
    figure2width.put("aien_0407.png", 48);
    figure2width.put("aien_0408.png", 25);
    figure2width.put("aien_0409.png", 41);
    figure2width.put("aien_0410.png", 52);
    figure2width.put("aien_0501.png", 41);
    figure2width.put("aien_0502.png", 41);
    figure2width.put("aien_0503.png", 41);
    figure2width.put("aien_0504.png", 41);
    figure2width.put("aien_0505.png", 41);
    figure2width.put("aien_0506.png", 41);
    figure2width.put("aien_0507.png", 41);
    figure2width.put("aien_0508.png", 41);
    figure2width.put("aien_0509.png", 41);
    figure2width.put("aien_0510.png", 41);
    figure2width.put("aien_0511.png", 41);
    figure2width.put("aien_0512.png", 41);
    figure2width.put("aien_0513.png", 41);
    figure2width.put("aien_0514.png", 41);
    figure2width.put("aien_0515.png", 41);
    figure2width.put("aien_0516.png", 41);
    figure2width.put("aien_0601.png", 41);
    figure2width.put("aien_0602.png", 41);
    figure2width.put("aien_0603.png", 41);
    figure2width.put("aien_0604.png", 41);
    figure2width.put("aien_0605.png", 41);
    figure2width.put("aien_0606.png", 41);
    figure2width.put("aien_0607.png", 41);
    figure2width.put("aien_0608.png", 41);
    figure2width.put("aien_0609.png", 41);
    figure2width.put("aien_0610.png", 41);
    figure2width.put("aien_0611.png", 41);
    figure2width.put("aien_0612.png", 41);
    figure2width.put("aien_0613.png", 41);
    figure2width.put("aien_0614.png", 41);
    figure2width.put("aien_0615.png", 41);
    figure2width.put("aien_0616.png", 41);
    figure2width.put("aien_0701.png", 41);
    figure2width.put("aien_0702.png", 41);
    figure2width.put("aien_0703.png", 41);
    figure2width.put("aien_0704.png", 41);
    figure2width.put("aien_0705.png", 41);
    figure2width.put("aien_0706.png", 41);
    figure2width.put("aien_0707.png", 41);
    figure2width.put("aien_0708.png", 41);
    figure2width.put("aien_0709.png", 41);
    figure2width.put("aien_0710.png", 41);
    figure2width.put("aien_0711.png", 41);
    figure2width.put("aien_0712.png", 41);
    figure2width.put("aien_0713.png", 41);
    figure2width.put("aien_0714.png", 41);
    figure2width.put("aien_0715.png", 41);
    figure2width.put("aien_0716.png", 41);
    figure2width.put("aien_0717.png", 41);
    figure2width.put("aien_0718.png", 41);
    figure2width.put("aien_0719.png", 41);
    figure2width.put("aien_0720.png", 41);
    figure2width.put("aien_0801.png", 41);
    figure2width.put("aien_0802.png", 41);
    figure2width.put("aien_0803.png", 41);
    figure2width.put("aien_0804.png", 41);
    figure2width.put("aien_0805.png", 41);
    figure2width.put("aien_0806.png", 41);
    figure2width.put("aien_0807.png", 41);
    figure2width.put("aien_0901.png", 41);
    figure2width.put("aien_0902.png", 41);
    figure2width.put("aien_0903.png", 41);
    figure2width.put("aien_0904.png", 41);
    figure2width.put("aien_0905.png", 41);
    figure2width.put("aien_0906.png", 41);
    figure2width.put("aien_0907.png", 41);
    figure2width.put("aien_0908.png", 41);
    figure2width.put("aien_0909.png", 41);
    figure2width.put("aien_0910.png", 41);
    figure2width.put("aien_0911.png", 41);
    figure2width.put("aien_0912.png", 41);
    figure2width.put("aien_0913.png", 41);
    figure2width.put("aien_0914.png", 41);
    figure2width.put("aien_0915.png", 41);
    figure2width.put("aien_0916.png", 41);
    figure2width.put("aien_0917.png", 41);
    figure2width.put("aien_0918.png", 41);
    figure2width.put("aien_0919.png", 41);
    figure2width.put("aien_1001.png", 41);
    figure2width.put("aien_1002.png", 41);
    figure2width.put("aien_1003.png", 41);
    figure2width.put("aien_1004.png", 41);
    figure2width.put("aien_1005.png", 41);
    figure2width.put("aien_1006.png", 41);
    figure2width.put("aien_1007.png", 41);
    figure2width.put("aien_1008.png", 41);
    figure2width.put("aien_1009.png", 41);
    figure2width.put("aien_1010.png", 41);
    figure2width.put("aien_1011.png", 41);
    figure2width.put("aien_1012.png", 41);
    figure2width.put("aien_1013.png", 41);
    figure2width.put("aien_1014.png", 41);
    figure2width.put("aien_1015.png", 41);
    figure2width.put("aien_1016.png", 41);
    figure2width.put("aien_1017.png", 41);
    figure2width.put("aien_1018.png", 41);
    figure2width.put("aien_1019.png", 41);
    figure2width.put("aien_1020.png", 41);
    figure2width.put("aien_1021.png", 41);
    document
        .select("img[src]")
        .forEach(
            el -> {
              int i = el.attr("src").lastIndexOf('/');
              if (i > 0) {
                String imageFilename = el.attr("src").substring(i + 1);
                Integer width = figure2width.get(imageFilename);
                if (width != null) {
                  el.attr("style", "width: " + width + "rem");
                }
              }
            });

    // insert the pre back and write to file
    Utils.insertPreBackThenWriteToFile(document, tuple._2(), PATH_HTML);
  }
}
