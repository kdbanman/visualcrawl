package crawlVis;


import java.net.URI;
import java.net.URISyntaxException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;

import java.util.Date;

import com.ontologycentral.ldspider.Crawler;
import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.frontier.BasicFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.frontier.RankedFrontier;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilter;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterSuffix;
import com.ontologycentral.ldspider.hooks.links.LinkFilter;
import com.ontologycentral.ldspider.hooks.links.LinkFilterDefault;
import com.ontologycentral.ldspider.hooks.sink.Sink;
import com.ontologycentral.ldspider.hooks.sink.SinkCallback;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerLogger;

import org.semanticweb.yars.util.CallbackNxOutputStream;
import org.semanticweb.yars.nx.parser.Callback;


public class BreadthVis {

	/**
	 * @param args
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @inproceedings{ldspider,
		author = { Robert Isele and J\"{u}rgen Umbrich and Chris Bizer and Andreas Harth},
		title = { {LDSpider}: An open-source crawling framework for the Web of Linked Data} ,
		year = { 2010 },
		booktitle = { Proceedings of 9th International Semantic Web Conference (ISWC 2010) Posters and Demos},
		url = { http://iswc2010.semanticweb.org/pdf/495.pdf }
		}
	 */
	public static void main(String[] args) throws URISyntaxException, IOException {
	    
		/*
		 * Ensure command line input is valid
		 */
		String modeOption = null;
		try {
			modeOption = args[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			printUsage();
			System.exit(-1);
		}
		
		if ( args.length != 5 || ! (modeOption.equals("-static") || modeOption.equals("-dynamic")) ) {
			System.out.println("" + args.length + " " + modeOption);
			printUsage();
			System.exit(-1);
		}
		
		String seedURI = args[2];
		if (!seedURI.substring(0, 7).equalsIgnoreCase("http://")) {
			System.out.println("Seed URI must begin with http://");
			System.exit(-1);
		}
		
		// number of crawl rounds
		int depth = -1;
		// ceiling of URIs to crawl (checked per round)
		int maxURIs = -1;
		// max number of URIs crawled per pay-level domain per crawl round
		int maxPLDs = 5;
		try {
			depth = Integer.parseInt(args[3]);
			maxURIs = Integer.parseInt(args[4]);
		} catch (NumberFormatException e) {
			System.out.println("Crawl depth and URI ceiling must be integer values.");
			System.exit(-1);
		}
		
		String endpoint = null;
		String outName = null;
		if (modeOption.equals("-static")) {
			endpoint = args[1].toLowerCase();
			if (! (endpoint.startsWith("http://") || endpoint.endsWith("/update/"))) {
				System.out.println("SPARQL endpoint update URU must start with http:// and end with /update/");
				System.exit(-1);
			}
		} else if (modeOption.equals("-dynamic")) {
			outName = args[1].toLowerCase();
			if (! outName.endsWith(".gexf")) {
				outName += ".gexf";
			}
		}
		//TODO:  include in CLI
		//String front = "UNSORTED";
		String front = "SORTED";
		
		// set link follow constraints  (not properly implemented in LDSpider 1.1e rev 304)
		//Crawler.Mode mode = Crawler.Mode.ABOX_ONLY;
		//Crawler.Mode mode = Crawler.Mode.TBOX_ONLY;
		Crawler.Mode mode = Crawler.Mode.ABOX_AND_TBOX;
		
		// Assemble relative path for each crawl by date/settings identifier
		Date date = new Date();
		String crawlID = "crawls/" + date.toString().substring(4, 16).replace(" ", "_").replace(":", "_");
		crawlID += "_" + depth + "_" + maxURIs;
		// File object to make directory for crawls using .mkdirs()
		File crawlFile = new File(crawlID);
		crawlFile.mkdirs();
		
		Sink sink = null;
		Callback cbOutstream = null;
		OutputStream outstream = null;
		if (modeOption.equals("-static")) { 
			// initialize callback sink to output crawled triples to a sparql endpoint by update POSTs
			boolean provenance = false;
			String graph = "local:" + crawlID;
			sink = new SinkSparul(endpoint, provenance, graph);
		}
		else if (modeOption.equals("-dynamic")) {
			// initialize callback sink to output crawled triples to a dynamic graph in .gexf format
			outstream = new BufferedOutputStream(new FileOutputStream(crawlID + "/" + outName));
			cbOutstream = new CallbackGEXFOutputStream(outstream);
			sink = new SinkCallback(cbOutstream);
			cbOutstream.startDocument();
		} else {
			// sink was not set
			System.out.println("data sink was not set");
			System.exit(0);
		}
	    
		/*
		 *   Initialize and configure log outputs for the crawl
		 */
		
	    // initialize stream for access log file output
	    OutputStream logstream = new FileOutputStream(crawlID + "/accessLog.txt");
	    PrintStream logprint = new PrintStream(new BufferedOutputStream(logstream));
	    
	    // initialize callback sink for redirects.nx file
	    OutputStream redirects = new BufferedOutputStream(new FileOutputStream(crawlID + "/redirectLog.nx"));
	    Callback cbRedirects = new CallbackNxOutputStream(redirects);
	    cbRedirects.startDocument();
	    
	    /*
	     *  Define crawl parameters and dependent data structures
	     */
	    
	    // initialize crawler with number of threads
	    int numberOfThreads = 2;
	    Crawler crawler = new Crawler(numberOfThreads);
	    
	    // define frontier and populate with seed
	    Frontier frontier = null;
	    if (front == "UNSORTED") { frontier = new BasicFrontier(); }
	    else if (front == "SORTED") { frontier = new RankedFrontier(); }
	    else {
	    	// frontier was not set
	    	System.out.println("crawl frontier was not set");
	    	System.exit(0);
	    }
	    frontier.add(new URI(seedURI));
	    
	    // set crawler output sink
	    crawler.setOutputCallback(sink);
	    
	    // set default link filter and blacklist
	    LinkFilter linkFilter = new LinkFilterDefault(frontier);
	    crawler.setLinkFilter(linkFilter);
	    FetchFilter blacklist = new FetchFilterSuffix(CrawlerConstants.BLACKLIST);
	    crawler.setBlacklistFilter(blacklist);
	    
	    // error handler
	    ErrorHandler eh = new ErrorHandlerLogger(logprint, cbRedirects, false);
	    frontier.setErrorHandler(eh);
	    linkFilter.setErrorHandler(eh);
	    crawler.setErrorHandler(eh);
	    
	    /*
	     *  
	     */
	    
	    // begin crawl 
	    crawler.evaluateBreadthFirst(frontier, depth, maxURIs, maxPLDs, mode); 
	    
	    // close output files and print final status of crawl
	    cbRedirects.endDocument();
	    logprint.close();
	    if (modeOption.equals("-dynamic")) {
	    	// the hack to properly finish the .gexf file is in the .toString method ... :(
	    	System.out.println(cbOutstream.toString());
	    	cbOutstream.endDocument();
	    }
	    
	    System.runFinalization();
	    System.exit(0);
	    }
	
	private static void printUsage() {
		System.out.println("");
		System.out.println("~~USAGE:~~");
		System.out.println("");
		System.out.println("");
		System.out.println("Static mode where crawl results are POSTed to a SPARQL endpoint update address:");
		System.out.println("");
		System.out.println("java -jar BreadthVis.jar -static [SPARQL endpoint update URI] [crawl seed URI] [crawl depth] [URIs per crawl round]");
		System.out.println("");
		System.out.println("");
		System.out.println("Dynamic mode where crawl results are saved to a dynamic .gexf graph file:");
		System.out.println("");
		System.out.println("java -jar BreadthVis.jar -dynamic [.gexf output filename] [crawl seed URI] [crawl depth] [URIs per crawl round]");
		System.out.println("");
		System.out.println("");
		System.out.println("A folder will be created in a directory called 'crawls' for each crawl, containing:");
		System.out.println("");
		System.out.println("accessLog.txt - an HTTP error log");
		System.out.println("redirectLog.nx - a log of HTTP redirects (as a list of URI pairs)");
		System.out.println("results.gexf - (if -dynamic mode) a dynamic graph file of the crawl results");
	}

	
	}