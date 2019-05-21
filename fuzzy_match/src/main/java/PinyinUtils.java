import com.hankcs.hanlp.HanLP;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qli <qli@mobvoi.com>
 * @date 2019/5/20
 */
@Slf4j
public class PinyinUtils {
  public static final String PINYIN_SEPARATOR = ",";
  public static void convertTextToPinyinWoToneDict(String inputFile, String outputFile) {
    Map<String, String> text2Pinyin = new HashMap<>();

    // load inputFile and compute pinyin for each line
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile))) {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        line = line.trim();
        line = line.replace("\t", "");
        String pinyin = HanLP.convertToPinyinString(line.trim(), PINYIN_SEPARATOR, false);
        if (pinyin == null) {
          continue;
        }
        text2Pinyin.put(line, pinyin);
      }
    } catch (FileNotFoundException e) {
      log.error("{}", e);
    } catch (IOException e) {
      log.error("{}", e);
    }

    // write text2pinyin dict into $outputFile
    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile))) {
      for (Map.Entry entry : text2Pinyin.entrySet()) {
        String line = entry.getKey() + "\t" + entry.getValue() + "\n";
        if (line.trim().split("\t").length != 2) {
          log.warn("Invalid line format {}", line);
        }
        bufferedWriter.write(line);
      }
    } catch (IOException e) {
      log.error("{}", e);
    }
  }

  public static Map<String, String> loadDict(String inputFile) {
    Map<String, String> text2Pinyin = new HashMap<>();

    try(BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile))) {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        String[] segments = line.trim().split("\t");
        if (segments.length != 2) {
          log.warn("Invalid line format {}", line);
          continue;
        }
        text2Pinyin.put(segments[0], segments[1]);
      }
    } catch (FileNotFoundException e) {
      log.error("{}", e);
    } catch (IOException e) {
      log.error("{}", e);
    }

    return text2Pinyin;
  }

  public static void main(String[] args) {
    PinyinUtils.convertTextToPinyinWoToneDict("src/main/resources/neighbourhoods", "src/main/resources/neighbourhoods.pinyin.dict");
  }
}
