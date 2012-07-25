# Semantic Web Crawling #

**WORK IN PROGRESS:**
- This document is intended for readers already familiar with semantic web principles and technologies who wish to explore the idea of semantic web crawling.  No knowledge of web crawlers is assumed.
- Efficiency and parallelization will not be explored.
- Citations will be of the form [{name}] until the document is finalized and the bibliography can be alphabetized.

## Web Crawlers

A crawler is an automated HTTP client that:

1. Traverses links between documents on the web
2. Collects some kind of data as it crawls
3. (May) prioritize its traversal or data collection

#### 1. Link Traversal ####

- A crawler needs a place to start.  This is done by providing one or more *seed URIs* to be dereferenced by the crawler when it starts.
- A crawler needs a way to continue.  This is done with a *frontier*, a master list of URIs to be crawled, and a *queue*, an immediate list of URIs to be crawled 

The queue is initially populated with the seed URIs, so it will dereference them and extract their content.  The content is parsed and its outlinked URIs are added to the frontier.  When the queue is exhausted, discovered URIs now in the frontier are used to repopulate the queue and work starts again.  This recursive relationship, where the frontier URIs are crawled to reveal more frontier URIs, defines a breadth-first traversal of the web if the process is unaltered by prioritization or filtering of URIs.

The high-level structures of seed, frontier, and queue are implemented very broadly in web crawlers: 
semantic crawlers Slug [{dodds}] and LDSpider, [{isele}] which is implemented in the Semantic Web Search Engine [{hogan}]; 
traditional crawlers including Mercator [{heydon}] and Google's first crawler [{brin}]; 
and historical crawlers from the advent of automated web traversal like RBSE Spider [{eichmann}] and the World Wide Web Worm [{mcbryan}].

#### 2. Data Collection ####

A crawler's purpose is to collect information about the content it's exploring.  This is usually done by storing one of the following for each crawled URI:
- A complete copy of the page content
- An index of terms or media on the page
- A map of the inlinks and outlinks of the page
- A set of provenance or meta data

Once this data is collected, it can be processed, displayed, or queried.

#### 3. Prioritization ####

By using crawl timing, or manipulation of the URI queue or frontier, the crawl can be prioritized or structured.  There are a few reasons one might want to do this:  

1. Politeness
2. Page importance
3. Content focus
4. Topical focus

##### 3.1 Politeness #####

A crawler is necessarily a high traffic client, so they can heavily stress servers.  The following principles are to keep web crawling civil:
- In accordance with Robot Exclusion Principle, crawlers should respect the constraints within a server's *robots.txt* file.  This is a guideline, not a requirement - a URI prefix is not hidden from a crawler if it is forbidden in robots.txt
- In order to avoid hammering a particular server with too many requests, a minumum delay between requests of a particular domain should be implemented.  This applies generally and with the optional minimum delay in robots.txt

##### 3.2 Page importance #####

The size and rate of expansion of the web has rendered it impossible to crawl even a small fraction of it, especially if up-to-date information is required.  Either for search priority or for frequency of crawl, *linking degree* is a common metric for page importance.  Bin and Page implement this in the PageRank algorithm.

> The correctness of equating page importance to link degree is disputed in light of the Semantic Web.

##### 3.3 Content focus #####

One may only be interested in a particular type of content, like PDF, jpeg, or rdf+xml.  There are various approaches to giving crawlers tendencies towards specific content types:

#### Biblography ####

[{brin}] S. Brin, L. Page; The Anatomy of a Large-Scale Hypertextual Web Search Engine; Computer Networks 30 (1-7) (1998) 107–117
[{dodds}] L. Dodds; Slug: A Semantic Web Crawler; http://www.ldodds.com/projects/slug (2006)
[{eichmann}] D. Eichmann; The RBSE Spider - Balancing Effective Search Against Web Load; University of Houston - Clear Lake (1994)
[{heydon}] A. Heydon, M. Najork; Mercator: A Scalable, Extensible Web Crawler; Compaq Systems Research Center, (1999)
[{hogan}] A. Hogan, A. Harth, J. Ubrich, S. Kinsella, A. Polleres, S. Decker; Searching and Browsing Linked Data with SWSE: the Semantic Web Search Engine; Digital Enterprise Research Institute, National University of Ireland, Galway; AIFB, Karlsruhe Institute of Technology, Germany (2011)
[{mcbryan}] O. McBryan; GENVL and WWWW: Tools for Taming the Web; Proceeding s of the First International World Wide Web Conference (1994)
