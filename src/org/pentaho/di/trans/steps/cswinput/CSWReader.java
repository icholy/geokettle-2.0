/**
 * 
 */
package org.pentaho.di.trans.steps.cswinput;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
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
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.pentaho.di.core.exception.KettleException;
import org.xml.sax.InputSource;

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
	private String constraintLanguage;
	private Integer startPosition;
	private Integer maxRecords;
	
	private boolean simpleSearch;
	private String keyword;
	private String title;
	private String startDate;
	private String endDate;
	private HashMap<String,Double> BBOX;
	private String elementSet;
	private String outputSchema;
	private ArrayList<Element> parseResult;
	public Element el;
	
	//private String profile; 
	
	
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
		query +="&constraintLanguage="+this.constraintLanguage;
		query +="&resultType=results";
		if (this.outputSchema!=null){
			if (this.outputSchema.trim().length()!=0)
			query +="&OutputSchema="+this.outputSchema;		
		}
		
		query +="&elementSetName="+this.elementSet.toLowerCase();
		query += "&version="+this.version;
		query +="&constraint_language_version=1.0.0";
		query +="&startPosition="+this.startPosition;
		query +="&maxRecords="+this.maxRecords;
		//query=buildConstrainteRequest(query,"AnyText",this.keyword);
		System.out.println(query);
		
		
		
		return query;
	}
	
	/**
	 * Build constraint query
	 * */
	private String buildConstrainteRequest(String query, String propertyName, String propertyValue){
		String q;
		q= query+"&CONSTRAINT="+propertyName+"+like+'"+propertyValue+"'";
		/*q=query +"&CONSTRAINT=<ogc:Filter xmlns:ogc=http://www.opengis.net/ogc>";
		q +="<ogc:PropertyIsLike wildCard=\"%\" singleChar=\"_\" escape=\"\\\"><ogc:PropertyName>"+propertyName+"</ogc:PropertyName><ogc:Literal>"+propertyValue+"</ogc:Literal></ogc:PropertyIsLike>";
		q +="</ogc:Filter>";*/
		return q;
	}
	/**
	 * this method allows to retrieve outputschema information from capabilitie doc
	 * @throws KettleException 
	 * */
	public ArrayList<String> extractOutputSchemaFromCapabilitiesDocument(String capabilitiesDoc) throws ServletException, IOException, KettleException{
		
		Element rootElement;
		try {
			rootElement = fromStringToJDOMDocument(capabilitiesDoc).getRootElement();
		} catch (KettleException e) {
			throw new KettleException("Error parsing CSW response...", e);
			//e.printStackTrace();
		}
		
		//output schema information is under tag <operation>
		ArrayList<Element> listGetRecords=findElement(rootElement, "Operation");
		Iterator<?> it=listGetRecords.iterator();
		ArrayList<String> content=new ArrayList<String>();
		while (it.hasNext()){
			Element c=(Element)it.next();
			//System.out.println("Att Name -->"+c.getAttribute("name").getValue());
			if (c.getAttribute("name").getValue().equalsIgnoreCase("GetRecords")){
				//System.out.println("Att2 Name -->"+c.getAttribute("name").getValue());
				Iterator<?> itRecord=c.getChildren().iterator();
				while (itRecord.hasNext()){
					Element subRecord=(Element)itRecord.next();
					if (subRecord.getName().equalsIgnoreCase("Parameter")){
						if (subRecord.getAttribute("name").getValue().equalsIgnoreCase("OutputSchema")){
							Iterator<?> it2=subRecord.getChildren().iterator();
							while(it2.hasNext()){
								Element c2=(Element)it2.next();
								content.add(c2.getTextTrim());
								//System.out.println(c2.getName()+" valeur "+c2.getText());
							}
						}
					}
				}
				
			}
		}
		
		
		return content;
	}
	
	/**
	 * @throws IOException 
	 * @throws ServletException  
	 * @throws KettleException 
	 *
	 * **/
	public ArrayList<ArrayList<Object>> getCatalogRecords() throws ServletException, IOException, KettleException{
		ArrayList<ArrayList<Object>> recordList=new ArrayList<ArrayList<Object>>();
		
		
		String pattern=null;
		if (this.elementSet.equalsIgnoreCase("brief")){
			pattern="BriefRecord";
		}else
		if (elementSet.equalsIgnoreCase("summary")){
			pattern="SummaryRecord";
		}else
		if(elementSet.equalsIgnoreCase("full")){
			pattern="Record";
		}
		
		Element rootElement=fromStringToJDOMDocument(GetRecords()).getRootElement();
		ArrayList<Element> el=this.findElement(rootElement,pattern);
		Iterator<?> it=el.iterator();
		while (it.hasNext()){
			Element courant=(Element)it.next();
			Iterator<Element> it2=getColumns(courant).iterator();
			ArrayList<Object> o=new ArrayList<Object>();
			while (it2.hasNext()){
				Element c=it2.next();					
				o.add(c.getText());
				System.out.println(c.getText());
			}	
			recordList.add(o);
		}
		
		return recordList;
	}
	
	/**
	 * Getrecords method
	 * @throws KettleException 
	 * */
	public String GetRecords() throws KettleException{
		String response = null;
		try {
			if (this.method.equalsIgnoreCase("GET")){
			
				response=CSWGET(buildGetRecordsGETQuery());
			} 
			else
			if (this.method.equalsIgnoreCase("POST")){
				//TODO
				System.out.println("POST Method");
			}else
				if(this.method.equalsIgnoreCase("SOAP")){
					//TODO
				}
		}
		catch (KettleException e) {
			throw new KettleException("Error parsing CSW GetRecord...", e);
		}
		System.out.println(response);
		return response;
	}
	
	/**
	 * parse string to JDOM document
	 * */
	public Document fromStringToJDOMDocument(String str)  throws KettleException{
		SAXBuilder parser = new SAXBuilder();
		Document doc;
		try{
			doc = parser.build(new InputSource(new StringReader(str)));
		}catch(Exception e){
			throw new KettleException("Error parsing CSW response...", e);
		}
		return doc;		
	}
	
	/**
	 * GetCapabilities method
	 * */
	public String GetCapabilities()throws KettleException {
		return CSWGET(buildGetCapabilitiesGETQuery());
	}

public Element findSubElement(Element element, String elementName)throws ServletException, IOException{		
		boolean trouve=false;
		List<?> list=element.getChildren();	
		Iterator<?> it=list.iterator();
		while (it.hasNext()&& (trouve==false)){
			Element courant=(Element)it.next();
			if (courant.getQualifiedName().equalsIgnoreCase(elementName)){
				trouve=true;
				el=courant;				
			}
			if ((trouve==false)) 
				findSubElement(courant, elementName);
		}		
		
		return el;
	}

public ArrayList<Element> getColumns(Element element)throws ServletException, IOException{		
	
	parseResult = new ArrayList<Element>();
	
	// Traverse the tree
	GetSubElement(element.getChildren()); 
	
	//System.out.println("ParseResult--> "+parseResult.size());
	
	return parseResult;
}
	
	private void GetSubElement(List<?> elements) throws IOException{			
		// Cycle through all the child nodes of the root
		
		Iterator<?> iter = elements.iterator();
		while (iter.hasNext()){			
	        Element el = (Element) iter.next();	        
	        	if (el.getChildren().size()==0){
	        		parseResult.add(el);
	        	}else
	        		GetSubElement(el.getChildren());
		}
		
	}
	
	
public ArrayList<Element> findElement(Element element, String elementName)throws ServletException, IOException{		
		
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
	/**
	 * @return the constraintLanguage
	 */
	public String getConstraintLanguage() {
		return constraintLanguage;
	}

	/**
	 * @param constraintLanguage the constraintLanguage to set
	 */
	public void setConstraintLanguage(String constraintLanguage) {
		this.constraintLanguage = constraintLanguage;
	}

	/**
	 * @return the startPosition
	 */
	public Integer getStartPosition() {
		return startPosition;
	}

	/**
	 * @param startPosition the startPosition to set
	 */
	public void setStartPosition(Integer startPosition) {
		this.startPosition = startPosition;
	}

	/**
	 * @return the maxRecords
	 */
	public Integer getMaxRecords() {
		return maxRecords;
	}

	/**
	 * @param maxRecords the maxRecords to set
	 */
	public void setMaxRecords(Integer maxRecords) {
		this.maxRecords = maxRecords;
	}
	

}
