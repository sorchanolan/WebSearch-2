package ie.tcd.websearch;

import ie.tcd.websearch.parsers.*;
import ie.tcd.websearch.util.TextUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.simple.SimpleQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {
  private static String INDEX_PATH = "index";
  private static String RESULTS_PATH = "results.txt";
  private static String SINGLE_QUERY_RESULTS_PATH = "single_query_results.txt";
  private static String GROUND_TRUTH_PATH = "qrels.assignment2.part1";
//  private static String GROUND_TRUTH_PATH = "qrelstrec8.txt";

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
      Indexer indexer = new Indexer(new EnglishAnalyzer(), new BM25Similarity(), INDEX_PATH);
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

    search(topics, new EnglishAnalyzer(), new BM25Similarity());
    runTrecEval(GROUND_TRUTH_PATH, RESULTS_PATH);

//    CranfieldParser cranfieldParser = new CranfieldParser();
//    cranfieldParser.parseRelevanceJudgements();
//    List<Document> documents = cranfieldParser.parseDocuments();
//    List<Query> queries = cranfieldParser.parseQueries();
//
//    List<AnalyserObj> analyzers = new ArrayList<>();
//    analyzers.add(new AnalyserObj(new StandardAnalyzer(), "Standard"));
//    analyzers.add(new AnalyserObj(new WhitespaceAnalyzer(), "Whitespace"));
//    analyzers.add(new AnalyserObj(new EnglishAnalyzer(), "English"));
//    analyzers.add(new AnalyserObj(new StopAnalyzer(), "Stop"));
//
//    List<SimilarityObj> similarities = new ArrayList<>();
//    similarities.add(new SimilarityObj(new ClassicSimilarity(), "Classic"));
//    similarities.add(new SimilarityObj(new BM25Similarity(), "BM25"));
//    similarities.add(new SimilarityObj(new BooleanSimilarity(), "Boolean"));
//
//    List<Results> resultsList = new ArrayList<>();
//
//    for (SimilarityObj similarity : similarities) {
//      for (AnalyserObj analyzer : analyzers) {
//        Path indexPath = Paths.get(String.format("index-%s-%s", analyzer.getName(), similarity.getName()));
//        String resultsPath = String.format("trec-qrels-results-%s-%s.txt", analyzer.getName(), similarity.getName());
//        createIndex(documents, analyzer.getAnalyzer(), similarity.getSimilarity(), indexPath);
//        search(queries, analyzer.getAnalyzer(), similarity.getSimilarity(), indexPath, resultsPath);
//
//        Results results = new Results();
//        results.setAnalyzer(analyzer.getName());
//        results.setSimilarity(similarity.getName());
//        resultsList.add(runTrecEval(qrelsPath, resultsPath, results, analyzer.getName(), similarity.getName()));
//      }
//    }
//
//    Results bestResults = getBestResults(resultsList);
//    System.out.format("\nSystem with %s Analyser and %s scoring performs the best.\n", bestResults.getAnalyzer(), bestResults.getSimilarity());
//    Analyzer bestAnalyser = analyzers.stream()
//        .filter(analyserObj -> analyserObj.getName().equals(bestResults.getAnalyzer()))
//        .map(AnalyserObj::getAnalyzer)
//        .findFirst()
//        .get();
//    Similarity bestSimilarity = similarities.stream()
//        .filter(similarityObj -> similarityObj.getName().equals(bestResults.getSimilarity()))
//        .map(SimilarityObj::getSimilarity)
//        .findFirst()
//        .get();
//
//    runSearchEngine(bestResults, bestAnalyser, bestSimilarity);
  }

  private void search(List<Topic> topics, Analyzer analyzer, Similarity similarity) throws Exception {
    List<String> queries = topics.stream()
            .map(Topic::getQuery)
            .collect(Collectors.toList());

    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_PATH)));
    IndexSearcher searcher = new IndexSearcher(reader);
    searcher.setSimilarity(similarity);

//    MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
//        new String[] {"text", "doc_number", "author", "headline", "originalId", "byline", "meta", "date", "publication", "length"},
//        analyzer);
    SimpleQueryParser queryParser = new SimpleQueryParser(analyzer, "text");

    PrintWriter writer = new PrintWriter(RESULTS_PATH, "UTF-8");

    for (int queryIndex = 1; queryIndex <= queries.size(); queryIndex++) {
      String originalQuery = queries.get(queryIndex-1);
      String currentQuery = queryExpansion(originalQuery, queryParser, searcher, reader);
      Query query = queryParser.parse(currentQuery);
      TopDocs results = searcher.search(query, 1000);
      ScoreDoc[] hits = results.scoreDocs;

      PrintWriter singleQueryWriter = new PrintWriter(SINGLE_QUERY_RESULTS_PATH, "UTF-8");
      for (int hitIndex = 0; hitIndex < hits.length; hitIndex++) {
        ScoreDoc hit = hits[hitIndex];
        int docIndex = hit.doc;
        int queryId = 400 + queryIndex;
        String docId = reader.document(docIndex).get("doc_number");
        String line = String.format("%d 0 %s %d %f 0 ", queryId, docId, hitIndex, hit.score);
        writer.println(line);
        singleQueryWriter.println(line);
      }
      singleQueryWriter.close();
      runTrecEval(GROUND_TRUTH_PATH, SINGLE_QUERY_RESULTS_PATH);
    }
    System.out.println("Results stored in file 'results.txt'.\n");
    writer.close();
  }

  private List<Document> searchInputQuery(String query, Analyzer analyzer, Similarity similarity, Path indexPath, int numDocs) throws Exception {
    IndexReader reader = DirectoryReader.open(FSDirectory.open(indexPath));
    IndexSearcher searcher = new IndexSearcher(reader);
    searcher.setSimilarity(similarity);

    MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
        new String[] {"text", "title", "author", "journal", "index"},
        analyzer);
//    query = QueryParser.escape(query);
    org.apache.lucene.search.Query searchQuery = queryParser.parse(query);
    TopDocs results = searcher.search(searchQuery, numDocs);
    ScoreDoc[] hits = results.scoreDocs;

    List<Document> documents = new ArrayList<>();
    for (int hitIndex = 0; hitIndex < hits.length; hitIndex++) {
      ScoreDoc hit = hits[hitIndex];
      Document document = reader.document(hit.doc);
      documents.add(document);
      System.out.format("%d. %s\n", hitIndex+1, document.get("title"));
    }
    return documents;
  }

  private String queryExpansion(String currentQuery, SimpleQueryParser queryParser, IndexSearcher searcher, IndexReader reader) throws Exception {
    int numTopDocsUsed = 15;
    int numTopWordsUsed = 8;
    int frequencyThreshold = 5;

    Query query = queryParser.parse(currentQuery);
    TopDocs results = searcher.search(query, 1000);
    ScoreDoc[] hits = results.scoreDocs;
    TextUtil textUtil = new TextUtil();
    Map<String, Integer> wordFreqency = new HashMap<>();

    for (int hitIndex = 0; hitIndex < numTopDocsUsed; hitIndex++) {
      ScoreDoc hit = hits[hitIndex];
      String text = reader.document(hit.doc).get("text");// + " " + reader.document(hit.doc).get("headline");
      wordFreqency = textUtil.getWordFrequency(text, wordFreqency);
    }

//    Map<String, Integer> finalWordFreqency = wordFreqency;
//    String newQuery = Arrays.stream(query.toString().replace("text:", "").split(" "))
//        .distinct()
//        .filter(finalWordFreqency::containsKey)
//        .filter(word -> finalWordFreqency.get(word) > frequencyThreshold)
//        .collect(Collectors.joining( " " ));

    return query.toString().replace("text:", "") + " " +
        wordFreqency.entrySet().stream()
            .limit(numTopWordsUsed)
            .filter(d -> !query.toString().contains(d.getKey()))
            .map(Map.Entry::getKey)
            .collect(Collectors.joining(" "));
  }

  private void runTrecEval(String groundTruthPath, String resultsPath) throws Exception {
    String[] command = {"./trec_eval/trec_eval", groundTruthPath, resultsPath};
    ProcessBuilder processBuilder = new ProcessBuilder(command);

    Process process = processBuilder.start();
    InputStream is = process.getInputStream();
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);
    String line;

//    System.out.format("\n%s Analyser, %s Similarity\n", analyser, similarity);

    while ((line = br.readLine()) != null) {
      System.out.println(line);
//      if (line.startsWith("map")) {
//        results.setMap(Double.parseDouble(line.split("\\s+")[2]));
//      } else if (line.startsWith("gm_map")) {
//        results.setGm_map(Double.parseDouble(line.split("\\s+")[2]));
//      } else if (line.startsWith("P_5 ")) {
//        results.setP_5(Double.parseDouble(line.split("\\s+")[2]));
//      } else if (line.startsWith("P_10 ")) {
//        results.setP_10(Double.parseDouble(line.split("\\s+")[2]));
//      } else if (line.startsWith("P_15 ")) {
//        results.setP_15(Double.parseDouble(line.split("\\s+")[2]));
//      } else if (line.startsWith("Rprec")) {
//        results.setRPrec(Double.parseDouble(line.split("\\s+")[2]));
//      }
    }

    process.waitFor();
//    return results;
  }


  private Results getBestResults(List<Results> resultsList) {
    resultsList.stream()
        .max(Comparator.comparing(Results::getMap))
        .get()
        .incrementScore();
    resultsList.stream()
        .max(Comparator.comparing(Results::getGm_map))
        .get()
        .incrementScore();
    resultsList.stream()
        .max(Comparator.comparing(Results::getRPrec))
        .get()
        .incrementScore();
    resultsList.stream()
        .max(Comparator.comparing(Results::getP_5))
        .get()
        .incrementScore();
    resultsList.stream()
        .max(Comparator.comparing(Results::getP_10))
        .get()
        .incrementScore();
    resultsList.stream()
        .max(Comparator.comparing(Results::getP_15))
        .get()
        .incrementScore();
    return resultsList.stream()
        .max(Comparator.comparing(Results::getScore))
        .get();
  }

  private void runSearchEngine(Results bestResults, Analyzer bestAnalyser, Similarity bestSimilarity) throws Exception {
    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    String input;
    int num = -1;

    outerloop:
    while (true) {
      System.out.println("\nEnter search query (type exit to finish).");
      input = stdin.readLine();

      if (input.equals("exit")) {
        break;
      }

      do {
        System.out.println("Enter number of return results required.");
        input = stdin.readLine();
        if (input.matches("-?\\d+(\\.\\d+)?")) {
          num = Integer.parseInt(input);
        } else {
          if (input.equals("exit")) {
            break;
          }
          System.out.println("Please enter a number.");
        }
      } while (!input.matches("-?\\d+(\\.\\d+)?"));

      System.out.format("\nTitles of %d relevant documents:\n", num);
      Path indexPath = Paths.get(String.format("index-%s-%s", bestResults.getAnalyzer(), bestResults.getSimilarity()));
      List<Document> documents = searchInputQuery(input, bestAnalyser, bestSimilarity, indexPath, num);

      do {
        System.out.println("\nEnter document number to return document.");
        input = stdin.readLine();
        if (input.matches("-?\\d+(\\.\\d+)?")) {
          num = Integer.parseInt(input);
        } else {
          if (input.equals("exit")) {
            break outerloop;
          }
          System.out.println("Please enter a number.");
        }
      } while (!input.matches("-?\\d+(\\.\\d+)?"));

      Document document = documents.get(num-1);
      String text = document.get("text").replaceAll("((?:\\w+\\s){10}\\w+)(\\s)", "$1\n");
      System.out.print(text);
    }
  }
}
