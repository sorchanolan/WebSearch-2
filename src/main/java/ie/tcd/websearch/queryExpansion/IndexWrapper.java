package ie.tcd.websearch.queryExpansion;

import ie.tcd.websearch.Topic;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

public class IndexWrapper {

  private IndexReader index;
  private IndexSearcher searcher;
  private Similarity similarity;
  private Analyzer analyzer;

  public IndexWrapper(String pathToIndex) {
    try {
      Path path = FileSystems.getDefault().getPath(pathToIndex);
      index = DirectoryReader.open(FSDirectory.open(path));
      searcher = new IndexSearcher(index);
      analyzer = new EnglishAnalyzer();
      similarity = new LMDirichletSimilarity();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public double docCount() {
    try {
      return (double) index.numDocs();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return -1.0;
  }

  public double docFreq(String term) {
    double df = 0;

    try {
      Fields fields = MultiFields.getFields(index);
      for (String field : fields) {
        df += index.docFreq(new Term(field, term));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return df;
  }

  public double docLengthAvg() {
    double avgDocLen = 0;
    try {
      double docCount = index.numDocs();
      avgDocLen = index.getSumTotalTermFreq("text") / docCount;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return avgDocLen;
  }

  public FeatureVector getDocVector(int docID, String field) {

    FeatureVector fv = new FeatureVector();
    try {
      Set<Terms> termsSet = new HashSet<>();

      if (field == null) {
        Fields fields = index.getTermVectors(docID);
        for (String fieldName : fields) {
          Terms terms = fields.terms(fieldName);
          if (terms != null) {
            termsSet.add(terms);
          }
        }
      } else {
        Terms terms = index.getTermVector(docID, field);
        termsSet.add(terms);
      }

      for (Terms terms : termsSet) {
        if (terms != null) {
          TermsEnum termsEnum = terms.iterator();
          while (termsEnum.next() != null) {
            String term = termsEnum.term().utf8ToString();

            if (EnglishAnalyzer.getDefaultStopSet().contains(term)) {
              continue;
            }

            long f = termsEnum.totalTermFreq();
            fv.addTerm(term, f);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return fv;
  }

  public TopDocs runQuery(Topic topic, int count) {
    try {
      String queryString = topic.getQueryString();
      QueryParser parser = new QueryParser("text", analyzer);
      Query query = parser.parse(queryString);
      searcher.setSimilarity(this.similarity);
      return searcher.search(query, count);
    } catch (IOException | ParseException e) {
      e.printStackTrace();
    }

    return null;
  }
}
