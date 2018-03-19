package ie.tcd.websearch.parsers;

import ie.tcd.websearch.documents.ForeignBroadcastDoc;
import ie.tcd.websearch.documents.LosAngelesTimesDoc;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LosAngelesTimesDocsParser extends BaseParser {
  private static String DOCUMENT_ROOT_PATH = "docs/latimes/";
  private List<LosAngelesTimesDoc> losAngelesTimesDocs = new ArrayList<>();

  public LosAngelesTimesDocsParser() {
    List<Path> files = this.getFiles(DOCUMENT_ROOT_PATH);
    int count = 0;
    for(Path file : files) {
      try {
        List<Element> docs = this.getDocElements(file);
        count += docs.size();
        for (Element doc : docs) {
          LosAngelesTimesDoc laDoc = new LosAngelesTimesDoc();

          laDoc.setDocNo(doc.getChildTextTrim("DOCNO"));
          laDoc.setOriginalId(doc.getChildTextTrim("DOCID"));
          laDoc.setDate(doc.getChildTextTrim("DATE"));
          laDoc.setHeadline(doc.getChildTextTrim("HEADLINE"));
          laDoc.setByline(doc.getChildTextTrim("BYLINE"));
          laDoc.setText(doc.getChildTextTrim("TEXT"));
          laDoc.setGraphicCaption(doc.getChildTextTrim("GRAPHIC"));
          laDoc.setSection(doc.getChildTextTrim("SECTION"));

          if (laDoc.getText() != null) {
              laDoc.setLength(laDoc.getText().length());
          }

//        this.losAngelesTimesDocs.add(laDoc);
        }

      } catch (IOException | JDOMException e) {
          e.printStackTrace();
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
