import com.github.liblevenshtein.transducer.Candidate;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author qli <qli@mobvoi.com>
 * @date 2019/5/21
 */
@Slf4j
public class PinyinBasedFuzzyMatchTest {

  @Test
  public void testPerformance() {
    PinyinBasedFuzzyMatcher matcher = new PinyinBasedFuzzyMatcher("src/main/resources/neighbourhoods.pinyin.dict", 2);
    Stopwatch stopwatch = Stopwatch.createStarted();
    int numTry = 10000;  // 尝试次数
    int count = 0;
    for (Set<String> texts : matcher.pinyin2Text.values()) {
      for (String text : texts) {
        if (count >= numTry) {
          break;
        }
        for (final Candidate candidate : matcher.match(text)) {
          log.info("{} {} {}", text, candidate.term(), candidate.distance());
        }
        ++count;
      }
    }
    long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
    log.info("Total process time {}ms, average process time per query {}ms", elapsed, (double)elapsed / (double)numTry);
  }

  @Test
  public void testPinyinBasedFuzzyMatch() {
    PinyinBasedFuzzyMatcher matcher = new PinyinBasedFuzzyMatcher("src/test/resources/neighbourhoods.pinyin.dict.small", 1);

    String text = "同心家园";

    Map<String, Integer> text2Distance = matcher.match(text).stream()
        .collect(Collectors.toMap(Candidate::term, Candidate::distance));
    assert (text2Distance.size() == 4);
    assert (text2Distance.get("侗馨家园") == 1);
    assert (text2Distance.get("农辛家园") == 1);
    assert (text2Distance.get("童馨家园") == 0);
    assert (text2Distance.get("同心家园") == 0);
  }
}
