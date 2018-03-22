package ie.tcd.websearch.documents;

import lombok.Data;
import org.apache.lucene.document.*;

@Data
public class FinancialTimesDoc extends BaseDoc {

  private String profile;
  private String date;
  private String headline;
  private String publication;
  private String page;

  public FinancialTimesDoc() {
    this.profile = "";
    this.date = "";
    this.headline = "";
    this.publication = "";
    this.page = "";
  }

  @Override
  public Document convertToLuceneDoc() {
    Document doc = new Document();
    doc.add(new StringField("doc_number", this.getDocNo(), Field.Store.YES));
    doc.add(new StringField("author", this.getProfile(), Field.Store.YES));
    doc.add(new StringField("date", this.getDate(), Field.Store.YES));
    doc.add(new TextField("headline", this.getHeadline(), Field.Store.YES));
    doc.add(new TextField("text", this.getText(), Field.Store.YES));
    doc.add(new TextField("publication", this.getPublication(), Field.Store.YES));
    doc.add(new TextField("meta", this.getPage(), Field.Store.YES));
    doc.add(new IntPoint("length", this.getLength()));

    return doc;
  }
}
