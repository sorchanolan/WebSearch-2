package ie.tcd.websearch;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;

public class Indexer {
  private static String INDEX_PATH = "index";
  private IndexWriterConfig config;
  private Directory directory;
  private final IndexWriter writer;

  public Indexer(Analyzer analyzer, Similarity similarity) throws Exception {
    config = new IndexWriterConfig(analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    config.setSimilarity(similarity);
    directory = FSDirectory.open(Paths.get(INDEX_PATH));
    writer = new IndexWriter(directory, config);
  }

  public void createIndexEntry(Document document) throws Exception {
    writer.addDocument(document);
  }

  public void closeIndex() throws Exception {
    writer.close();
    directory.close();
  }
}
