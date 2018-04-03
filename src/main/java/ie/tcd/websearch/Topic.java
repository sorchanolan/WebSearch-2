package ie.tcd.websearch;

import ie.tcd.websearch.queryExpansion.FeatureVector;
import lombok.Data;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import java.util.Iterator;

@Data
public class Topic {
  private int number;
  private String title;
  private String description;
  private String narrative;
  private FeatureVector featureVector;

  public String getQuery() {
    return this.title;
  }

  public String getQueryString() {
    StringBuilder queryString = new StringBuilder();

    FeatureVector fv = this.getFeatureVector();
    fv.normalize();
    for (String term: fv.getFeatures()) {
      queryString.append(" ");
      queryString.append(term + "^" + fv.getFeatureWeight(term));
    }
    return queryString.toString();
  }

  public void calculateFeatureVector() {
    this.featureVector  = new FeatureVector(this.getQuery());
  }

  public void applyStopper() {
    FeatureVector temp = new FeatureVector(this.getQuery());
    Iterator<String> it = featureVector.iterator();
    while(it.hasNext()) {
      String feature = it.next();
      if(EnglishAnalyzer.getDefaultStopSet().contains(feature))
        continue;
      temp.addTerm(feature, featureVector.getFeatureWeight(feature));
    }
    this.featureVector = temp;
  }
}
