package ie.tcd.websearch.documents;

import lombok.Data;
import org.apache.lucene.document.Document;

@Data
public abstract class BaseDoc {

  private String docNo;
  private String text;
  private int length;

  public abstract Document convertToLuceneDoc();
}
