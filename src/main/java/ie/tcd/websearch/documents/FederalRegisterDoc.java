package ie.tcd.websearch.documents;

import lombok.Data;
import org.apache.lucene.document.*;

@Data
public class FederalRegisterDoc extends BaseDoc {

  private String date;
  private String parent;
  private String publication = "Federal Register";

  public FederalRegisterDoc() {
    this.date = "";
    this.parent = "";
  }

  @Override
  public Document convertToLuceneDoc() {
    Document doc = new Document();
    doc.add(new StringField("doc_number", this.getDocNo(), Field.Store.YES));
    doc.add(new StringField("date", this.getDate(), Field.Store.YES));
    doc.add(new TextField("text", this.getText(), Field.Store.YES));
    doc.add(new TextField("publication", this.getPublication(), Field.Store.YES));
    doc.add(new TextField("meta", this.getParent(), Field.Store.YES));
    doc.add(new IntPoint("length", this.getLength()));

    return doc;
  }
}
