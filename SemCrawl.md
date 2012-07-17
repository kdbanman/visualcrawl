# Semantic Web Crawling #

**WORK IN PROGRESS:** this document is intended for readers already familiar with semantic web principles and technologies who wish to explore the idea of semantic web crawling.  Efficiency and parallelization will not be explored.

## Web Crawlers

A crawler is an automated HTTP client that:

1. Traverses links between documents on the web
2. Collects some kind of data as it crawls
3. (May) prioritize its traversal or data collection

#### 1. Link Traversal ####

- A crawler needs a place to start.  This is done by providing one or more *seed URIs* to be dereferenced by the crawler when it starts.
- A crawler needs a way to continue.  This is done with a *frontier*, a master list of URIs to be crawled, and a *queue*, an immediate list of URIs to be crawled 

The queue is initially populated with the seed URIs, so it will dereference them and extract their content.  The content is parsed and its outlinked URIs are added to the frontier.  When the queue is exhausted, discovered URIs now in the frontier are used to repopulate the queue and work starts again.  This recursive relationship, where the frontier URIs are crawled to reveal more frontier URIs, defines a breadth-first traversal of the web if the process is unaltered by prioritization or filtering of URIs.

The high-level structure of seed, frontier, and queue is implemented in semantic and traditional crawlers.  {examples and references.  slug, ldspider, mercator, google's, historical ones}

#### 2. Data Collection ####

A crawler's purpose is to collect information about the content it's exploring.  This is usually done by storing one of the following for each crawled URI:
- A complete copy of the page content
- An index of terms or media on the page
- A map of the inlinks and outlinks of the page

Once this data is collected, it can be processed, displayed, or queried.

#### 3. Prioritization ####

There are a few reasons a crawler's behaviour can be prioritized or structured:  

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

