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
  private FeatureVector descriptionFeatureVector;

  private String getQueryStringFromVector(FeatureVector featureVector) {
    StringBuilder queryString = new StringBuilder();

    FeatureVector fv = featureVector;
    fv.normalize();
    for (String term: fv.getFeatures()) {
      queryString.append(" ");
      queryString.append(term + "^" + fv.getFeatureWeight(term));
    }
    return queryString.toString();
  }

  public String getQueryString() {
    return getQueryStringFromVector(this.getFeatureVector());
  }

  public String getDescQueryString() {
    return getQueryStringFromVector(this.getDescriptionFeatureVector());
  }

  public void calculateFeatureVector() {
    this.featureVector = new FeatureVector(this.title);
    this.descriptionFeatureVector  = new FeatureVector(this.description);
  }

  public void applyStopper() {
    FeatureVector temp = new FeatureVector(this.title);
    Iterator<String> it = featureVector.iterator();
    while(it.hasNext()) {
      String feature = it.next();
      if(EnglishAnalyzer.getDefaultStopSet().contains(feature))
        continue;
      temp.addTerm(feature, featureVector.getFeatureWeight(feature));
    }
    this.featureVector = temp;

    temp = new FeatureVector(this.description);
    it = descriptionFeatureVector.iterator();
    while(it.hasNext()) {
      String feature = it.next();
      if(EnglishAnalyzer.getDefaultStopSet().contains(feature))
        continue;
      temp.addTerm(feature, descriptionFeatureVector.getFeatureWeight(feature));
    }
    this.descriptionFeatureVector = temp;
  }
}
