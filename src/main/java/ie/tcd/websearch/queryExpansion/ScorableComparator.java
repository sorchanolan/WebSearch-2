package ie.tcd.websearch.queryExpansion;

import java.util.Comparator;

public class ScorableComparator implements Comparator<Scorable>{
  private boolean decreasing = true;

  public ScorableComparator(boolean decreasing) {
    this.decreasing = decreasing;
  }

  public int compare(Scorable x, Scorable y) {
    double xVal = x.getScore();
    double yVal = y.getScore();

    if(decreasing) {
      return (Double.compare(yVal, xVal));
    } else {
      return (Double.compare(xVal, yVal));
    }
  }
}
