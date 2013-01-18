## Semantic Web Crawling Visualization
### Version 0.1
Kirby Banman, <kdbanman@ualberta.ca>

# Important Bits

Examples and visualizations of semantic web crawls using:

- Graph Visualizer: [Gephi 0.8.1 beta 201202141941](http://gephi.org/) using plugin [HttpGraph 1.0.6](https://gephi.org/plugins/http-graph/)
- Semantic Cawler: [LDSpider 1.1e](http://code.google.com/p/ldspider/)

# Overview

*The initial motivation for this project was to produce [this video](http://www.youtube.com/watch?v=CCBvwWIba3c) and [this other video](http://www.youtube.com/watch?v=w9UKUpyqw_4).*

There are many unanswered questions regarding the nature of semantic web crawling (see SemCrawl.md in docs directory), and this is my attempt to sharpen those questions with some visual depictions of different crawling strategies.

This is a preliminary experiment to see the difference between the classic crawl strategies of depth-first and breadth-first traversals in the context of the semantic web.  It also represents an exploration of visaulization techniques and tools. *Note, the depth-first traversal of LDSpider (called load balanced, is not purely depth-first).*

- Crawl settings are described in the shell scripts.
- Generated graphs show the dereferenced URIs as nodes and the links between URIs as edges.
- The real-time generation of the graph is shown at the linked video for each crawl.
- The crawl settings are adjusted so that the complete activity graph contains less than 1000 nodes.

##### Script Procedure

Each script in trafficGraph/:

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

# License

All software used in this project that is not my own is licensed as open source.

All content authored by me licensed under the terms of the GNU General Public License as published by the Free Software Foundation, either  3 of the License, or (at your option) any later gc.

This content is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License in the docs directory.  If not, see <http://www.gnu.org/licenses>.
