package ie.tcd.websearch.documents;

import lombok.Data;
import org.apache.lucene.document.*;

@Data
public class LosAngelesTimesDoc extends BaseDoc {

  private String originalId;
  private String date;
  private String headline;
  private String byline;
  private String graphicCaption;
  private String section;
  private String publication = "Los Angeles Times";

  public LosAngelesTimesDoc() {
    this.originalId = "";
    this.graphicCaption = "";
    this.date = "";
    this.headline = "";
    this.byline = "";
    this.section = "";
  }

  @Override
  public Document convertToLuceneDoc() {
    Document doc = new Document();
    doc.add(new StringField("doc_number", this.getDocNo(), Field.Store.YES));
    doc.add(new StringField("originalId", this.getOriginalId(), Field.Store.YES));
    doc.add(new StringField("date", this.getDate(), Field.Store.YES));
    doc.add(new TextField("headline", this.getHeadline(), Field.Store.YES));
    doc.add(new TextField("byline", this.getByline(), Field.Store.YES));
    doc.add(new TextField("text", (this.getText() + this.getGraphicCaption()), Field.Store.YES)); //concatenate graphicCaption to text for doc indexing
    doc.add(new TextField("publication", this.getPublication(), Field.Store.YES));
    doc.add(new TextField("meta", this.getSection(), Field.Store.YES));
    doc.add(new IntPoint("length", this.getLength()));

    return doc;
  }
}
