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
		
		// must have a command line parameter for dynamic or static visualization mode
		String modeParam = null;
		try {
			modeParam = args[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			printUsage();
			System.exit(-1);
		}
		
		// must have 6 command line parameters, first parameter must be -static or -dynamic
		if ( args.length != 6 || ! (modeParam.equals("-static") || modeParam.equals("-dynamic")) ) {
			System.out.println("" + args.length + " " + modeParam);
			printUsage();
			System.exit(-1);
		}
		
		// third command line parameter must be a valid URI (this is just a basic inspection)
		String seedURI = args[2];
		if (!seedURI.substring(0, 7).equalsIgnoreCase("http://") || seedURI.length() < 8) {
			System.out.println("Seed URI must begin with http://");
			System.exit(-1);
		}
		
		// set (invalid) defaults for crawl parameters, then set crawl depth and ceiling to fourth and fifth command line parameters
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
			System.out.println("Crawl depth and URI ceiling must be integer values greater than zero.");
			System.exit(-1);
		}
		
		// second command line parameter is either a sparql endpoint or .gexf filename, depending on crawl mode
		String endpoint = args[1].toLowerCase();
		String outName = args[1].toLowerCase();
		if (modeParam.equals("-static")) {
			if (! (endpoint.startsWith("http://") || endpoint.endsWith("/update/"))) {
				System.out.println("SPARQL endpoint update URU must start with http:// and end with /update/");
				System.exit(-1);
			}
		} else if (modeParam.equals("-dynamic")) {
			if (! outName.endsWith(".gexf")) {
				outName += ".gexf";
			}
		}
		
		// sixth command line parameter indicates whether a sorted or unsorted frontier will be used
		String front = args[5].toLowerCase();
		if (! (front.equals("-sorted") || front.equals("-unsorted")) ) {
			System.out.println("Frontier must either be -sorted or -unsorted");
			System.exit(-1);
		}
		
		// set link follow constraints  (not properly implemented in LDSpider 1.1e rev 304, different modes change nothing)
		Crawler.Mode mode = Crawler.Mode.ABOX_AND_TBOX;
		
		// Assemble relative path for each crawl by date/settings identifier
		Date date = new Date();
		String crawlID = "crawls/" + date.toString().substring(4, 16).replace(" ", "_").replace(":", "_");
		crawlID += "_" + depth + "_" + maxURIs + "_" + front.replace("-", "");
		// File object to make directory for crawls using .mkdirs()
		File crawlFile = new File(crawlID);
		crawlFile.mkdirs();
		
		// LDSpider uses a data structure called a Sink that is fed triples (parsed by NxParser) and streams
		// formatted RDF to an output.  Here the output is either a SPARQL update address or a .gexf file.
		Sink sink = null;
		Callback cbOutstream = null;
		OutputStream outstream = null;
		if (modeParam.equals("-static")) { 
			// initialize callback sink to output crawled triples to a sparql endpoint by update POSTs
			boolean provenance = false;
			String graph = "local:" + crawlID;
			sink = new SinkSparul(endpoint, provenance, graph);
		}
		else if (modeParam.equals("-dynamic")) {
			// initialize callback sink to output crawled triples to a dynamic graph in .gexf format
			outstream = new BufferedOutputStream(new FileOutputStream(crawlID + "/" + outName));
			cbOutstream = new CallbackGEXFOutputStream(outstream);
			sink = new SinkCallback(cbOutstream);
			cbOutstream.startDocument();
		} else {
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
	    
	    // create text file containing crawl seed used
	    OutputStream seedstream = new FileOutputStream(crawlID + "/seed.txt");
	    seedstream.write(seedURI.getBytes());
	    seedstream.flush();
	    seedstream.close();
	    
	    /*
	     *  Define crawl parameters and dependent data structures
	     */
	    
	    // initialize crawler with number of threads
	    int numberOfThreads = 2;
	    Crawler crawler = new Crawler(numberOfThreads);
	    
	    // define frontier and populate with seed
	    Frontier frontier = null;
	    if (front.equals("-unsorted")) { frontier = new BasicFrontier(); }
	    else if (front.equals("-sorted")) { frontier = new RankedFrontier(); }
	    else {
	    	// frontier was not set
	    	System.out.println("crawl frontier was not set properly (last parameter)");
	    	System.out.println(front);
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
	     *  crawl.  finalize outputs and exit when done.
	     */
	    
	    // begin crawl 
	    crawler.evaluateBreadthFirst(frontier, depth, maxURIs, maxPLDs, mode); 
	    
	    // close output files and print final status of crawl
	    cbRedirects.endDocument();
	    logprint.close();
	    if (modeParam.equals("-dynamic")) {
	    	// the hack to properly finish the .gexf file:
	    	((CallbackGEXFOutputStream) cbOutstream).readyToClose();
	    	
	    	System.out.println(cbOutstream.toString());
	    	cbOutstream.endDocument();
	    }
	    System.out.println("");
	    System.out.println("This crawl's identifier is:");
	    System.out.println(crawlID.replaceFirst("crawls/", ""));
	    System.out.println("");
	    if (modeParam.equals("-dynamic")) {
	    	System.out.println("The results have been saved to the file:");
	    	System.out.println(outName);
	    	System.out.println("in the crawls folder of the same directory as this .jar");
	    } else if (modeParam.equals("-static")) {
	    	System.out.println("The results have been stored in the SPARQL endpoint at the URI:");
	    	System.out.println(endpoint.replace("/update/", ""));
	    	System.out.println("under the named graph:");
	    	System.out.println("local:" + crawlID);
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
		System.out.println("java -jar BreadthVis.jar -static [endpoint] [seed] [rounds] [breadth] [frontier]");
		System.out.println("");
		System.out.println("[endpoint] - SPARQL endpoint update URI");
		System.out.println("[seed] - crawl seed URI");
		System.out.println("[rounds] - breadth-first crawl depth");
		System.out.println("[breadth] - URIs per crawl round");
		System.out.println("[frontier] - either -sorted or -unsorted frontier (URIs sorted by inlink degree every round)");
		System.out.println("");
		System.out.println("");
		System.out.println("Dynamic mode where crawl results are saved to a dynamic .gexf graph file:");
		System.out.println("");
		System.out.println("java -jar BreadthVis.jar -dynamic [filename] [seed] [rounds] [breadth] [frontier]");
		System.out.println("");
		System.out.println("[filename] - .gexf output filename");
		System.out.println("[seed] - crawl seed URI");
		System.out.println("[rounds] - breadth-first crawl depth");
		System.out.println("[breadth] - URIs per crawl round");
		System.out.println("[frontier] - either -sorted or -unsorted frontier (URIs sorted by inlink degree every round)");
		System.out.println("");
		System.out.println("");
		System.out.println("A folder will be created in a directory called 'crawls' for each crawl, containing:");
		System.out.println("");
		System.out.println("accessLog.txt - an HTTP error log");
		System.out.println("redirectLog.nx - a log of HTTP redirects (as a list of URI pairs)");
		System.out.println("seed.txt - a text file containing the seed used for the crawl");
		System.out.println("results.gexf - (if -dynamic mode is used) a dynamic graph file of the crawl results");
	}

	
	}