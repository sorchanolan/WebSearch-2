package ie.tcd.websearch.queryExpansion;

import ie.tcd.websearch.Topic;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class Rocchio {

  private double alpha;
  private double beta;
  private double k1;
  private double b;

  /**
   * Default parameter values taken from:
   * https://nlp.stanford.edu/IR-book/html/htmledition/the-rocchio71-algorithm-1.html
   */
  public Rocchio() {
    this(1.0, 0.75);
  }

  public Rocchio(double alpha, double beta) {
    this(alpha, beta, 1.2, 0.75);
  }

  public Rocchio(double alpha, double beta, double k1, double b) {
    this.alpha = alpha;
    this.beta = beta;
    this.k1 = k1;
    this.b = b;
  }

  public Topic expandQuery(IndexWrapper index, Topic topic, int fbDocs, int fbTerms) {

    TopDocs hits = index.runQuery(topic, fbDocs);
    TopDocs broadHits = index.runBroadQuery(topic, fbDocs);

    FeatureVector feedbackVec = new FeatureVector();
    ScoreDoc[] docs = hits.scoreDocs;
    ScoreDoc[] broadDocs = broadHits.scoreDocs;

    Set<ScoreDoc> uniqueDocs = Arrays.stream(docs).collect(Collectors.toSet());
    Set<Integer> uniqueDocIds = Arrays.stream(docs).map(d -> d.doc).collect(Collectors.toSet());
    Arrays.stream(broadDocs).filter(d -> !uniqueDocIds.contains(d.doc)).forEach(d -> uniqueDocs.add(d));

    for (ScoreDoc doc: uniqueDocs) {
      // Get the document tokens and add to the doc vector
      FeatureVector docVec = index.getDocVector(doc.doc, null);

      // Compute the BM25 weights and add to the feedbackVector
      feedbackVec = computeBM25Weights(index, docVec, feedbackVec);
    }

    // Multiply the summed term vector by beta / |Dr|
    FeatureVector relDocTermVec = new FeatureVector();
    for (String term : feedbackVec.getFeatures()) {
      relDocTermVec.addTerm(term, feedbackVec.getFeatureWeight(term) * beta / fbDocs);
    }

    // Create a query vector and scale by alpha
    FeatureVector origQueryVec = topic.getFeatureVector();

    FeatureVector weightedQueryVec = new FeatureVector();
    weightedQueryVec = computeBM25Weights(index, origQueryVec, weightedQueryVec);

    FeatureVector queryTermVec = new FeatureVector();
    for (String term : origQueryVec.getFeatures()) {
      queryTermVec.addTerm(term, weightedQueryVec.getFeatureWeight(term) * alpha);
    }

    // Combine query and feedback vectors
    for (String term : queryTermVec.getFeatures()) {
      relDocTermVec.addTerm(term, queryTermVec.getFeatureWeight(term));
    }

    // Get top terms
    relDocTermVec.clip(fbTerms);

    topic.setFeatureVector(relDocTermVec);

    return topic;
  }

  private FeatureVector computeBM25Weights(IndexWrapper index, FeatureVector docVec, FeatureVector summedTermVec) {
    for (String term : docVec.getFeatures()) {
      double docCount = index.docCount();
      double docOccur = index.docFreq(term);
      double avgDocLen = index.docLengthAvg();

      double idf = Math.log( (docCount + 1) / (docOccur + 0.5) ); // following Indri
      double tf = docVec.getFeatureWeight(term);

      double weight = (idf * k1 * tf) / (tf + k1 * (1 - b + b * docVec.getLength() / avgDocLen));

      summedTermVec.addTerm(term, weight);
    }

    return summedTermVec;
  }
}
