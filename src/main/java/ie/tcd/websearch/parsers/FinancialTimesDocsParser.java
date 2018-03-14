package ie.tcd.websearch.parsers;

import ie.tcd.websearch.documents.FinancialTimesDoc;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FinancialTimesDocsParser extends BaseParser {

  private static String DOCUMENT_ROOT_PATH = "docs/ft/";
  private List<FinancialTimesDoc> financialTimesDocs = new ArrayList<>();

  public FinancialTimesDocsParser() {

    List<Path> files = this.getFiles(DOCUMENT_ROOT_PATH);
    int count = 0;
    for(Path file : files) {
      try {
        List<Element> docs = this.getDocElements(file);
        count += docs.size();
        for (Element doc : docs) {
          FinancialTimesDoc ftDoc = new FinancialTimesDoc();

          ftDoc.setDocNo(doc.getChildTextTrim("DOCNO"));
          ftDoc.setProfile(doc.getChildTextTrim("PROFILE"));
          ftDoc.setDate(doc.getChildTextTrim("DATE"));
          ftDoc.setText(doc.getChildTextTrim("TEXT"));
          ftDoc.setPublication(doc.getChildTextTrim("PUB"));
          ftDoc.setPage(doc.getChildTextTrim("PAGE"));

          Element headline = doc.getChild("HEADLINE");
          if (headline != null) {
            ftDoc.setHeadline(headline.getTextTrim());
          }

          if (ftDoc.getText() != null) {
            ftDoc.setLength(ftDoc.getText().length());
          }

//          this.financialTimesDocs.add(ftDoc);
        }
      } catch (IOException | JDOMException e) {
        e.printStackTrace();
      }
  }

  System.out.println(String.format("%s Processed %d docs", DOCUMENT_ROOT_PATH, count));
  }

  public List<FinancialTimesDoc> getDocs() {
    return this.financialTimesDocs;
  }

  public void removeDocs() {
    this.financialTimesDocs.clear();
  }
}
