package ie.tcd.websearch.documents;

import ie.tcd.websearch.util.VectorFieldUtil;
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
    doc.add(new VectorFieldUtil("text", this.getText()).getField());
    doc.add(new VectorFieldUtil("publication", this.getPublication()).getField());
    doc.add(new VectorFieldUtil("meta", this.getParent()).getField());
    doc.add(new IntPoint("length", this.getLength()));

    return doc;
  }
}
