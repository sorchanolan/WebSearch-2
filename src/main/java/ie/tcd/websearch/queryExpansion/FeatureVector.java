package ie.tcd.websearch.queryExpansion;

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class FeatureVector  {
  private static EnglishAnalyzer analyzer;
  private Map<String, Double> features;
  private CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
  private double length = 0.0;

  public FeatureVector(String text) {
    analyzer = new EnglishAnalyzer(stopWords);

    features = new HashMap<>();
    List<String> terms = this.analyze(text);

    for (String term : terms) {
      length += 1.0;
      Double val = features.get(term);
      if (val == null) {
        features.put(term, 1.0);
      } else {
        double v = val + 1.0;
        features.put(term, v);
      }
    }
  }

  public FeatureVector() {
    analyzer = new EnglishAnalyzer(stopWords);
    features = new HashMap<>();
  }

  // ACCESSORS

  public Set<String> getFeatures() {
    return features.keySet();
  }

  public double getLength() {
    return length;
  }

  public int getFeatureCount() {
    return features.size();
  }

  public double getFeatureWeight(String feature) {
    Double w = features.get(feature);
    return (w==null) ? 0.0 : w.doubleValue();
  }

  public Iterator<String> iterator() {
    return features.keySet().iterator();
  }

  public boolean contains(Object key) {
    return features.containsKey(key);
  }

  public double getVectorNorm() {
    double norm = 0.0;
    Iterator<String> it = features.keySet().iterator();
    while(it.hasNext()) {
      norm += Math.pow(features.get(it.next()), 2.0);
    }
    return Math.sqrt(norm);
  }

  public void addTerm(String term, double weight) {
    if(EnglishAnalyzer.getDefaultStopSet().contains(term))
      return;

    Double w = features.get(term);
    if(w == null) {
      features.put(term, weight);
    } else {
      double f = w;
      features.put(term, f + weight);
    }
    length += weight;
  }

  public void normalize() {
    Map<String,Double> f = new HashMap<>(features.size());

    double sum = 0.0;

    Iterator<String> it = features.keySet().iterator();
    while(it.hasNext()) {
      String feature = it.next();
      double obs = features.get(feature);
      sum += obs;
    }

    it = features.keySet().iterator();
    while(it.hasNext()) {
      String feature = it.next();
      double obs = features.get(feature);
      f.put(feature, obs/sum);
    }

    features = f;
    length = 1.0;
  }

  public void clip(int k) {
    List<KeyValuePair> kvpList = getOrderedFeatures();

    Iterator<KeyValuePair> it = kvpList.iterator();

    Map<String,Double> newMap = new HashMap<>(k);
    int i=0;
    length = 0;
    while(it.hasNext()) {
      if(i++ >= k)
        break;
      KeyValuePair kvp = it.next();
      length += kvp.getScore();
      newMap.put(kvp.getKey(), kvp.getScore());
    }

    features = newMap;
  }

  // VIEWING

  @Override
  public String toString() {
    return this.toString(features.size());
  }

  private List<KeyValuePair> getOrderedFeatures() {
    List<KeyValuePair> kvpList = new ArrayList<KeyValuePair>(features.size());
    Iterator<String> featureIterator = features.keySet().iterator();
    while(featureIterator.hasNext()) {
      String feature = featureIterator.next();
      double value   = features.get(feature);
      KeyValuePair keyValuePair = new KeyValuePair(feature, value);
      kvpList.add(keyValuePair);
    }
    ScorableComparator comparator = new ScorableComparator(true);
    Collections.sort(kvpList, comparator);

    return kvpList;
  }

  public String toString(int k) {
    DecimalFormat format = new DecimalFormat("#.#########");
    StringBuilder b = new StringBuilder();
    List<KeyValuePair> kvpList = getOrderedFeatures();
    Iterator<KeyValuePair> it = kvpList.iterator();
    int i=0;
    while(it.hasNext() && i++ < k) {
      KeyValuePair pair = it.next();
      b.append(format.format(pair.getScore())).append(" ").append(pair.getKey()).append("\n");
    }

    return b.toString();
  }


  // UTILS
  private List<String> analyze(String text) {
    List<String> result = new LinkedList<String>();
    try {
      TokenStream stream = null;
      stream = analyzer.tokenStream("text", new StringReader(text));

      CharTermAttribute charTermAttribute = stream.addAttribute(CharTermAttribute.class);
      stream.reset();
      while(stream.incrementToken()) {
        String term = charTermAttribute.toString();
        result.add(term);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;
  }
}
