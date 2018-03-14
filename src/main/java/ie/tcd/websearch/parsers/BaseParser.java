package ie.tcd.websearch.parsers;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BaseParser {

  public List<Path> getFiles(String documentPath) {
    List<Path> files = new ArrayList<>();

    try {
      List<Path> directories = Files.list(Paths.get(documentPath)).filter(Files::isDirectory).collect(Collectors.toList());
      List<Path> filesInRoot = Files.list(Paths.get(documentPath))
              .filter(Files::isRegularFile)
              .filter(path -> !path.toString().contains("read"))
              .collect(Collectors.toList());
      files.addAll(filesInRoot);
      for (Path directory : directories) {
        List<Path> paths = Files.list(directory).filter(Files::isRegularFile).collect(Collectors.toList());
        files.addAll(paths);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return files;
  }

  private String structureAndCleanXMLFile(String fileContent) {
    fileContent = "<DOCS>" + fileContent + "</DOCS>";
    fileContent = fileContent.replaceAll("P=\\d+","");
    fileContent = fileContent.replaceAll("ID=[A-Z0-9-]+","");
    fileContent = fileContent.replaceAll("&eacute;","é");
    fileContent = fileContent.replaceAll("&egrave;","è");
    fileContent = fileContent.replaceAll("&oacute;","ó");
    fileContent = fileContent.replaceAll("&Omacr;","Ō");
    fileContent = fileContent.replaceAll("&Ubreve;","Ŭ");
    fileContent = fileContent.replaceAll("&ubreve;","ŭ");
    fileContent = fileContent.replaceAll("&AElig;","Æ");
    fileContent = fileContent.replaceAll("&yen;","\u200E¥\u200E");
    fileContent = fileContent.replaceAll("&pound;","£");
    fileContent = fileContent.replaceAll("&deg;","°");
    fileContent = fileContent.replaceAll("&egs;","⪖");
    fileContent = fileContent.replaceAll("&els;","⪕");
    fileContent = fileContent.replaceAll("&percnt;","%");
    fileContent = fileContent.replaceAll("&ohm;","Ω");
    fileContent = fileContent.replaceAll("&ap;","≈");
    fileContent = fileContent.replaceAll("&[a-zA-Z]+gr;"," gr");    // erases some data
    fileContent = fileContent.replaceAll(" ?&(?![a-zA-Z])"," ");    // Removes any & that aren't unescaped

    return fileContent;
  }

  public List<Element> getDocElements(Path file) throws JDOMException, IOException {
//    System.out.println(String.format("file name: %s | Size: %d M", file.toString(), Files.size(file)));
    String fileContent = new String(Files.readAllBytes(file));
    fileContent = this.structureAndCleanXMLFile(fileContent);

    SAXBuilder builder = new SAXBuilder(XMLReaders.NONVALIDATING);
    builder.setExpandEntities(true);
    InputStream stream = new ByteArrayInputStream(fileContent.getBytes("UTF-8"));

    Document document = builder.build(stream);

    Element docsRoot = document.getRootElement();
    return docsRoot.getChildren();
  }
}
