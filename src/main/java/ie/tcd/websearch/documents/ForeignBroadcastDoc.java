package ie.tcd.websearch.documents;

import lombok.Data;
import org.apache.lucene.document.*;

@Data
public class ForeignBroadcastDoc extends BaseDoc {

  private String originalId;
  private String author;
  private String date;
  private String headline;
  private String byline;
  private String text;
  private String publication = "Foreign Broadcast Information Service";

  @Override
  public Document convertToLuceneDoc() {
    Document doc = new Document();
    doc.add(new StringField("doc_number", this.getDocNo(), Field.Store.YES));
    doc.add(new StringField("author", this.getAuthor(), Field.Store.YES));
    doc.add(new StringField("date", this.getDate(), Field.Store.YES));
    doc.add(new TextField("headline", this.getHeadline(), Field.Store.YES));
    doc.add(new TextField("byline", this.getByline(), Field.Store.YES));
    doc.add(new TextField("text", this.getText(), Field.Store.YES));
    doc.add(new TextField("publication", this.getPublication(), Field.Store.YES));
    doc.add(new IntPoint("length", this.getLength()));

    return doc;
  }
}
