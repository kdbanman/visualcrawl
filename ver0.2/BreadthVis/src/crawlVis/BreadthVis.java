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
	 */
	public static void main(String[] args) throws URISyntaxException, IOException {
	    
		/* TODO: configure for command line use.  options for NX, SPARQL, GEXF output; breadth, depth, sequential (?) crawls; sorted and unsorted frontiers
		for (String arg : args) {
			System.out.println(arg);
		}
		System.exit(0);*/
		
		// define crawl seed and crawl extent parameters
	    String seedUri = "http://dbpedia.org/resource/Albert_Einstein";
	    // the following parameters only apply to a breadth-first traversal
	    int depth = 8;
	    int maxURIs = 5;
	    int maxPLDs = 2;
	    // the following parameter only applies to a depth-first traversal
	    int depthMaxURIs = 30;
		
		// output to either sparql endpoint, nquads file, or dynamic gexf file
		//String out = "SPARQL";
		//String out = "NX";
		String out = "GEXF";
		
		String front = "UNSORTED";
		//String front = "SORTED";
		
		String traversal = "BREADTH";
		//String traversal = "DEPTH";
		//String traversal = "SEQUENTIAL";
		
		// set link follow constraints  (i have doubts that this does anything)
		Crawler.Mode mode = Crawler.Mode.ABOX_ONLY;
		//Crawler.Mode mode = Crawler.Mode.TBOX_ONLY;
		//Crawler.Mode mode = Crawler.Mode.ABOX_AND_TBOX;
		
		// Assemble relative path for each crawl
		Date date = new Date();
		String crawlTime = "crawls/" + date.toString().substring(4, 16).replace(" ", "_").replace(":", "_");
		crawlTime += "_" + front + "_" + traversal + "_" + mode.toString();
		// File object to make directory for crawls using .mkdirs()
		File crawlFile = new File(crawlTime);
		crawlFile.mkdirs();
		
		Sink sink = null;
		Callback cbOutstream = null;
		OutputStream outstream = null;
		if (out == "NX") { 
			// initialize callback sink to output crawled triples as NQuads
			outstream = new BufferedOutputStream(new FileOutputStream(crawlTime + "/outtest.nx"));
			cbOutstream = new CallbackNxOutputStream(outstream);
			sink = new SinkCallback(cbOutstream);
			cbOutstream.startDocument();
		}
		else if (out == "SPARQL") { 
			// initialize callback sink to output crawled triples to a sparql endpoint by update POSTs
			String endpoint = "http://192.168.1.133:8000/update/";
			boolean provenance = false;
			String graph = "local:" + crawlTime;
			sink = new SinkSparul(endpoint, provenance, graph);
		}
		else if (out == "GEXF") {
			// initialize callback sink to output crawled triples to a dynamic graph in .gexf format
			outstream = new BufferedOutputStream(new FileOutputStream(crawlTime + "/outtest.gexf"));
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
	    OutputStream logstream = new FileOutputStream(crawlTime + "/logtest.txt");
	    PrintStream logprint = new PrintStream(new BufferedOutputStream(logstream));
	    
	    // initialize callback sink for redirects.nx file
	    OutputStream redirects = new BufferedOutputStream(new FileOutputStream(crawlTime + "/redirectstest.nx"));
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
	    frontier.add(new URI(seedUri));
	    
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
	    
	    // begin crawl according to chosen mode
	    // TODO: get load balanced to be fast.
	    if (traversal == "BREADTH") { crawler.evaluateBreadthFirst(frontier, depth, maxURIs, maxPLDs, mode); }
	    else if (traversal == "DEPTH") { crawler.evaluateLoadBalanced(frontier, depthMaxURIs); }
	    else if (traversal == "SEQUENTIAL") { crawler.evaluateSequential(frontier); }
	    else {
	    	// crawl mode was not set
	    	System.out.println("crawl mode was not set");
	    	System.exit(0);
	    }
	    
	    // close output files and print final status of crawl
	    cbRedirects.endDocument();
	    logprint.close();
	    if (out == "NX" || out == "GEXF") {
	    	System.out.println(cbOutstream.toString());
	    	cbOutstream.endDocument();
	    }
	    
	    System.runFinalization();
	    System.exit(0);
	    }

	
	}