package ie.tcd.websearch;

import ie.tcd.websearch.parsers.FinancialTimesDocsParser;
import ie.tcd.websearch.parsers.ForeignBroadcastDocsParser;
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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
  private String qrelsPath = "QRelsCorrectedforTRECeval.txt";

  public static void main(String[] args) throws Exception {
    new Main();
  }

  public Main() throws Exception {
    FinancialTimesDocsParser ftParser = new FinancialTimesDocsParser();
    ftParser.getDocs();
    ftParser.removeDocs();

    ForeignBroadcastDocsParser fbParser = new ForeignBroadcastDocsParser();
    fbParser.getDocs();
    fbParser.removeDocs();

    CranfieldParser cranfieldParser = new CranfieldParser();
    cranfieldParser.parseRelevanceJudgements();
    List<Document> documents = cranfieldParser.parseDocuments();
    List<Query> queries = cranfieldParser.parseQueries();

    List<AnalyserObj> analyzers = new ArrayList<>();
    analyzers.add(new AnalyserObj(new StandardAnalyzer(), "Standard"));
    analyzers.add(new AnalyserObj(new WhitespaceAnalyzer(), "Whitespace"));
    analyzers.add(new AnalyserObj(new EnglishAnalyzer(), "English"));
    analyzers.add(new AnalyserObj(new StopAnalyzer(), "Stop"));

    List<SimilarityObj> similarities = new ArrayList<>();
    similarities.add(new SimilarityObj(new ClassicSimilarity(), "Classic"));
    similarities.add(new SimilarityObj(new BM25Similarity(), "BM25"));
    similarities.add(new SimilarityObj(new BooleanSimilarity(), "Boolean"));

    List<Results> resultsList = new ArrayList<>();

    for (SimilarityObj similarity : similarities) {
      for (AnalyserObj analyzer : analyzers) {
        Path indexPath = Paths.get(String.format("index-%s-%s", analyzer.getName(), similarity.getName()));
        String resultsPath = String.format("trec-qrels-results-%s-%s.txt", analyzer.getName(), similarity.getName());
        createIndex(documents, analyzer.getAnalyzer(), similarity.getSimilarity(), indexPath);
        search(queries, analyzer.getAnalyzer(), similarity.getSimilarity(), indexPath, resultsPath);

        Results results = new Results();
        results.setAnalyzer(analyzer.getName());
        results.setSimilarity(similarity.getName());
        resultsList.add(runTrecEval(qrelsPath, resultsPath, results, analyzer.getName(), similarity.getName()));
      }
    }

    Results bestResults = getBestResults(resultsList);
    System.out.format("\nSystem with %s Analyser and %s scoring performs the best.\n", bestResults.getAnalyzer(), bestResults.getSimilarity());
    Analyzer bestAnalyser = analyzers.stream()
        .filter(analyserObj -> analyserObj.getName().equals(bestResults.getAnalyzer()))
        .map(AnalyserObj::getAnalyzer)
        .findFirst()
        .get();
    Similarity bestSimilarity = similarities.stream()
        .filter(similarityObj -> similarityObj.getName().equals(bestResults.getSimilarity()))
        .map(SimilarityObj::getSimilarity)
        .findFirst()
        .get();

    runSearchEngine(bestResults, bestAnalyser, bestSimilarity);
  }

  private void createIndex(List<Document> documents, Analyzer analyzer, Similarity similarity, Path indexPath) throws Exception {
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    config.setSimilarity(similarity);
    Directory directory = FSDirectory.open(indexPath);

    final IndexWriter writer = new IndexWriter(directory, config);
    for (Document document : documents) {
      writer.addDocument(document);
    }
    writer.close();
    directory.close();
  }

  private void search(List<Query> queries, Analyzer analyzer, Similarity similarity, Path indexPath, String resultsPath) throws Exception {
    IndexReader reader = DirectoryReader.open(FSDirectory.open(indexPath));
    IndexSearcher searcher = new IndexSearcher(reader);
    searcher.setSimilarity(similarity);

    MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
        new String[] {"text", "title", "author", "journal", "index"},
        analyzer);

    PrintWriter writer = new PrintWriter(resultsPath, "UTF-8");

    for (int queryIndex = 1; queryIndex <= queries.size(); queryIndex++) {
      String currentQuery = queries.get(queryIndex-1).getQuery();
      currentQuery = QueryParser.escape(currentQuery);
      org.apache.lucene.search.Query query = queryParser.parse(currentQuery);
      TopDocs results = searcher.search(query, 1400);
      ScoreDoc[] hits = results.scoreDocs;

      for (int hitIndex = 0; hitIndex < hits.length; hitIndex++) {
        ScoreDoc hit = hits[hitIndex];
        int docIndex = hit.doc + 1;
        writer.format("%d 0 %d %d %f 0 \n", queryIndex, docIndex, hitIndex, hit.score);
      }
    }
    writer.close();
  }

  private List<Document> searchInputQuery(String query, Analyzer analyzer, Similarity similarity, Path indexPath, int numDocs) throws Exception {
    IndexReader reader = DirectoryReader.open(FSDirectory.open(indexPath));
    IndexSearcher searcher = new IndexSearcher(reader);
    searcher.setSimilarity(similarity);

    MultiFieldQueryParser queryParser = new MultiFieldQueryParser(
        new String[] {"text", "title", "author", "journal", "index"},
        analyzer);
    query = QueryParser.escape(query);
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

  private Results runTrecEval(String groundTruthPath, String resultsPath, Results results, String analyser, String similarity) throws Exception {
    String[] command = {"./trec_eval/trec_eval", groundTruthPath, resultsPath};
    ProcessBuilder processBuilder = new ProcessBuilder(command);

    Process process = processBuilder.start();
    InputStream is = process.getInputStream();
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);
    String line;

    System.out.format("\n%s Analyser, %s Similarity\n", analyser, similarity);

    while ((line = br.readLine()) != null) {
      System.out.println(line);
      if (line.startsWith("map")) {
        results.setMap(Double.parseDouble(line.split("\\s+")[2]));
      } else if (line.startsWith("gm_map")) {
        results.setGm_map(Double.parseDouble(line.split("\\s+")[2]));
      } else if (line.startsWith("P_5 ")) {
        results.setP_5(Double.parseDouble(line.split("\\s+")[2]));
      } else if (line.startsWith("P_10 ")) {
        results.setP_10(Double.parseDouble(line.split("\\s+")[2]));
      } else if (line.startsWith("P_15 ")) {
        results.setP_15(Double.parseDouble(line.split("\\s+")[2]));
      } else if (line.startsWith("Rprec")) {
        results.setRPrec(Double.parseDouble(line.split("\\s+")[2]));
      }
    }

    process.waitFor();
    return results;
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
