package org.pentaho.di.trans.steps.wpsclient.process;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xpath.XPathAPI;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

public class ProcessBuilder {
	private static final String PROCESSES_EXPR = "//*[local-name() = 'Process']";
	private static final String PROCESS_NAME_EXPR = "./*[local-name() = 'Title']";
	private static final String PROCESS_ID_EXPR = "./*[local-name() = 'Identifier']";

	private static final String DESCRIBEPROCESS_POST_URL = "//*[local-name() = 'Operation'][@*[local-name() = 'name']='DescribeProcess']/*[local-name() = 'DCP']/*[local-name() = 'HTTP']/*[local-name() = 'Post']/@*[local-name() = 'href']";
	private static final String DESCRIBEPROCESS_GET_URL = "//*[local-name() = 'Operation'][@*[local-name() = 'name']='DescribeProcess']/*[local-name() = 'DCP']/*[local-name() = 'HTTP']/*[local-name() = 'Get']/@*[local-name() = 'href']";
	private static final String EXECUTE_POST_URL = "//*[local-name() = 'Operation'][@*[local-name() = 'name']='Execute']/*[local-name() = 'DCP']/*[local-name() = 'HTTP']/*[local-name() = 'Post']/@*[local-name() = 'href']";
	private static final String EXECUTE_GET_URL = "//*[local-name() = 'Operation'][@*[local-name() = 'name']='Execute']/*[local-name() = 'DCP']/*[local-name() = 'HTTP']/*[local-name() = 'Get']/@*[local-name() = 'href']";

	private Map<String, String> urls;
	
	public ProcessBuilder(){
		urls = new HashMap<String, String> (4);
	}
	
	public Map<String, String> getUrls() {
		return urls;
	}
	
	public Map<String, String> parseProcesses(String response) throws KettleException{
		Map<String, String> processes = new HashMap<String, String>(); 
		try {
		    DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		    dfactory.setNamespaceAware(false);
		    Document doc = dfactory.newDocumentBuilder().parse(new InputSource(new StringReader(response)));

		    Node n;
		    NodeIterator nodeIt = XPathAPI.selectNodeIterator(doc, PROCESSES_EXPR);
		    while ((n = nodeIt.nextNode())!= null){ 
		    	processes.put(XPathAPI.eval(n, PROCESS_NAME_EXPR).str(), XPathAPI.eval(n, PROCESS_ID_EXPR).str());
		    }	

		    String describeProcessPOSTURL = XPathAPI.eval(doc, DESCRIBEPROCESS_POST_URL).str();
		    String describeProcessGETURL = XPathAPI.eval(doc, DESCRIBEPROCESS_GET_URL).str();
		    String executePOSTURL = XPathAPI.eval(doc, EXECUTE_POST_URL).str();
		    String executeGETURL = XPathAPI.eval(doc, EXECUTE_GET_URL).str();

		    if(!Const.isEmpty(describeProcessPOSTURL))
		    	urls.put("describeProcessPOSTURL", describeProcessPOSTURL);
		    if(!Const.isEmpty(describeProcessGETURL))
		    	urls.put("describeProcessGETURL", describeProcessGETURL);
		    if(!Const.isEmpty(executePOSTURL))
		    	urls.put("executePOSTURL", executePOSTURL);
		    if(!Const.isEmpty(executeGETURL))
		    	urls.put("executeGETURL", executeGETURL);
	    } catch (Exception e) {
			throw new KettleException("Error retrieving processes from WPS response...", e);
		}		
	    return processes;
	}
}
