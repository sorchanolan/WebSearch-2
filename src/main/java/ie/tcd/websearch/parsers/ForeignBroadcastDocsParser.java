package ie.tcd.websearch.parsers;

import ie.tcd.websearch.Indexer;
import ie.tcd.websearch.documents.FinancialTimesDoc;
import ie.tcd.websearch.documents.ForeignBroadcastDoc;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ForeignBroadcastDocsParser extends BaseParser {
  private static String DOCUMENT_ROOT_PATH = "docs/fbis/";
  private List<ForeignBroadcastDoc> foreignBroadcastDocs = new ArrayList<>();

  public ForeignBroadcastDocsParser(Indexer indexer) throws Exception {

    List<Path> files = this.getFiles(DOCUMENT_ROOT_PATH);
    int count = 0;
    for(Path file : files) {
      try {
        List<Element> docs = this.getDocElements(file);
        count += docs.size();
        for (Element doc : docs) {
          ForeignBroadcastDoc fbDoc = new ForeignBroadcastDoc();

          fbDoc.setDocNo(doc.getChildTextTrim("DOCNO"));
          fbDoc.setOriginalId(doc.getChildTextTrim("HT"));
          fbDoc.setAuthor(doc.getChildTextTrim("AU"));
          fbDoc.setDate(doc.getChildTextTrim("DATE1"));
          fbDoc.setHeadline(doc.getChildTextTrim("HT"));
          fbDoc.setByline(doc.getChildTextTrim("H4"));
          fbDoc.setText(doc.getChildTextTrim("TEXT"));

          if (fbDoc.getText() != null) {
            fbDoc.setLength(fbDoc.getText().length());
          }

          indexer.createIndexEntry(fbDoc.convertToLuceneDoc());
//          this.foreignBroadcastDocs.add(fbDoc);
        }

      } catch (IOException | JDOMException e) {
        e.printStackTrace();
      }
      break;
    }

    System.out.println(String.format("%s Processed %d docs", DOCUMENT_ROOT_PATH, count));
  }

  public List<ForeignBroadcastDoc> getDocs() {
    return this.foreignBroadcastDocs;
  }

  public void removeDocs() {
    this.foreignBroadcastDocs.clear();
  }
}
