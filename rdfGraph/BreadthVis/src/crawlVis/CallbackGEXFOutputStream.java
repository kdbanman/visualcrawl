package crawlVis;

import java.io.IOException;
import java.io.OutputStream;

import org.semanticweb.yars.nx.parser.Callback;

import it.uniroma1.dis.wiserver.gexf4j.core.Edge;
import it.uniroma1.dis.wiserver.gexf4j.core.EdgeType;
import it.uniroma1.dis.wiserver.gexf4j.core.Gexf;
import it.uniroma1.dis.wiserver.gexf4j.core.Graph;
import it.uniroma1.dis.wiserver.gexf4j.core.Mode;
import it.uniroma1.dis.wiserver.gexf4j.core.Node;
import it.uniroma1.dis.wiserver.gexf4j.core.data.Attribute;
import it.uniroma1.dis.wiserver.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wiserver.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wiserver.gexf4j.core.data.AttributeType;
import it.uniroma1.dis.wiserver.gexf4j.core.dynamic.Spell;
import it.uniroma1.dis.wiserver.gexf4j.core.dynamic.TimeFormat;
import it.uniroma1.dis.wiserver.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wiserver.gexf4j.core.impl.SpellImpl;
import it.uniroma1.dis.wiserver.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wiserver.gexf4j.core.impl.data.AttributeListImpl;

import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class CallbackGEXFOutputStream implements Callback {
	
	final OutputStream _out;
	
	long _cnt;
	long _refTime;
	boolean _close;
	boolean _justStarted = true;
	
	Gexf _gexf = null;
	Graph _graph = null;
	AttributeList _nodeAttrList = null;
	Attribute _attTimesCrawled = null;
	
	Map<String, Node> _nodeMap = null;
	Set<String> _tripleIDs = null;
	
	public CallbackGEXFOutputStream(OutputStream out) {
		this(out, false);
	}
	
	public CallbackGEXFOutputStream(OutputStream out, boolean close) {
		_out = out;
		_close = close;
	}
	
	/*
	 * NOTE:	Within LDSpider, the .startDocument() and .endDocument() methods are called numerous times
	 * 			throughout the crawl.  It may have been their intention to split the crawl results up into
	 * 			many files, but their final products don't reflect this.  Their CallbackRDFXMLOutputStream
	 * 			writes a single document containing numerous headers and footers because of the numerous
	 * 			aforementioned method calls.  Their CallbackNxOutputStream is affected by the same bug, but
	 * 			the .startDocument() and .endDocument() methods were designed to not affect the actual
	 * 			document.
	 * 
	 * 			The GEXF4J library requires the .startDocument() and .endDocument() methods, so 
	 */
	
	@Override
	public void startDocument() {
		// HACK:	the class is initialized with _justStarted as true, so the .reallyStartDocument() method
		//			is only called once
		if (_justStarted){
			this.reallyStartDocument();
		}
		_justStarted = false;
	}
	
	public void reallyStartDocument() {
		
		_refTime = System.currentTimeMillis();
		_cnt = 0;
		_gexf = new GexfImpl();
		
		_gexf.getMetadata()
			.setCreator("LDSpider @ UAlberta ECE")
			.setDescription("Semantic webcrawl results")
			.setLastModified(new Date());
		
		_graph = _gexf.getGraph();
		_graph.setDefaultEdgeType(EdgeType.DIRECTED)
			.setMode(Mode.DYNAMIC)
			.setTimeType(TimeFormat.INTEGER);
		
		_nodeAttrList = new AttributeListImpl(AttributeClass.NODE);
		_graph.getAttributeLists().add(_nodeAttrList);
		_attTimesCrawled = _nodeAttrList.createAttribute(AttributeType.INTEGER, "timesCrawled");
		
		
		_nodeMap = new HashMap<String, Node>();
		_tripleIDs = new HashSet<String>();
		
	}
	
	@Override
	public synchronized void processStatement(org.semanticweb.yars.nx.Node[] nodes) {
		
		int relTime = (int) (System.currentTimeMillis() - _refTime);
		
		if (_gexf == null) throw new IllegalStateException("Must open document before writing statements");
		if (nodes == null) throw new NullPointerException("Nodes must not be null");
		if (nodes.length < 3) throw new IllegalArgumentException("A statement must consist of at least 3 nodes");
		
		String subjID = nodes[0].toN3();
		String predID = nodes[1].toN3();
		String objID = nodes[2].toN3();
		
		Node subjNode = null;
		Node objNode = null;
		
		if (! _nodeMap.containsKey(subjID)) {
			
			subjNode = _graph.createNode(subjID);
			subjNode
				.setLabel(subjID)
				.getAttributeValues()
					.addValue(_attTimesCrawled, "1");
			
			Spell spellSubjNode = new SpellImpl();
			spellSubjNode.setStartValue(relTime);
			subjNode.getSpells().add(spellSubjNode);
			
			_nodeMap.put(subjID, subjNode);
			
		} else {
			
			subjNode = _nodeMap.get(subjID);
			int timesCrawled = Integer.parseInt(subjNode.getAttributeValues().get(0).getValue().toString()) + 1;
			subjNode.getAttributeValues().get(0).setValue("" + timesCrawled);
			
		}
		
		if (! _nodeMap.containsKey(objID)) {
			
			objNode = _graph.createNode(objID);
			objNode
				.setLabel(objID)
				.getAttributeValues()
					.addValue(_attTimesCrawled, "1");
			
			Spell spellObjNode = new SpellImpl();
			spellObjNode.setStartValue(relTime);
			objNode.getSpells().add(spellObjNode);
			
			_nodeMap.put(objID, objNode);
			
		} else {
			
			objNode = _nodeMap.get(objID);
			int timesCrawled = Integer.parseInt(objNode.getAttributeValues().get(0).getValue().toString()) + 1;
			objNode.getAttributeValues().get(0).setValue("" + timesCrawled);
			
		}
		
		if (! _tripleIDs.contains(subjID + predID + objID)) {
			
			try {
				// edge ID is string of entire triple to ensure unique ID
				Edge edge = subjNode.connectTo(subjID + predID + objID, objNode);
				edge
					.setLabel(predID)
					.setEdgeType(EdgeType.DIRECTED);
				Spell spellEdge = new SpellImpl();
				spellEdge.setStartValue(relTime);
				edge.getSpells().add(spellEdge);
			} catch (IllegalArgumentException e) {
				//System.out.println(subjID + predID + objID);
			}
			
		}
		
		_cnt++;
	}
	
	@Override
	public void endDocument() {
		// HACK:	So the .endDocument() doesn't write the output prematurely, _close must be set to true for
		//			the document to be written.
		if (_close) {
			StaxGraphWriter graphWriter = new StaxGraphWriter();
			try {
				graphWriter.writeToStream(_gexf, _out, "UTF-8");
				_out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
	}
	
	public String toString() {
		return _cnt + " tuples in " + (System.currentTimeMillis() - _refTime) + "ms";
	}
	
	public void readyToClose() {
		// hack to *actually* trigger document writing
		_close = true;
	}
}
