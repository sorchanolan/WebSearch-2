package ie.tcd.websearch.parsers;

import ie.tcd.websearch.Indexer;
import ie.tcd.websearch.documents.ForeignBroadcastDoc;
import org.jdom2.Element;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ForeignBroadcastDocsParser extends BaseParser {
  private static String DOCUMENT_ROOT_PATH = "docs/fbis/";

  public ForeignBroadcastDocsParser(Indexer indexer) {
    this.indexer = indexer;
  }

  public Runnable parse() {
    return () -> {
      List<Path> files = this.getFiles(DOCUMENT_ROOT_PATH);
      int count = 0;
      for (Path file : files) {
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
            fbDoc.setText(Optional.ofNullable(doc.getChildTextTrim("TEXT")).orElse("").replace("\n", " "));

            if (fbDoc.getText() != null) {
              fbDoc.setLength(fbDoc.getText().length());
            }

            indexer.createIndexEntry(fbDoc.convertToLuceneDoc());
          }

        } catch (Exception e) {
           // e.printStackTrace();
        }
      }

      System.out.println(String.format("%s Processed %d docs", DOCUMENT_ROOT_PATH, count));
    };
  }
}
