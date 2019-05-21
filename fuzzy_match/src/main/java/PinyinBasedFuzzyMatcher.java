import java.util.*;
import java.util.concurrent.TimeUnit;

import com.github.liblevenshtein.collection.dictionary.SortedDawg;
import com.github.liblevenshtein.transducer.Algorithm;
import com.github.liblevenshtein.transducer.Candidate;
import com.github.liblevenshtein.transducer.ITransducer;
import com.github.liblevenshtein.transducer.factory.TransducerBuilder;
import com.google.common.base.Stopwatch;
import com.hankcs.hanlp.HanLP;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qli <qli@mobvoi.com>
 * @date 2019/5/19
 */
@Slf4j
public class PinyinBasedFuzzyMatcher {
  Map<String, Set<String>> pinyin2Text = new HashMap<>();
  ITransducer<Candidate> transducer;

  public PinyinBasedFuzzyMatcher(String text2PinyinDictPath, int maxDistance) {
    Map<String, String> text2Pinyin = PinyinUtils.loadDict(text2PinyinDictPath);

    // Build pinyin2text.
    for (Map.Entry<String, String> entry : text2Pinyin.entrySet()) {
      pinyin2Text.putIfAbsent(entry.getValue(), new HashSet<>());
      pinyin2Text.get(entry.getValue()).add(entry.getKey());
    }

    // Build levenshtein automata.
    Collection<String> sortedPinyinList = new TreeSet(text2Pinyin.values());
    final SortedDawg dictionary = new SortedDawg(sortedPinyinList);
    this.transducer = new TransducerBuilder()
        .dictionary(dictionary)
        .algorithm(Algorithm.TRANSPOSITION)
        .defaultMaxDistance(maxDistance)
        .includeDistance(true)
        .build();
  }

  public List<Candidate> match(String text) {
    String textPinyin = HanLP.convertToPinyinString(text, PinyinUtils.PINYIN_SEPARATOR, false);
    List<Candidate> candidates = new ArrayList<>();
    for (final Candidate candidate : transducer.transduce(textPinyin)) {
      for (final String candidateText : this.pinyin2Text.get(candidate.term())) {
        candidates.add(new Candidate(candidateText, candidate.distance()));
      }
    }
    return candidates;
  }

  public static void main(String[] args) {
    PinyinBasedFuzzyMatcher matcher = new PinyinBasedFuzzyMatcher(
        "src/main/resources/neighbourhoods.pinyin.dict",
        2);

    Stopwatch stopWatch = Stopwatch.createStarted();
    String testString = "同心家园";
    log.info("Process {}", testString);

    stopWatch.reset().start();
    for (final Candidate candidate : matcher.match(testString)) {
      log.info("{} {} {}", testString, candidate.term(), candidate.distance());
    }
    log.info("Get Similar Pinyin Elapsed {}ms", stopWatch.elapsed(TimeUnit.MILLISECONDS));
  }
}
