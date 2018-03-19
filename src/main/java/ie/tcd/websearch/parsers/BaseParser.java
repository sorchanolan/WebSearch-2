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
    fileContent = fileContent.replaceAll("&blank;","␣");
    fileContent = fileContent.replaceAll("&rsquo;","’");
    fileContent = fileContent.replaceAll("&hyph;","‐");
    fileContent = fileContent.replaceAll("&Agrave;","À");
    fileContent = fileContent.replaceAll("&acirc;","â");
    fileContent = fileContent.replaceAll("&aacute;","á");
    fileContent = fileContent.replaceAll("&agrave;","à");
    fileContent = fileContent.replaceAll("&auml;","ä");
    fileContent = fileContent.replaceAll("&atilde;","ã");
    fileContent = fileContent.replaceAll("&Ccedil;","Ç");
    fileContent = fileContent.replaceAll("&cacute;","ć");
    fileContent = fileContent.replaceAll("&ccedil;","ç");
    fileContent = fileContent.replaceAll("&racute;","ŕ");
    fileContent = fileContent.replaceAll("&Egrave;","È");
    fileContent = fileContent.replaceAll("&Eacute;","É");
    fileContent = fileContent.replaceAll("&Euml;","Ë");
    fileContent = fileContent.replaceAll("&eacute;","é");
    fileContent = fileContent.replaceAll("&egrave;","è");
    fileContent = fileContent.replaceAll("&euml;","ë");
    fileContent = fileContent.replaceAll("&Gacute;","G");
    fileContent = fileContent.replaceAll("&Iuml;","Ï");
    fileContent = fileContent.replaceAll("&iacute;","í");
    fileContent = fileContent.replaceAll("&iuml;","ï");
    fileContent = fileContent.replaceAll("&igrave;","ì");
    fileContent = fileContent.replaceAll("&Kuml;","K");
    fileContent = fileContent.replaceAll("&lacute;","ĺ");
    fileContent = fileContent.replaceAll("&nacute;","ń");
    fileContent = fileContent.replaceAll("&ncirc;","n");
    fileContent = fileContent.replaceAll("&oacute;","ó");
    fileContent = fileContent.replaceAll("&ouml;","ö");
    fileContent = fileContent.replaceAll("&ocirc;","ô");
    fileContent = fileContent.replaceAll("&ograve;","ò");
    fileContent = fileContent.replaceAll("&Omacr;","Ō");
    fileContent = fileContent.replaceAll("&Ograve;","Ò");
    fileContent = fileContent.replaceAll("&Ouml;","Ö");
    fileContent = fileContent.replaceAll("&pacute;","p");
    fileContent = fileContent.replaceAll("&sacute;","ś");
    fileContent = fileContent.replaceAll("&Ubreve;","Ŭ");
    fileContent = fileContent.replaceAll("&ubreve;","ŭ");
    fileContent = fileContent.replaceAll("&uuml;","ü");
    fileContent = fileContent.replaceAll("&uacute;","ú");
    fileContent = fileContent.replaceAll("&utilde;","ũ");
    fileContent = fileContent.replaceAll("&ugrave;","ù");
    fileContent = fileContent.replaceAll("&AElig;","Æ");
    fileContent = fileContent.replaceAll("&ntilde;","ñ");
    fileContent = fileContent.replaceAll("&yen;","\u200E¥\u200E");
    fileContent = fileContent.replaceAll("&pound;","£");
    fileContent = fileContent.replaceAll("&deg;","°");
    fileContent = fileContent.replaceAll("&egs;","⪖");
    fileContent = fileContent.replaceAll("&els;","⪕");
    fileContent = fileContent.replaceAll("&sect;","§");
    fileContent = fileContent.replaceAll("&cir;","○");
    fileContent = fileContent.replaceAll("&para;","¶");
    fileContent = fileContent.replaceAll("&reg;","®");
    fileContent = fileContent.replaceAll("&bull;","•");
    fileContent = fileContent.replaceAll("&times;","×");
    fileContent = fileContent.replaceAll("&mu;","μ");
    fileContent = fileContent.replaceAll("&ge;","≥");
    fileContent = fileContent.replaceAll("&cent;","¢");
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
