## Semantic Web Crawling Visualization
Kirby Banman, <kdbanman@ualberta.ca>

# Important Bits

###### Examples and visualizations of semantic web crawls using:

- Graph Visualizer: [Gephi 0.8.1 beta 201202141941](http://gephi.org/) using plugin [HttpGraph 1.0.6](https://gephi.org/plugins/http-graph/)
- Semantic Crawler: [LDSpider 1.1e](http://code.google.com/p/ldspider/)

# Overview

###### The initial motivation for this project was to produce [this video](http://www.youtube.com/watch?v=CCBvwWIba3c) and [this other video](http://www.youtube.com/watch?v=w9UKUpyqw_4).

There are many unanswered questions regarding the nature of semantic web crawling, and this is my attempt to sharpen those questions with some pretty visualizations.  Different methods of crawling give rise to very different behaviour, which should be evident in the different images, graphs, and videos.

- Crawl settings are described in the shell scripts.
- Generated graphs show the dereferenced URIs as nodes and the links between URIs as edges.
- The real-time generation of the graph is shown at the linked video for each crawl.
- The crawl settings are adjusted so that the complete activity graph contains less than 1000 nodes.

# Script Procedure

###### Each script in the root:

1. Generates directory of the same name for:
    1. Associated visualisation images and graph files 
    2. Text file containing link to crawl visualization video (if present)
    3. Crawl results (if any)
2. Populates the seed text file with the seed URI(s)
3. Starts the crawler with the options described in the script header

# Human Procedure

1. Gephi/HttpGraph are set up to generate a graph according to HTTP traffic routed through `http://localhost:8088`
2. Script is run, starting the crawler with traffic routed through the proxy
3. When the crawl is done, the resulting graph is altered for appearance and saved in `.svg`, `.gml`, and `.gephi` formats

# Roadmap

###### A sufficient set of seed URIs, LDSpider settings, and Gephi settings could be found so that scripting the 'Human Procedure' could be done by command line or respective APIs:
- For each seed URI and for each link type followed:
- Crawl breadth- and depth- first to obtain similarly sized graphs (current strategy is to aim for less than 1000 nodes)
- Crawl breadth- and depth- first with a uniform set of configurations
- The uniform set of configurations could be 2 separate configs, one for a small graph and one for a larger graph, for each of breadth and depth
- Video visualizations are likely impractical for the uniform set, as the crawl time could become impractically large for some seed/link combinations.  For example, a depth-first crawl from Einstein's dbpedia URI, following only rdf:type links, has a Round 2 queue of ~290 URIs, then a Round 3 queue of ~57000 URIs, and the  maxuri parameter [is only checked at the end of every round](http://code.google.com/p/ldspider/source/browse/trunk/src/com/ontologycentral/ldspider/Crawler.java#358)

###### Especially after above scripting, split programmatic and configuration files from presentation files into separate directories.  In addition to sorting what's already there:
- Include crawler config in plain english in the visualization directories.
- Include a terminal log and the crawl output file.

# License

All scripts, images, and graphs are Copyright 2012 (C) Kirby Banman, <kdbanman@ualberta.ca>.

This content is licensed under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This content is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this content.  If not, see <http://www.gnu.org/licenses>.
