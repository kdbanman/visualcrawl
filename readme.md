Semantic Web Crawling Visualization
===================================


Important Bits
--------------

Examples and visualizations of semantic web crawls using
- Graph Visualizer: Gephi 0.8.1 beta 201202141941
                          using plugin HttpGraph 1.0.6
- Semantic Crawler: LDSpider 1.1e

Overview
--------

There are many unanswered questions regarding the nature of semantic web crawling, and this is my attempt to sharpen those questions with some pretty visualizations.  Different methods of crawling give rise to very different behaviour, which should be evident in the different images, graphs, and videos.

Crawl settings are described in the shell scripts.
Generated graphs show the dereferenced URIs as nodes and the links between URIs as edges.
The real-time generation of the graph is shown at the linked video for each crawl.

Script Procedure
----------------

Each script in the root
1. Generates directory of the same name for
    1. Associated visualisation images and graph files 
    2. Text file containing link to crawl visualization video (if present)
    3. Crawl results (if any)
2. Populates the seed text file with the seed URI(s)
3. Starts the crawler with the options described in the script header

Human Procedure
---------------

1. Gephi/HttpGraph are set up to generate a graph according to HTTP traffic routed through `http://localhost:8088`
2. Script is run, starting the crawler with traffic routed through the proxy
3. When the crawl is done, the resulting graph is altered for appearance and saved in `.svg`, `.gml`, and `.gephi` formats

Roadmap
-------

- A sufficient set of seed URIs, LDSpider settings, and Gephi settings could be found so that scripting the 'Human Procedure' could be done by command line or respective APIs

License
-------

All scripts, images, and graphs are Copyright 2012 Kirby Banman `kdbanman@ualberta.ca`.

This content is licensed under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This content is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this content.  If not, see `http://www.gnu.org/licenses/`.
