/**
 * 
 */
package org.pentaho.di.trans.steps.cswinput;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jdom.Element;
import org.pentaho.di.core.exception.KettleException;

/**
 * @author Ouattara Mamadou
 *
 */
public class CSWReader {
	
	private URL catalogUrl;
	private String version;
	private String method;
	private String loginServiceUrl;
	private String username;
	private String password;
	private boolean simpleSearch;
	private String keyword;
	private String title;
	private String startDate;
	private String endDate;
	private HashMap<String,Double> BBOX;
	private String elementSet;
	private String outputSchema;
	private ArrayList<Element> parseResult;
	
	
	/**
	 * @param catalogUrl
	 * @param version
	 * @param method
	 */
	public CSWReader(URL catalogUrl, String version, String method) {
		super();
		this.catalogUrl = catalogUrl;
		this.version = version;
		this.method = method;
	}
	
	public CSWReader() {
		// TODO Auto-generated constructor stub
		BBOX=new HashMap<String, Double>();
		BBOX.put("NORTH", new Double(90));
		BBOX.put("SOUTH", new Double(-90));
		BBOX.put("EAST", new Double(180));
		BBOX.put("WEST", new Double(180));
	}

	/**
	 * this method build GET method based query
	 * */
	private String buildGetCapabilitiesGETQuery(){
		String query=catalogUrl.toString();
		query += "?";
		query += "service=CSW";
		query += "&request=GetCapabilities";
		query += "&version="+this.version;		
		
		return query;
	}
	
	/**
	 * build GetRecords query based on http GET metho
	 * */
	private String buildGetRecordsGETQuery(){
		String query=catalogUrl.toString();
		query += "?";
		query += "service=CSW";
		query += "&request=GetRecords";
		query +="&typeNames=csw:Record";
		query +="&constraintLanguage=CQL_TEXT";
		query +="&resultType=results";
		query +="&elementSetName="+this.elementSet;
		query += "&version="+this.version;
		
		return query;
	}
	
	/**
	 * Getrecords method
	 * @throws KettleException 
	 * */
	private String GetRecords() throws KettleException{
		return CSWGET(buildGetRecordsGETQuery());
	}
	
	/**
	 * GetCapabilities method
	 * */
	public String GetCapabilities()throws KettleException {
		return CSWGET(buildGetCapabilitiesGETQuery());
	}
	
private ArrayList<Element> findElement(Element element, String elementName)throws ServletException, IOException{		
		
		parseResult = new ArrayList<Element>();
		
		// Traverse the tree
		recurse_findElement(element.getChildren(), elementName); 
		
		return parseResult;
	}
		
	private void recurse_findElement(List<?> elements, String elementName)throws IOException{			
		// Cycle through all the child nodes of the root
		Iterator<?> iter = elements.iterator();
		while (iter.hasNext()){			
	        Element el = (Element) iter.next();
	        
	        if (el.getName().equals(elementName)) parseResult.add((Element) el);
	       	       
	        // If the node has children, call this method with the node
	        if (el.getChildren()!=null) recurse_findElement(el.getChildren(), elementName);	        
		}
	}
	
	private String CSWGET(String query) throws KettleException{    	   	
		HttpMethod httpMethod = new GetMethod(query);
		try { 			
			 //Prepare HTTP Get		
    		 HttpClient httpclient = new HttpClient();
    		    		
    		 //Execute request
    		 httpclient.executeMethod(httpMethod);
             
             // the response
             InputStream inputStream = httpMethod.getResponseBodyAsStream();
             StringBuffer bodyBuffer = new StringBuffer();
             int c;
             while ( (c=inputStream.read())!=-1) bodyBuffer.append((char)c);
             inputStream.close();

    		 return bodyBuffer.toString();
		}catch (IOException e) { 
			throw new KettleException("Error connecting to catalog...", e);
		}finally{
			 httpMethod.releaseConnection();
		}
	}
	/**
	 * @throws IOException 
	 * @throws JDOMException 
	 * 
	 * */
	/*public Document CSWGET(String query) throws JDOMException, IOException{
		SAXBuilder builder = new SAXBuilder();
		Document capabilitieXMLDocument= builder.build(buildGetCapabilitiesGETQuery());		
		return capabilitieXMLDocument;
	}*/
	/**
	 * @param catalogUrl the catalogUrl to set
	 */
	public void setCatalogUrl(URL catalogUrl) {
		this.catalogUrl = catalogUrl;
	}
	public void setCatalogUrl(String catalogUrl) throws MalformedURLException {
		this.catalogUrl = new URL(catalogUrl);
	}
	/**
	 * @return the catalogUrl
	 */
	public URL getCatalogUrl() {
		return catalogUrl;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param method the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}
	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}
	/**
	 * @return the loginServiceUrl
	 */
	public String getLoginServiceUrl() {
		return loginServiceUrl;
	}
	/**
	 * @param loginServiceUrl the loginServiceUrl to set
	 */
	public void setLoginServiceUrl(String loginServiceUrl) {
		this.loginServiceUrl = loginServiceUrl;
	}
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the simpleSearch
	 */
	public boolean isSimpleSearch() {
		return simpleSearch;
	}
	/**
	 * @param simpleSearch the simpleSearch to set
	 */
	public void setSimpleSearch(boolean simpleSearch) {
		this.simpleSearch = simpleSearch;
	}
	/**
	 * @return the keyword
	 */
	public String getKeyword() {
		return keyword;
	}
	/**
	 * @param keyword the keyword to set
	 */
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}
	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	/**
	 * @return the endDate
	 */
	public String getEndDate() {
		return endDate;
	}
	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	/**
	 * @return the bBOX
	 */
	public HashMap<String, Double> getBBOX() {
		return BBOX;
	}
	/**
	 * @param bBOX the bBOX to set
	 */
	public void setBBOX(HashMap<String, Double> bBOX) {
		BBOX = bBOX;
	}
	/**
	 * @return the elementSet
	 */
	public String getElementSet() {
		return elementSet;
	}
	/**
	 * @param elementSet the elementSet to set
	 */
	public void setElementSet(String elementSet) {
		this.elementSet = elementSet;
	}
	/**
	 * @return the outputSchema
	 */
	public String getOutputSchema() {
		return outputSchema;
	}
	/**
	 * @param outputSchema the outputSchema to set
	 */
	public void setOutputSchema(String outputSchema) {
		this.outputSchema = outputSchema;
	}
	

}
