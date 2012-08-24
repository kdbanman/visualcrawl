## Semantic Web Crawling Visualization
Kirby Banman, <kdbanman@ualberta.ca>

# Important Bits

Examples and visualizations of semantic web crawls using:

### Version 0.2:
- Graph Visualizer: [Gephi 0.8.1 beta 201202141941](http://gephi.org/) using plugin [SemanticWebImport](https://gephi.org/plugins/semanticwebimport/)
- Semantic Crawler: [LDSpider 1.1e](http://code.google.com/p/ldspider/) revision 304 checked out for extension to .gexf output using the [gexf4j 0.4.0](https://github.com/francesco-ficarola/gexf4j) library
- Triplestore Instance: [4Store 1.1.3](http://4store.org/trac/wiki/Download)

### Version 0.1:
- Graph Visualizer: [Gephi 0.8.1 beta 201202141941](http://gephi.org/) using plugin [HttpGraph 1.0.6](https://gephi.org/plugins/http-graph/)
- Semantic Crawler: [LDSpider 1.1e](http://code.google.com/p/ldspider/)

# Overview

*The initial motivation for this project was to produce [this video](http://www.youtube.com/watch?v=CCBvwWIba3c) and [this other video](http://www.youtube.com/watch?v=w9UKUpyqw_4).*

There are many unanswered questions regarding the nature of semantic web crawling (see [SemCrawl.md](visualcrawl/docs/SemCrawl.md) in docs directory), and this is my attempt to sharpen those questions with some visual depictions of different crawling strategies.

### Version 0.2:

This is an attempt to address the inadequacies of version 0.1 (see 0.1 analysis below). Rather than visualizing a dynamic graph of the HTTP traffic of the crawler, one may visualize the RDF graph that the crawler aggregates as it crawls.  There is a way to visualize just the final results of the crawl, as well as a dynamic graph of the crawl results.
*There was not time enough to implement more advanced crawl strategies, due partially to LDSpider's complexity and unimplemented features (ABox/TBox link constraints, sorted queue).  This problem was exaggerated by my unfamiliarity with java.  However, a breadth-first crawl was implemented and the static and dynamic RDF visualizations were made to work.*

##### Static:

java -jar BreadthVis.jar -static <SPARQL endpoint update URI> <crawl seed URI> <crawl depth> <URIs per crawl round>

The basic idea here is that the crawl results are fed into a triplestore as a unique named graph for each crawl.  To visualize, Gephi's SemanticWebImport plugin can be used to query the triplestore using a CONSTRUCT query for all triples under the specific crawl's named graph.

######TODO: implement CLI, output graph name at end of crawl, describe steps in gephi

##### Dynamic:

java -jar BreadthVis.jar -dynamic <.gexf output filename> <crawl seed URI> <crawl depth> <URIs per crawl round>

######TODO: implement CLI, describe steps in gephi

For reference, each crawl will create a directory of the same name as the named graph used in static mode.  The directory will contain ######TODO: log types and names

### Version 0.1:

This  is a preliminary experiment to see the difference between the classic crawl strategies of depth-first and breadth-first, initially explored in 1994 [{pinkerton}][{de bra}], in the context of the semantic web.  It also represents an exploration of visaulization techniques and tools.

- Crawl settings are described in the shell scripts.
- Generated graphs show the dereferenced URIs as nodes and the links between URIs as edges.
- The real-time generation of the graph is shown at the linked video for each crawl.
- The crawl settings are adjusted so that the complete activity graph contains less than 1000 nodes.

##### Script Procedure

Each script in ver0.1/:

1. Generates directory of the same name for:
    - Associated visualisation images and graph files 
    - Text file containing link to crawl visualization video (if present)
    - Crawl results (if any)
2. Populates the seed text file with the seed URI(s)
3. Starts the crawler with the options described in the script header

##### Human Procedure

1. Gephi/HttpGraph are set up to generate a graph according to HTTP traffic routed through `http://localhost:8088`
2. Script is run, starting the crawler with traffic routed through the proxy
3. When the crawl is done, the resulting graph is altered for appearance and saved in `.svg`, `.gml`, and `.gephi` formats

> Analysis:  The strategies give rise to very different results and behaviour (see pics/videos).
> Using the HTTP traffic to visualize the crawl behaviour was good as an initial experiment, but visualizing the RDF graph as it is aggregated would be much more relevant and would eliminate the inclusion of the `localhost` node.
> A dynamic graph document would be much more suitable for analysis of crawl behaviour than a video.  The graph could be explored by the viewer as they see fit, rather than the naive presentation of the graph as it forms for the author.

# Roadmap

### Version 0.2:

##### Static: Visualize the RDF graph at the end of the crawl for different focused algorithms:
- get LDSpider to output RDF+XML using the API rather than the CLI
- use Gephi's Semantic Web Import to visualize the crawl results (may be able to do semi-dynamic vis by SPARQL queries and round-based named graphs)
- implement a few topically focused algorithms in LDSpider to visualize results of a control topic

##### Dynamic: Visualize the RDF graph as it's crawled, rather than the crawler's HTTP traffic:
- Extend LDSpider to output crawl results as a dynamic graph for visualization - .gexf format seems most applicable
- Revision 304 of LDSpider checked out for extension
- [gexf4j](https://github.com/francesco-ficarola/gexf4j) library may be the best way to author the dynamic graph files for output

### Version 0.1:

##### A sufficient set of seed URIs, LDSpider settings, and Gephi settings could be found so that scripting the 'Human Procedure' could be done by command line or respective APIs:
- For each seed URI and for each link type followed:
- Crawl breadth- and depth- first to obtain similarly sized graphs (current strategy is to aim for less than 1000 nodes)
- Crawl breadth- and depth- first with a uniform set of configurations
- The uniform set of configurations could be 2 separate configs, one for a small graph and one for a larger graph, for each of breadth and depth
- Video visualizations are likely impractical for the uniform set, as the crawl time could become impractically large for some seed/link combinations.  For example, a depth-first crawl from Einstein's dbpedia URI, following only rdf:type links, has a Round 2 queue of ~290 URIs, then a Round 3 queue of ~57000 URIs, and the  maxuri parameter [is only checked at the end of every round](http://code.google.com/p/ldspider/source/browse/trunk/src/com/ontologycentral/ldspider/Crawler.java#358)

##### Especially after above scripting, split programmatic and configuration files from presentation files into separate directories.  In addition to sorting what's already there:
- Include crawler config in plain english in the visualization directories.
- Include a terminal log and the crawl output file.

# License

All scripts, images, and graphs are Copyright 2012 (C) Kirby Banman, <kdbanman@ualberta.ca>.

This content is licensed under the terms of the GNU General Public License as published by the Free Software Foundation, either  3 of the License, or (at your option) any later gc.

This content is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this content.  If not, see <http://www.gnu.org/licenses>.
