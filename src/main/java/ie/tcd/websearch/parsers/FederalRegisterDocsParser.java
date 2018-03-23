package ie.tcd.websearch.parsers;

import ie.tcd.websearch.Indexer;
import ie.tcd.websearch.documents.FederalRegisterDoc;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FederalRegisterDocsParser extends BaseParser {

  private static String DOCUMENT_ROOT_PATH = "docs/fr94/";
  private List<FederalRegisterDoc> federalRegisterDocs = new ArrayList<>();

  public FederalRegisterDocsParser(Indexer indexer) throws Exception {
    List<Path> files = this.getFiles(DOCUMENT_ROOT_PATH);
    int count = 0;
    for(Path file : files) {
      try {
        List<Element> docs = this.getDocElements(file);
        count += docs.size();
        for (Element doc : docs) {
          FederalRegisterDoc frDoc = new FederalRegisterDoc();

          frDoc.setDocNo(Optional.ofNullable(doc.getChildTextTrim("DOCNO")).orElse(""));
          frDoc.setDate(Optional.ofNullable(doc.getChildTextTrim("DATE")).orElse(""));
          frDoc.setText(Optional.ofNullable(doc.getChildTextTrim("TEXT")).orElse("").replace("\n", " "));
          frDoc.setParent(Optional.ofNullable(doc.getChildTextTrim("PARENT")).orElse(""));

          if (frDoc.getText() != null) {
            frDoc.setLength(frDoc.getText().length());
          }

          indexer.createIndexEntry(frDoc.convertToLuceneDoc());
          //        this.federalRegisterDocs.add(laDoc);
        }

      } catch (IOException | JDOMException e) {
//          e.printStackTrace();
        }
    }

    System.out.println(String.format("%s Processed %d docs", DOCUMENT_ROOT_PATH, count));
  }

  public List<FederalRegisterDoc> getDocs() {
    return this.federalRegisterDocs;
}

  public void removeDocs() {
    this.federalRegisterDocs.clear();
}
}
