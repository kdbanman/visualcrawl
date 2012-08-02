import java.net.URI;

import com.ontologycentral.ldspider.Crawler;
import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.frontier.Frontier;

public class BreadthTest {

  public static void main(String[] args) {

    int numberOfThreads = 2;
    String seedUri = "http://dbpedia.org/resource/Albert_Einstein";

    Crawler crawler = new Crawler(numberOfThreads);
    Frontier frontier = new Frontier();
    frontier.setBlacklist(CrawlerConstants.BLACKLIST);
    frontier.add(new URI(seedUri));

  }

}
  
