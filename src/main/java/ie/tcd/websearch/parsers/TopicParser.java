package ie.tcd.websearch.parsers;

import ie.tcd.websearch.Topic;
import org.apache.lucene.document.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TopicParser {
  private static String TOPIC_PATH = "docs/topics.txt";

  public List<Topic> parseTopics() throws Exception {
    Scanner scanner = new Scanner(new File(TOPIC_PATH));
    List<Topic> topics = new ArrayList<>();

    String token;
    int number;
    StringBuilder title = new StringBuilder();
    StringBuilder description = new StringBuilder();
    StringBuilder narrative = new StringBuilder();
    Topic topic = new Topic();

    while (scanner.hasNext()) {
      token = scanner.next();

      switch (token) {
        case ("<num>"): {
          scanner.next();
          number = scanner.nextInt();
          topic.setNumber(number);
          break;
        }
        case ("<title>"): {
          while (scanner.hasNext() && !scanner.hasNext("<desc>(.*)")) {
            title.append(scanner.next()).append(" ");
          }
          topic.setTitle(title.toString());
          break;
        }
        case ("<desc>"): {
          scanner.nextLine();
          while (scanner.hasNext() && !scanner.hasNext("<narr>(.*)")) {
            description.append(scanner.nextLine()).append(" ");
          }
          topic.setDescription(description.toString());
          break;
        }
        case ("<narr>"): {
          scanner.nextLine();
          while (scanner.hasNext() && !scanner.hasNext("</top>(.*)")) {
            narrative.append(scanner.nextLine()).append(" ");
          }
          topic.setNarrative(narrative.toString());
          title = new StringBuilder();
          description = new StringBuilder();
          narrative = new StringBuilder();
          topics.add(topic);
          topic = new Topic();
          break;
        }
      }
    }
    return topics;
  }
}
