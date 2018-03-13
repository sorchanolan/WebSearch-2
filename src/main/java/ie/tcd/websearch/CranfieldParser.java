package com.company.sorchanolan;

import org.apache.lucene.document.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CranfieldParser {
  private File documentFile = new File("cran/cran.all.1400");
  private File queryFile = new File("cran/cran.qry");
  private File relevanceJudgementFile = new File("cran/cranqrel");

  public List<org.apache.lucene.document.Document> parseDocuments() throws Exception {
    Scanner scanner = new Scanner(documentFile);
    List<org.apache.lucene.document.Document> documents = new ArrayList<>();

    String token;
    int index;
    StringBuilder text = new StringBuilder();
    StringBuilder title = new StringBuilder();
    StringBuilder author = new StringBuilder();
    StringBuilder journal = new StringBuilder();
    org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();

    while (scanner.hasNextLine()) {
      token = scanner.next();

      switch (token) {
        case (".I"): {
          index = scanner.nextInt();
          document.add(new IntPoint("index", index));
          break;
        }
        case (".T"): {
          while (scanner.hasNext() && !scanner.hasNext("\\.A(.*)")) {
            title.append(scanner.nextLine()).append(" ");
          }
          document.add(new TextField("title", title.toString(), Field.Store.YES));
          break;
        }
        case (".A"): {
          while (scanner.hasNext() && !scanner.hasNext("\\.B(.*)")) {
            author.append(scanner.nextLine()).append(" ");
          }
          document.add(new StringField("author", author.toString(), Field.Store.YES));
          break;
        }
        case (".B"): {
          while (scanner.hasNext() && !scanner.hasNext("\\.W(.*)")) {
            journal.append(scanner.nextLine()).append(" ");
          }
          document.add(new StringField("journal", journal.toString(), Field.Store.YES));
          break;
        }
        case (".W"): {
          while (scanner.hasNext() && !scanner.hasNext("\\.I(.*)")) {
            text.append(scanner.nextLine()).append(" ");
          }
          document.add(new TextField("text", text.toString(), Field.Store.YES));
          documents.add(document);
          document = new org.apache.lucene.document.Document();
          title = new StringBuilder();
          author = new StringBuilder();
          journal = new StringBuilder();
          text = new StringBuilder();
          break;
        }
      }
    }
    return documents;
  }

  public List<Query> parseQueries() throws Exception {
    Scanner scanner = new Scanner(queryFile);
    List<Query> queries = new ArrayList<>();

    int index = -1;
    String token;
    StringBuilder text = new StringBuilder();

    while (scanner.hasNextLine()) {
      token = scanner.next();
      switch (token) {
        case (".I"): {
          index = scanner.nextInt();
          break;
        }
        case (".W"): {
          while (scanner.hasNext() && !scanner.hasNext("\\.I(.*)")) {
            text.append(scanner.nextLine()).append(" ");
          }
          queries.add(new Query(index, text.toString()));
          text = new StringBuilder();
          break;
        }
      }
    }
    return queries;
  }

  public List<RelevanceJudgement> parseRelevanceJudgements() throws Exception {
    Scanner scanner = new Scanner(relevanceJudgementFile);
    List<RelevanceJudgement> relevanceJudgements = new ArrayList<>();
    while (scanner.hasNextLine()) {
      RelevanceJudgement relevanceJudgement = new RelevanceJudgement(scanner.nextInt() ,scanner.nextInt() , scanner.nextInt());
      relevanceJudgements.add(relevanceJudgement);
    }
    return relevanceJudgements;
  }
}
