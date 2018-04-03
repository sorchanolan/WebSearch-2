package ie.tcd.websearch.documents;

import ie.tcd.websearch.util.VectorFieldUtil;
import lombok.Data;
import org.apache.lucene.document.*;

@Data
public class ForeignBroadcastDoc extends BaseDoc {

  private String originalId;
  private String author;
  private String date;
  private String headline;
  private String byline;
  private String publication = "Foreign Broadcast Information Service";

  public ForeignBroadcastDoc() {
    this.originalId = "";
    this.author = "";
    this.date = "";
    this.headline = "";
    this.byline = "";
  }

  @Override
  public Document convertToLuceneDoc() {
    Document doc = new Document();
    doc.add(new StringField("doc_number", this.getDocNo(), Field.Store.YES));
    doc.add(new StringField("author", this.getAuthor(), Field.Store.YES));
    doc.add(new StringField("date", this.getDate(), Field.Store.YES));
    doc.add(new VectorFieldUtil("headline", this.getHeadline()).getField());
    doc.add(new VectorFieldUtil("byline", this.getByline()).getField());
    doc.add(new VectorFieldUtil("text", this.getText()).getField());
    doc.add(new VectorFieldUtil("publication", this.getPublication()).getField());
    doc.add(new IntPoint("length", this.getLength()));

    return doc;
  }
}
