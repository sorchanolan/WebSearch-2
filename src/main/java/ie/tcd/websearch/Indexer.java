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
  private static String INDEX_PATH = "index-";
  private Analyzer analyzer;
  private Similarity similarity;

  public Indexer(Analyzer analyzer, Similarity similarity) throws Exception {
    this.analyzer = analyzer;
    this.similarity = similarity;
  }

  public void createIndexEntry(Document document) throws Exception {
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    config.setSimilarity(similarity);
    Directory directory = FSDirectory.open(Paths.get(INDEX_PATH));

    final IndexWriter writer = new IndexWriter(directory, config);
    writer.addDocument(document);
    writer.close();
    directory.close();
  }
}
