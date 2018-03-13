package com.company.sorchanolan;

import lombok.Data;

@Data
public class RelevanceJudgement {
  private int queryIndex;
  private int documentIndex;
  private int relevance;

  public RelevanceJudgement(int queryIndex, int documentIndex, int relevance) {
    this.queryIndex = queryIndex;
    this.documentIndex = documentIndex;
    this.relevance = relevance;
  }
}
