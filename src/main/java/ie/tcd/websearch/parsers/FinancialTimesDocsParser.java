package ie.tcd.websearch.parsers;

import ie.tcd.websearch.Indexer;
import ie.tcd.websearch.documents.FinancialTimesDoc;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FinancialTimesDocsParser extends BaseParser {

  private static String DOCUMENT_ROOT_PATH = "docs/ft/";
  private List<FinancialTimesDoc> financialTimesDocs = new ArrayList<>();

  public FinancialTimesDocsParser(Indexer indexer) throws Exception {
    List<Path> files = this.getFiles(DOCUMENT_ROOT_PATH);
    int count = 0;
    for(Path file : files) {
      try {
        List<Element> docs = this.getDocElements(file);
        count += docs.size();
        for (Element doc : docs) {
          FinancialTimesDoc ftDoc = new FinancialTimesDoc();

          ftDoc.setDocNo(Optional.ofNullable(doc.getChildTextTrim("DOCNO")).orElse(""));
          ftDoc.setProfile(Optional.ofNullable(doc.getChildTextTrim("PROFILE")).orElse(""));
          ftDoc.setDate(Optional.ofNullable(doc.getChildTextTrim("DATE")).orElse(""));
          ftDoc.setText(Optional.ofNullable(doc.getChildTextTrim("TEXT")).orElse(""));
          ftDoc.setPublication(Optional.ofNullable(doc.getChildTextTrim("PUB")).orElse(""));
          ftDoc.setPage(Optional.ofNullable(doc.getChildTextTrim("PAGE")).orElse(""));

          Element headline = doc.getChild("HEADLINE");
          if (headline != null) {
            ftDoc.setHeadline(Optional.ofNullable(headline.getTextTrim()).orElse(""));
          }

          if (ftDoc.getText() != null) {
            ftDoc.setLength(ftDoc.getText().length());
          }

          indexer.createIndexEntry(ftDoc.convertToLuceneDoc());
//          this.financialTimesDocs.add(ftDoc);
        }
      } catch (IOException | JDOMException e) {
        e.printStackTrace();
      }
//      break;
//      System.out.println(String.format("%d/%d files indexed", files.indexOf(file), files.size()));
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
