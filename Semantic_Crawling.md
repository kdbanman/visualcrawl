# Semantic Web Crawling #

**WORK IN PROGRESS**

## Web Crawlers ## - because humans are slow

### A crawler is an automated HTTP client that: ###

1. Traverses links between documents at dereferenced URIs
2. Collects some kind of data from crawled URIs
3. May prioritize data collection or link following according to rules

#### 1. Link Traversal ####

- A crawler needs a place to start.  This is done by providing one or more *seed URIs* to be dereferenced by the crawler when it starts.
- A crawler needs a way to continue.  This is done with a *frontier*
