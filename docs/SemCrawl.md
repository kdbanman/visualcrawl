# Semantic Web Crawling #

- This document is intended for readers already familiar with semantic web principles and technologies who wish to explore the idea of semantic web crawling.  No knowledge of web crawlers is assumed.
- Efficiency and parallelization will not be explored.

## Web Crawlers

A crawler is an automated HTTP client that:

1. Traverses links between documents on the web
2. Collects some kind of data as it crawls
3. (May) prioritize its traversal or data collection

#### 1. Link Traversal ####

- A crawler needs a place to start.  This is done by providing one or more *seed URIs* to be dereferenced by the crawler when it starts.
- A crawler needs a way to continue.  This is done with a *frontier*, a master list of URIs to be crawled, and a *queue*, an immediate list of URIs to be crawled.  It is typical for each thread of a crawl process to have its own queue, which is directly populated from the frontier.

For simplicity, imagine a single crawler thread.  The queue is initially populated with the seed URIs, and the crawler will dereference the seeds to extract their content.  The content is parsed and the outlinked URIs are added to the frontier.  When the queue is exhausted, newly discovered URIs now in the frontier are used to repopulate the queue and work starts again.  This recursive relationship, where the frontier URIs are crawled to reveal more frontier URIs, defines a pure breadth-first traversal of the web if the process is unaltered by prioritization or filtering of URIs.

The high-level structures of seed, frontier, and queue are implemented very broadly in web crawlers: 
- Semantic crawlers Slug [3] and LDSpider, [8] the latter of which is implemented in the Semantic Web Search Engine [7]
- Traditional crawlers including Mercator [6] and Google's first crawler [2]
- Historical crawlers from the advent of automated web traversal like RBSE Spider [4] and the World Wide Web Worm [10]

#### 2. Data Collection ####

A crawler's purpose is to collect information about the content it's exploring.  This is usually done by storing one of the following types of data for each crawled URI:
- A complete copy of the page content
- An index of terms or media on the page
- A map of the inlinks and outlinks of the page
- A set of provenance data or metadata

Once this data is collected, it can be processed, displayed, queried, or simply stored.

#### 3. Prioritization ####

By manipulation of the URI queue(s) or frontier, the crawl can be prioritized so that it's traversal behaviour will be different than simply breadth-first.  There are a two main reasons one might want to do this:  

1. Politeness
2. Page importance

##### 3.1 Politeness #####

A crawler can be a very high traffic client, so it can heavily stress servers.  The following principles are to keep web crawling civil:
- In accordance with Robot Exclusion Protocol [11], crawlers should respect the constraints within a server's *robots.txt* file.  This is a guideline, not a requirement - a URI prefix is not hidden from a crawler if it is forbidden in robots.txt (and malicious crawlers often don't respect those constraints)
- In order to avoid hammering a particular server with too many requests, a minumum delay between requests of a particular domain should be implemented.  This is a good general guideline, but there is an optional minimum delay stipulation within the Robot Exclusion Principle

##### 3.2 Page importance #####

Assigning a relative importance to a URI after it's been discovered in order to prioritize a crawl frontier or queue requires processing the data collected (see Section 2. Data Collection).  Prioritizing a crawl by relative page importance is done for several reasons, but all of them share a common root: 
the size and rate of expansion of the web has rendered it impossible to crawl even a small fraction of it, especially if up-to-date information is required.  Upon realizing this, one must consider why they would restrict a crawler's traversal so that a method may be implemented:

###### 3.2.1 Crawl Frequency ######

In order to decide how often URIs should be crawled, Brin and Page of Google prioritize crawl frequency based on the PageRank algorithm. [2]  *Linking degree* is a common metric for page importance, and PageRank uses it to effectively model the behaviour of a user randomly surfing the web by following links and occasionally navigating to fresh URIs.  Linking degree as a measure of URI importance is defended as representing authority of the information its document holds. [9]

The correctness of equating page importance to link degree is disputed in light of the Semantic Web. *This isn't defended in the literature that I've read, only stated. I would speculate that part of the dispute is founded upon the fact that a semantic web document is comprised entirely of links.  Consider a traditional web document and a semantic web document on the same topic, containing the same information in their respective formats.  The former is natural human language with relevancy links strewn throughout, while the latter is data in the form of typed links between links representing entities (data in the form of triples).  A semantic document is a collection of links itself, so the job of a crawler - traversing semantic links (triples) and storing them - becomes a job of writing one large semantic web document.  For more discussion on the difference between semantic web documents and traditional web documents, see Appendix A - Documents Are Getting Fuzzy.*

###### 3.2.2 Content focus ######

One may only be interested in a particular type of content, like PDF, jpeg, or rdf+xml.  There are various approaches to giving crawlers tendencies towards specific content types, but this type of restriction won't be discussed heavily here.  BioCrawler of Batzios, et. al. has an interesting approach, modelling a web crawlers as artificial life agents in the environment of the web. [{batzios}]

###### 3.2.3 Topical focus ######

A crawler's behaviour may also be constrained by attempting to crawl webpages that are only relevant to a specific topic.  The topic of the crawl can be described by things like seed URIs or string queries.
A method of addressing the searchability of an ever-increasing web is "by distributing the crawling process across users, queries, or even client computers." [{menczer}]  A crawler that efficiently aggregates content that is related to a particular query topic is an asset any time the goal of a web traversal can be narrowed to a particular topic or set of topics.

Topical crawlers prioritize queues and/or frontiers by assigning an importance value to each URI and sorting the URI lists accordingly.
The goal is to accurately guess which URIs will lead to content that is most related to the predefined topic before they have been crawled.
The data on which to base these guesses is the data that has been collected or processed from the crawled URIs that led to the discovery of the URI being ranked.  
For instance, if a URI has been discovered on several pages that are related to the topic, it may deserve a high rank within the frontier.  Alternatively, a URI discovered on a page in close proximity to key words related to the topic may deserve a high rank as well.

There are many such approaches, simple and advanced, for assigning rank based on topical relevance. [{dong}][{diligenti}][{menczer}]
The common element between them is that they exploit structure as it pertains to the discovered URIs.
This includes link structure (like PageRank), semistructured page data (like HTML tags), structured data (like Microformats), or structure discovered by post-processing (like Natural Language Processing).
These sources of structure can be individually analyzed and combined to nearly limitless complexity, but the reason for such analysis is to get a heuristic for machine understanding of the meaning (semantics) of content leading to URI discovery so that the content topic of URIs may be guessed.

The algorithms for a topical focus can fit into one of two categories: fixed relevancy and adaptive relevancy. [{diligenti}] 
With fixed relevancy, the criteria for measuring the relevancy of a page to the query topic is defined before the crawl commences and does not change for the duration of the crawl. 
With adaptive relevancy, the results of the crawl affect the criteria for document relevancy.

Regardless of which class of algorithm is used, the only way a piece of software can judge whether or not content will be relevant is by exploiting structure (as described above).
The essence of RDF in the semantic web is to have fully structured content: entities, identified by URIs, linked by predicates, identified by URIs.
This mirrors how humans have encoded data in natural language in the structure of a sentence; with a subject, predicate, and object; where those three parts are identified by URIs that point to data (of the same format) describing the entities they identify.
In light of this, consider topically focused crawling and how completely different this environment is:
- Rather than using natural language processing to identify semantic patterns (such as Haerst patterns [12]), they are already encoded in RDF
- In addition to the scattered link structure of the traditional web in the form of rdfs:seeAlso and owl:sameAs [5]), the semantic web's content can be interlinked by typed links in the form of predicates
- The entities - subjects and objects - are identified in the traditional web with words relevant to human language, whereas those entities are only tied to human language by use of literals in the semantic web as RDF

These represent significant changes to the web environment and, consequentially, the methods used to crawl it.
This gives rise to novel opportunities as well as novel questions, and addressing these is of tantalizingly unknown potential.
The VisualCrawl project demonstrates (by example, at least) that the semantic web crawling problem can be modeled as a large dynamic network, and that the data can be visualized as such.
Leveraging human percecption and intuition for analysis of structure and transition of large dynamic networks can be effective for sharpening our questions, which is why visualization is an important tool. [1]


#### Appendix A - Documents Are Getting Fuzzier ###

*Here I attempt to draw the readers attention to how the nature of web documents (and, hence, the links between them) have evolved with turning points in the web.  This is relevant to crawling, as web documents are effectively the ecosystem within which crawlers exist.*

###### Web 1.0 ######

*Documents* are defined by human authors:

- Data is drawn from arbitrary sources and crafted directly into web content by human authors
- Content is split between documents by human judgement on a per-document basis (as in the chapters and sections of a book)
- Data is interlinked by links between documents

###### Web 2.0 ######

*Document classes* are defined by human authored templates:

- Data is drawn from arbitrary sources and fed into relational databases
- Content is extracted and rendered from relational databases according to the templates (as in XSLT and CSS)
- Data is interlinked by links between document classes

###### Web 3.0 ######

*Document representations* of resources are defined by human authored algorithms:

- Data is drawn from arbitrary sources into triplestores
- Content is extracted from triplestores by algorithmically discovering a relevant subgraph (as in Concise Bounded Description or Minimal Self-Contained Graph)
- Data is interlinked by entity equivalency links (owl:sameAs), relevance links (rdfs:seeAlso), and the data itself (all triples are links)

#### Biblography ####

1. M. Bastian, S. Heymann, M. Jacomy; Gephi: An Open Source Software for Exploring and Manipulating Networks; Association for the Advancement of Artificial Intelligence; gephi.org/publications/gephi-bastian-feb09.pdf (2009)
2. S. Brin, L. Page; The Anatomy of a Large-Scale Hypertextual Web Search Engine; Computer Networks 30 (1-7) (1998) 107–117
3. L. Dodds; Slug: A Semantic Web Crawler; http://www.ldodds.com/projects/slug (2006)
4. D. Eichmann; The RBSE Spider - Balancing Effective Search Against Web Load; University of Houston - Clear Lake (1994)
5. H. Halpin, I. Herman; When owl:sameAs isn't the Same: An Analysis of Identity Links on the Semantic Web; RDF Next Steps Workshop, June 26-27, 2010, Palo Alto; http://www.w3.org/2009/12/rdf-ws/papers/ws21 (2010)
6. A. Heydon, M. Najork; Mercator: A Scalable, Extensible Web Crawler; Compaq Systems Research Center, (1999)
7. A. Hogan, A. Harth, J. Ubrich, S. Kinsella, A. Polleres, S. Decker; Searching and Browsing Linked Data with SWSE: the Semantic Web Search Engine; Digital Enterprise Research Institute, National University of Ireland, Galway; AIFB, Karlsruhe Institute of Technology, Germany (2011)
8. R. Isele, J. Umbrich, C. Bizer, A. Harth; LDSpider: An open-source crawling framework for the Web of Linked Data; AIFB, Karlsruhe Institute of Technology; DERI, National University of Ireland; Freie Universitaet Berlin, Web-based Systems Group; http://iswc2010.semanticweb.org/pdf/495.pdf (2010) 
9. J. Kleinberg; Authoritative Sources in a Hyperlinked Environment; Cornell University; Ithaca, New York (1998)
10. O. McBryan; GENVL and WWWW: Tools for Taming the Web; Proceeding s of the First International World Wide Web Conference (1994)
11. http://www.robotstxt.org/robotstxt.html 
12. D. Jurafsky, A. Ng, R. Snow; Learning syntactic patterns for automatic hypernym discovery; Advances in Neural Information Processing Systems, vol 17, MIT Press; Cambridge, MA (2005)
