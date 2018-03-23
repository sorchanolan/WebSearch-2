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
import java.util.Optional;

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

          fbDoc.setDocNo(Optional.ofNullable(doc.getChildTextTrim("DOCNO")).orElse(""));
          fbDoc.setOriginalId(Optional.ofNullable(doc.getChildTextTrim("HT")).orElse(""));
          fbDoc.setAuthor(Optional.ofNullable(doc.getChildTextTrim("AU")).orElse(""));
          fbDoc.setDate(Optional.ofNullable(doc.getChildTextTrim("DATE1")).orElse(""));
          fbDoc.setHeadline(Optional.ofNullable(doc.getChildTextTrim("HT")).orElse(""));
          fbDoc.setByline(Optional.ofNullable(doc.getChildTextTrim("H4")).orElse(""));
          fbDoc.setText(Optional.ofNullable(doc.getChildTextTrim("TEXT")).orElse(""));

          if (fbDoc.getText() != null) {
            fbDoc.setLength(fbDoc.getText().length());
          }

          indexer.createIndexEntry(fbDoc.convertToLuceneDoc());
//          this.foreignBroadcastDocs.add(fbDoc);
        }

      } catch (IOException | JDOMException e) {
//        e.printStackTrace();
      }
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
