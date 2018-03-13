package com.company.sorchanolan;

import lombok.Data;
import org.apache.lucene.search.similarities.Similarity;

@Data
public class SimilarityObj {
  private Similarity similarity;
  private String name;

  public SimilarityObj(Similarity similarity, String name) {
    this.similarity = similarity;
    this.name = name;
  }
}

