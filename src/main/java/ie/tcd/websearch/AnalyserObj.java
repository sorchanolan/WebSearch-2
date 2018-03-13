package com.company.sorchanolan;

import lombok.Data;
import org.apache.lucene.analysis.Analyzer;

@Data
public class AnalyserObj {
  private Analyzer analyzer;
  private String name;

  public AnalyserObj(Analyzer analyzer, String name) {
    this.analyzer = analyzer;
    this.name = name;
  }
}
