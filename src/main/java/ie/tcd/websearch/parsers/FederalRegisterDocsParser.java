package ie.tcd.websearch.parsers;

import ie.tcd.websearch.documents.FederalRegisterDoc;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FederalRegisterDocsParser extends BaseParser {

  private static String DOCUMENT_ROOT_PATH = "docs/fr94/";
  private List<FederalRegisterDoc> federalRegisterDocs = new ArrayList<>();

  public FederalRegisterDocsParser() {
    List<Path> files = this.getFiles(DOCUMENT_ROOT_PATH);
    int count = 0;
    for(Path file : files) {
      try {
        List<Element> docs = this.getDocElements(file);
        count += docs.size();
        for (Element doc : docs) {
          FederalRegisterDoc frDoc = new FederalRegisterDoc();

          frDoc.setDocNo(doc.getChildTextTrim("DOCNO"));
          frDoc.setDate(doc.getChildTextTrim("DATE"));
          frDoc.setText(doc.getChildTextTrim("TEXT"));
          frDoc.setParent(doc.getChildTextTrim("PARENT"));

          if (frDoc.getText() != null) {
            frDoc.setLength(frDoc.getText().length());
          }

          //        this.federalRegisterDocs.add(laDoc);
        }

      } catch (IOException | JDOMException e) {
          e.printStackTrace();
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
