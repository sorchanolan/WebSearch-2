package ie.tcd.websearch;

import ie.tcd.websearch.parsers.*;
import ie.tcd.websearch.queryExpansion.IndexWrapper;
import ie.tcd.websearch.queryExpansion.Rocchio;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
  private static String INDEX_PATH = "index";
  private static String RESULTS_PATH = "results.txt";
  private static String SINGLE_QUERY_RESULTS_PATH = "single_query_results.txt";
  private static String GROUND_TRUTH_PATH = "qrels.assignment2.part1";
//  private static String GROUND_TRUTH_PATH = "qrelstrec8.txt";

  private static final double ALPHA = 1.09;
  private static final double BETA = 0.55;
  private static final double K1 = 1.6;
  private static final double B = 0.75;
  private static final int NUM_FEEDBACK_DOCS = 22;
  private static final int NUM_FEEDBACK_TERMS = 60;

  public static void main(String[] args) throws Exception {
    new Main();
  }

  public Main() throws Exception {
    boolean doIndexing = true;
    if (Files.exists(Paths.get(INDEX_PATH))) {
      System.out.println("Index already exists. Would you like to reindex the files? (y/n)");
      Scanner scanner = new Scanner(System.in);
      String input = scanner.next();
      if (input.equals("n")) {
        doIndexing = false;
      }
    }

    if (doIndexing) {
      Indexer indexer = new Indexer(new EnglishAnalyzer(), new LMDirichletSimilarity(), INDEX_PATH);
      System.out.println("Currently indexing... \nPlease wait approximately 7 minutes.");
      ExecutorService taskExecutor = Executors.newFixedThreadPool(4);
      taskExecutor.execute(new FinancialTimesDocsParser(indexer).parse());
      taskExecutor.execute(new ForeignBroadcastDocsParser(indexer).parse());
      taskExecutor.execute(new LosAngelesTimesDocsParser(indexer).parse());
      taskExecutor.execute(new FederalRegisterDocsParser(indexer).parse());

      taskExecutor.shutdown();
      try {
        taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        indexer.closeIndex();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    TopicParser topicParser = new TopicParser();
    List<Topic> topics = topicParser.parseTopics();

    search(topics);

//    System.out.println("--------- MASTER --------");
//    runTrecEval(GROUND_TRUTH_PATH, RESULTS_PATH);
  }

  private void search(List<Topic> topics) throws Exception {
    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_PATH)));
    PrintWriter writer = new PrintWriter(RESULTS_PATH, "UTF-8");

    IndexWrapper index = new IndexWrapper(INDEX_PATH);

    for (int queryIndex = 1; queryIndex <= topics.size(); queryIndex++) {
      Topic topic = topics.get(queryIndex-1);
      topic.calculateFeatureVector();
      topic.applyStopper();

      Rocchio rocchioFb = new Rocchio(ALPHA, BETA, K1, B);
      topic = rocchioFb.expandQuery(index, topic, NUM_FEEDBACK_DOCS, NUM_FEEDBACK_TERMS);

      TopDocs results = index.runQuery(topic, 1000);
      ScoreDoc[] hits = results.scoreDocs;

//      PrintWriter singleQueryWriter = new PrintWriter(SINGLE_QUERY_RESULTS_PATH, "UTF-8");
      for (int hitIndex = 0; hitIndex < hits.length; hitIndex++) {
        ScoreDoc hit = hits[hitIndex];
        int docIndex = hit.doc;
        int queryId = 400 + queryIndex;
        String docId = reader.document(docIndex).get("doc_number");
        String line = String.format("%d 0 %s %d %f 0 ", queryId, docId, hitIndex, hit.score);
        writer.println(line);
//        singleQueryWriter.println(line);
      }
//      singleQueryWriter.close();
//      runTrecEval(GROUND_TRUTH_PATH, SINGLE_QUERY_RESULTS_PATH);
    }
    System.out.println("Results stored in file 'results.txt'.\n");
    writer.close();
  }

  private void runTrecEval(String groundTruthPath, String resultsPath) throws Exception {
    String[] command = {"./trec_eval/trec_eval", groundTruthPath, resultsPath};
    ProcessBuilder processBuilder = new ProcessBuilder(command);

    Process process = processBuilder.start();
    InputStream is = process.getInputStream();
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);
    String line;

    while ((line = br.readLine()) != null) {
      System.out.println(line);
    }

    System.out.println("---");
    process.waitFor();
  }
}
