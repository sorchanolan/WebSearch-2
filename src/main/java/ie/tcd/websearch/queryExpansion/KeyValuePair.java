package ie.tcd.websearch.queryExpansion;

import lombok.Data;

@Data
public class KeyValuePair implements Scorable {
  private String key;
  private double value;

  public KeyValuePair(String key, double value)  {
    this.key = key;
    this.value = value;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder(value + "\t" + key);
    return b.toString();
  }

  public void setScore(double score) {
    this.value = score;
  }

  public double getScore() {
    return value;
  }
}
