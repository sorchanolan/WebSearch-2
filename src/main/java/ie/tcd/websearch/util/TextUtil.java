package ie.tcd.websearch.util;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

public class TextUtil {

  private Analyzer analyzer;

  public TextUtil() {
    this.analyzer = new EnglishAnalyzer();
  }

  public List<String> tokenizeString(String string) {
    List<String> result = new ArrayList<String>();
    try {
      TokenStream stream  = this.analyzer.tokenStream(null, new StringReader(string));
      stream.reset();
      while (stream.incrementToken()) {
        result.add(stream.getAttribute(CharTermAttribute.class).toString());
      }
      stream.close();
    } catch (IOException e) {
      // not thrown b/c we're using a string reader...
      throw new RuntimeException(e);
    }
    return result;
  }

  public String removeStopWords(String text){
    try {
      CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
      TokenStream tokenStream  = this.analyzer.tokenStream(null, new StringReader(text.trim()));
      tokenStream = new StopFilter(tokenStream, stopWords);
      StringBuilder sb = new StringBuilder();
      CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
      tokenStream.reset();
      while (tokenStream.incrementToken()) {
        String term = charTermAttribute.toString();
        sb.append(term + " ");
      }
      tokenStream.close();
      return sb.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return "";
  }

  public String stemText (String term) {
    PorterStemmer stemmer = new PorterStemmer();
    stemmer.setCurrent(term);
    stemmer.stem();
    return stemmer.getCurrent();
  }

  public Map<String, Integer> getWordFrequency(String s) {
    return this.getWordFrequency(s, new HashMap<>());
  }

  public Map<String, Integer> getWordFrequency(String s, Map<String, Integer> frequencyMap) {
    s = this.stemText(s);
    s = this.removeStopWords(s);
    List<String> words = this.tokenizeString(s);

    for(String word: words) {
      frequencyMap.compute(word, (k, v) -> v == null ? 1 : v + 1);
    }

    return frequencyMap.entrySet().stream()
            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
            .collect(Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }
}
