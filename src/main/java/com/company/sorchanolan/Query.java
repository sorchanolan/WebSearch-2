package com.company.sorchanolan;

import lombok.Data;

@Data
public class Query {
  private int index;
  private String query;

  public Query(int index, String query) {
    this.index = index;
    this.query = query;
  }
}
