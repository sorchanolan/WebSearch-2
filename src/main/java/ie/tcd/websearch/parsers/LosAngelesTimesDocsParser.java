package ie.tcd.websearch.parsers;

import ie.tcd.websearch.Indexer;
import ie.tcd.websearch.documents.ForeignBroadcastDoc;
import ie.tcd.websearch.documents.LosAngelesTimesDoc;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LosAngelesTimesDocsParser extends BaseParser {
  private static String DOCUMENT_ROOT_PATH = "docs/latimes/";
  private List<LosAngelesTimesDoc> losAngelesTimesDocs = new ArrayList<>();

  public LosAngelesTimesDocsParser(Indexer indexer) throws Exception {
    List<Path> files = this.getFiles(DOCUMENT_ROOT_PATH);
    int count = 0;
    for(Path file : files) {
      try {
        List<Element> docs = this.getDocElements(file);
        count += docs.size();
        for (Element doc : docs) {
          LosAngelesTimesDoc laDoc = new LosAngelesTimesDoc();

          laDoc.setDocNo(Optional.ofNullable(doc.getChildTextTrim("DOCNO")).orElse(""));
          laDoc.setOriginalId(Optional.ofNullable(doc.getChildTextTrim("DOCID")).orElse(""));
          laDoc.setDate(Optional.ofNullable(doc.getChildTextTrim("DATE")).orElse(""));
          laDoc.setHeadline(Optional.ofNullable(doc.getChildTextTrim("HEADLINE")).orElse(""));
          laDoc.setByline(Optional.ofNullable(doc.getChildTextTrim("BYLINE")).orElse(""));
          laDoc.setText(Optional.ofNullable(doc.getChildTextTrim("TEXT")).orElse("").replace("\n", " "));
          laDoc.setGraphicCaption(Optional.ofNullable(doc.getChildTextTrim("GRAPHIC")).orElse(""));
          laDoc.setSection(Optional.ofNullable(doc.getChildTextTrim("SECTION")).orElse(""));

          if (laDoc.getText() != null) {
              laDoc.setLength(laDoc.getText().length());
          }

          indexer.createIndexEntry(laDoc.convertToLuceneDoc());
//        this.losAngelesTimesDocs.add(laDoc);
        }

      } catch (IOException | JDOMException e) {
//          e.printStackTrace();
      }
    }

    System.out.println(String.format("%s Processed %d docs", DOCUMENT_ROOT_PATH, count));
  }

  public List<LosAngelesTimesDoc> getDocs() {
    return this.losAngelesTimesDocs;
  }

  public void removeDocs() {
    this.losAngelesTimesDocs.clear();
  }
}
