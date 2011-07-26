/**
 * 
 */
package org.pentaho.di.trans.steps.cswinput;

import java.io.BufferedReader;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.xml.sax.InputSource;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * @author Ouattara Mamadou
 *
 */
public class CSWReader {
	
	private static final String EXCEPTION_CODE = "ows:Exception";
	private URL catalogUrl;
	private String version;
	private String method;
	private String loginServiceUrl;
	private String username;
	private String password;
	private String constraintLanguage;
	private Integer startPosition;
	private Integer maxRecords;
	private String[] outputSchemaList;
	private String[] queryableElement;
	private String[] comparisonOperator;
	
	private boolean simpleSearch;
	private boolean enableSpatialSearch;
	private String keyword;
	private String title;
	private String startDate;
	private String endDate;
	private HashMap<String,Double> BBOX;
	private String elementSet;
	private String outputSchema;
	private ArrayList<Element> parseResult;
	private ArrayList<String[]> advancedRequestParam;
	
	private Document XMLRequestResult;
	public Element el=null;
	private String capabilitiesDoc;
	
	private String profile;
	private RowMetaInterface columnField;
	private ArrayList<String> ColsName;
	private boolean useLoginService;
	private String spatialOperator;
	
	 
	
	
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
	 * build a GetCapabilities query based on post method
	 * */
	private String buildGetCapabilitiesPOSTQuery(){
		String query="<?xml version=\"1.0\"?>";
		query +="<csw:GetCapabilities xmlns:csw=\"http: // www.opengis.net/cat/csw/2.0.2\" service=\"CSW\">";
		query +=" <ows:AcceptVersions xmlns:ows=\"http://www.opengis.net/ows\">";
		query +="<ows:Version>"+ this.version+"</ows:Version>";
	    query +="</ows:AcceptVersions>";
	    query +="<ows:AcceptFormats xmlns:ows=\"http://www.opengis.net/ows\">";
	    query +="<ows:OutputFormat>application/xml</ows:OutputFormat>";
	    query +=" </ows:AcceptFormats>";
	    query +="</csw:GetCapabilities>";
			
		return query;
	}
	
	/**
	 * build a GetCapabilities query based on SOAP method
	 * */
	/*private String buildGetCapabilitiesSOAPQuery(){
		String query="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		query +="<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">";
		query +="<env:Body>";
		query +="<csw:GetCapabilities xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" service=\"CSW\">";
		query +="<ows:AcceptVersions xmlns:ows=\"http://www.opengis.net/ows\">";
		query +="<ows:Version>"+ this.version +"</ows:Version>";
		query +="</ows:AcceptVersions>";
		query +="<ows:AcceptFormats xmlns:ows=\"http://www.opengis.net/ows\">";
		query +="<ows:OutputFormat>application/xml</ows:OutputFormat>";
		query +="</ows:AcceptFormats>";
		query +="</csw:GetCapabilities>";
		query +="</env:Body>";
		query +="</env:Envelope>";
		return query;
	}*/
	
	/**
	 * build a GetRecords query using POST method
	 * */
	private String buildGetRecordsPOSTQuery(){
		String query="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		query +="<csw:GetRecords xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" service=\"CSW\" version=\""+this.version+"\"";
		query+=" outputSchema=\""+this.outputSchema+"\" resultType=\"results\" startPosition=\""+this.startPosition+"\" maxRecords=\""+this.maxRecords+"\">";
		query +="<csw:Query typeNames=\"csw:Record\">";
		query +="<csw:ElementSetName>"+this.elementSet.toLowerCase()+"</csw:ElementSetName>";
		query +="<csw:Constraint version=\"1.1.0\">";
		query+=buildConstrainteRequest(new String());		
        query +="</csw:Constraint>";
		query +=" </csw:Query>";
		query +="</csw:GetRecords>";
		System.out.println(query);
		return query;
	}
	
	/**
	 * build GetRecords query based on http GET method
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
		query=buildConstrainteRequest(query);
		//
		System.out.println(query);		
		
		return query;
	}
	
	/**
	 * return number of records return by a GetRecord request
	 * */
	
	public int getNumberOfRecord(Document doc,String pattern) throws KettleException{
		Element el2;
		int nb=-1;
		try {
			el2 = findSubElement(doc.getRootElement(),pattern);
			if (el2!=null)
				nb=el2.getAttribute("numberOfRecordsReturned").getIntValue();
			else{
				XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
			   
				throw new KettleException(sortie.outputString(doc));
			}
				
			
		} catch (ServletException e) {
			// TODO Auto-generated catch block

		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (DataConversionException e) {			
			throw new KettleException(e);			
		}
		
		return nb;
		
	}
	
	/**
	 * build constraint request when method is POST
	 * */
	
	private String buildPOSTConstraintRequest(String query){
		String q="";
		
		if (this.simpleSearch==false){
			
			if (keyword!=null && keyword.trim().length()>0){
				q="<csw:CqlText>";
				q +="AnyText like '%"+ this.keyword+"%'";
				q +="</csw:CqlText>";
			}else{
				q="<csw:CqlText>AnyText like '*'</csw:CqlText>";
			}
				
		}else{
			q="<csw:CqlText>";
			boolean ok=false;
			for(int i=0;i<this.advancedRequestParam.size();i++){
				String[] s=advancedRequestParam.get(i);
				//
				String temp="";
				
				if (s.length>0 && (s[0]!=null) && (s[0].trim().length()>0)){
					ok=true;
					for (int j=0;j<3;j++){
						String tmpValue=s[j];
						if (j==1){
							if (tmpValue.equalsIgnoreCase("EqualTo")){
								tmpValue="=";
							}
						}
						if (j==2){
							temp +="'%"+tmpValue+"%' ";
						}else{
							temp +=tmpValue+" ";
						}					
					}
					q +=temp;
					if (i!=advancedRequestParam.size()-1){
						q +=" AND ";
					}					
				}
			}
			if (ok){
				//q+=" AND ";
			}
			/*Build spatial query if user enabled spatial search checkbox*/
			if (this.enableSpatialSearch==true){
				q+=buildSpatialQuery();
			}
			
			q +="</csw:CqlText>";
		}//fin else
		
		return q;
	}
	
	/**
	 * build Constraint request when method is GET
	 * */
	
	private String buildGETConstraintRequest(String query){
		String q=query;
		if (this.method.equalsIgnoreCase("GET")){
			if (this.simpleSearch==false){
				if (keyword!=null && keyword.trim().length()>0)
					q += "&CONSTRAINT=AnyText"+"+like+'"+this.keyword+"'";
			}else{
				q +="&CONSTRAINT=";
				
				for(int i=0;i<this.advancedRequestParam.size();i++){
					String[] s=advancedRequestParam.get(i);
					String temp="";
					if (s[0].trim().length()>0){
					for (int j=0;j<3;j++){
						String tmpValue=s[j];
						if (j==1){
							if (tmpValue.equalsIgnoreCase("EqualTo")){
								tmpValue="=";
							}
						}
						if (j==2){
							temp +="'%"+tmpValue+"%'+";
						}else{
							temp +=tmpValue+"+";
						}					
					}
					q +=temp;
					if (i!=advancedRequestParam.size()-1){
						q +="+AND+";
					}
				}
					
				}
			}			
		}
		return q;
	}
	
	/**
	 * Build constraint query
	 * */
	private String buildConstrainteRequest(String query){
		String q=null;
		if (this.method.equalsIgnoreCase("GET")){
			q=buildGETConstraintRequest(query);
		}//GET
		if (this.method.equalsIgnoreCase("POST")){
			q=buildPOSTConstraintRequest(query);
		}			
		return q;
	}
	
	/**
	 * extract Comparison operators from capabilities doc
	 * */
	public String[] getComparisonOperator(Document doc){
		ArrayList<String> queryableElement=new ArrayList<String>();
		ArrayList<Element> SectionComparisonOp;
		try {
			SectionComparisonOp = findElement(doc.getRootElement(), "ogc:ComparisonOperator");
			for(Element e:SectionComparisonOp){
				queryableElement.add(e.getText());
				//
			}
		} catch (ServletException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return queryableElement.toArray(new String[queryableElement.size()]);
	}
	
	/**
	 * extract queryable elements from capabilities document
	 * */
	
	public String[] getQueryableElement(Document doc){
		ArrayList<String> queryableElement=new ArrayList<String>();
		try {
			ArrayList<Element> SectionOperation=findElement(doc.getRootElement(), "ows:Operation");
			for(Element el:SectionOperation){
				if (el.getAttribute("name").getValue().equalsIgnoreCase("GetRecords")){
					//
					ArrayList<Element> sectionConstraint=findElement(el, "ows:Constraint");
					for(Element s:sectionConstraint){
						if (s.getAttribute("name").getValue().equalsIgnoreCase("SupportedISOQueryables")){
							Iterator<?> it=s.getChildren().iterator();
							while (it.hasNext()){
								Element c=(Element)it.next();
								queryableElement.add(c.getText());								
							}
						}
						
						
					}
					
				}
			}
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Collections.sort(queryableElement);
		return queryableElement.toArray(new String[queryableElement.size()]);
		
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
			throw new KettleException("Error while parsing CSW capabilities document...", e);
			//e.printStackTrace();
		}
		
		//output schema information is under tag <operation>
		ArrayList<Element> listGetRecords=findElement(rootElement, "ows:Operation");
		Iterator<?> it=listGetRecords.iterator();
		ArrayList<String> content=new ArrayList<String>();
		while (it.hasNext()){
			Element c=(Element)it.next();			
			if (c.getAttribute("name").getValue().equalsIgnoreCase("GetRecords")){
				
				Iterator<?> itRecord=c.getChildren().iterator();
				while (itRecord.hasNext()){
					Element subRecord=(Element)itRecord.next();
					
					if (subRecord.getName().equalsIgnoreCase("Parameter")){
						
						if (subRecord.getAttribute("name").getValue().equalsIgnoreCase("OutputSchema")){
							
							Iterator<?> it2=subRecord.getChildren().iterator();
							while(it2.hasNext()){
								Element c2=(Element)it2.next();
								content.add(c2.getTextTrim());								
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
		ArrayList<ArrayList<Object>> recordList = null;	
		String err=null;
		String pattern=profile;	
		if (XMLRequestResult==null)
			return null;
		err=checkIfReponseReturnException(this.XMLRequestResult.getRootElement());
		
		if (err!=null){
			//throw new KettleException(err);
			return null;
			
		}
		if (this.outputSchema.equalsIgnoreCase(CSWInputMeta.DEFAULT_PROFILE)){
			recordList=getCatalogRecordFormattedInDefaultProfile(pattern);
		}else
		if (this.outputSchema.equalsIgnoreCase(CSWInputMeta.ISOTC211_2005_PROFILE)){
			recordList=getCatalogRecordFormattedUsingTC211Profile(pattern);
		}else{
			recordList=getCatalogRecordFormattedUsingTC211Profile(pattern);
		}
		
		return recordList;
	}
	
	/**
	 * this method parses catalog records that have been formatted using default csw profile
	 * */
	private ArrayList<ArrayList<Object>> getCatalogRecordFormattedInDefaultProfile(String profile) throws ServletException, IOException{
		ArrayList<ArrayList<Object>> recordList=new ArrayList<ArrayList<Object>>();
		Element rootElement=null;
		
		try{
			 rootElement=this.XMLRequestResult.getRootElement();
			 
			 ArrayList<Element> el=this.findElement(rootElement,profile);
			 if (this.ColsName==null)
				 return null;
		
			String[] colName=this.ColsName.toArray(new String[ColsName.size()]);
				//columnField.getFieldNames();
			Iterator<?> it=el.iterator();
			while (it.hasNext()){
				Element courant=(Element)it.next();
				ArrayList<Element> tempCour=getColumns(courant);
				//
				ArrayList<Object> o=new ArrayList<Object>();
				int taille=tempCour.size();
				if (colName.length<taille){
					taille=colName.length;
				}
				for(int i=0;i<taille;i++){
					Element e=tempCour.get(i);
					if ((e.getParentElement().getName()+"_"+e.getName()).equalsIgnoreCase(colName[i])){
						if ((e.getName().equalsIgnoreCase("lowercorner"))||(e.getName().equalsIgnoreCase("uppercorner"))){
							String[] coordinate=e.getText().split(" ");
							Geometry g1=null;
							try{
								 g1=new  GeometryFactory().createPoint(new Coordinate(Double.parseDouble(coordinate[0]), Double.parseDouble(coordinate[1])));
							}catch(NumberFormatException ex){
								
							}							
							o.add(g1);
						}else
						o.add(e.getText());	
						
					}else{
						//o.add("N/A");
						o.add(null);
					}
				}
				//
					
				recordList.add(o);
		}
		}catch(Exception e){
			
			throw new IOException(Messages.getString("CSWInput.Exception.ZeroRecord"));
		}
		
		return recordList;
	}
	
	/**
	 * this method parses catalog records formatted using isoTC211 version 2005 profile
	 * */
	
	private ArrayList<ArrayList<Object>> getCatalogRecordFormattedUsingTC211Profile(String profile) throws ServletException, IOException{
		ArrayList<ArrayList<Object>> recordList=new ArrayList<ArrayList<Object>>();
		Element rootElement=null;
		try{
			rootElement=this.XMLRequestResult.getRootElement();
			ArrayList<Element> el=this.findElement(rootElement,profile);
			 if (this.ColsName==null)
				 return null;
			String[] colName=this.ColsName.toArray(new String[ColsName.size()]);			
				
			Iterator<?> it=el.iterator();
			while (it.hasNext()){
				Element courant=(Element)it.next();
				ArrayList<Element> tempCour=getColumns(courant);
				//
				ArrayList<Object> o=new ArrayList<Object>();
				int taille=tempCour.size();
				if (colName.length<taille){
					taille=colName.length;
				}
				for(int i=0;i<taille;i++){
					Element e=tempCour.get(i);
					if ((e.getParentElement().getParentElement().getName()+"_"+e.getParentElement().getName()).equalsIgnoreCase(colName[i])){
						o.add(e.getText());	
						//System.out.print(e.getText());
					}else{
						o.add(null);
					}
				}					
				recordList.add(o);
			}
		}catch(Exception e){
			throw new IOException(Messages.getString("CSWInput.Exception.ZeroRecord"));
		}
		
		return recordList;
	}
	
	/**
	 * Getrecords method
	 * @throws KettleException 
	 * @throws UnsupportedEncodingException 
	 * */
	public String GetRecords() throws KettleException, UnsupportedEncodingException{
		String response = null;
		
		try {
			if (this.method.equalsIgnoreCase("GET")){
				 
				response=CSWGET(buildGetRecordsGETQuery());				
			} 
			else
			if (this.method.equalsIgnoreCase("POST")){
				
				response=CSWPOST(buildGetRecordsPOSTQuery(), this.catalogUrl);
				//
			}else
				if(this.method.equalsIgnoreCase("SOAP")){
					//TODO
				}
		}
		catch (KettleException e) {
			throw new KettleException("Error parsing CSW GetRecord...", e);
		}
		
		byte[] b =response.getBytes();	
		response=new String (b,"UTF-8");
		
		
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
		setXMLRequestResult(doc);
		return doc;		
	}
	
	/**
	 * GetCapabilities method
	 * */
	public String GetCapabilities()throws KettleException {
		if (this.method.equalsIgnoreCase("GET")){
			capabilitiesDoc=CSWGET(buildGetCapabilitiesGETQuery());
		}else
		if (this.method.equalsIgnoreCase("POST")){
			capabilitiesDoc=CSWPOST(this.buildGetCapabilitiesPOSTQuery(), this.catalogUrl);
		}else
		if (this.method.equalsIgnoreCase("SOAP")){
			//TODO
			//capabilitiesDoc=CSWSOAP(this.buildGetCapabilitiesSOAPQuery(), this.catalogUrl);
		}
		return capabilitiesDoc;
	}

/**
 * this method look for any exception in response return by csw server
 * */
public String checkIfReponseReturnException(Element rootElement) throws ServletException, IOException{
	Element el=null;
	String s=null;
	this.el=null;
	el=findSubElement(rootElement,EXCEPTION_CODE);
	if (el!=null){
		XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
		s=sortie.outputString(el);
	}	
	return s;
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
			if ((trouve==false)&&(courant!=null)) 
				
				findSubElement(courant, elementName);
		}		
		
		return el;
}

public ArrayList<Element> getColumns(Element element)throws ServletException, IOException{		
	
	parseResult = new ArrayList<Element>();
	
	// Traverse the tree
	GetSubElement(element.getChildren()); 
	
	
	
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
	        
	        if (el.getQualifiedName().equals(elementName)) parseResult.add((Element) el);
	       	       
	        // If the node has children, call this method with the node
	        if (el.getChildren()!=null) recurse_findElement(el.getChildren(), elementName);	        
		}
	}
	
	private String CSWGET(String query) throws KettleException{    	   	
		HttpMethod httpMethod = new GetMethod(query);
		if (useLoginService==true)
			httpMethod.setRequestHeader("Cookie", this.CatalogAuthentication());
			//
				
		try { 			
			 //Prepare HTTP Get		
    		 HttpClient httpclient = new HttpClient();
    		  		
    		 
    		 httpclient.executeMethod(httpMethod);
    		 
             // the response
             InputStream inputStream = httpMethod.getResponseBodyAsStream();
             StringBuffer bodyBuffer = new StringBuffer();
             int c; 
             while ( (c=inputStream.read())!=-1) 
            	 bodyBuffer.append((char)c);
             inputStream.close();

    		 return bodyBuffer.toString();
		}catch (IOException e) { 
			throw new KettleException("Error connecting to catalog using GET method...", e);
		}finally{
			 httpMethod.releaseConnection();
		}
	}
	
	
	/**
	 * this method allow connection to csw catalog using login service 
	 * */
	private String CatalogAuthentication() throws KettleException{  
		String cookieInformation=null;
		
		try {
			URL url=new URL(this.loginServiceUrl);
			// Send request
    		String userPassword=URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
    		userPassword+="&"+URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
    		    					
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(method);			
						
			conn.setDoOutput(true);

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()); 
			wr.write(userPassword); 
			wr.flush(); 			
			     
			// Get the response 
			InputStreamReader valTemp=new InputStreamReader(conn.getInputStream());
			BufferedReader rd = new BufferedReader(valTemp); 
			String response = "";
			String line; 	
			while ((line = rd.readLine()) != null) 
				response += line;			 			
			wr.close(); 
			rd.close();
			cookieInformation=getCookieInformation(conn);
			return cookieInformation;
		}catch (Exception e) { 
			throw new KettleException("Error while connecting to catalog using authentication service...", e);
		}
	}
	
	/**
	 * this method retrive cookie value from an HTTP response
	 * */
	private String getCookieInformation(HttpURLConnection conn){
		String headerField=null;		
		Map<String,List<String>>headerList=conn.getHeaderFields();
		int i=0;
		boolean trouve=false;
		while (i<headerList.size()&& trouve==false){
			String headerFieldName=conn.getHeaderFieldKey(i);
			if (headerFieldName!=null && headerFieldName.equalsIgnoreCase("Set-Cookie")){
				trouve=true;
				headerField=conn.getHeaderField(i);
			}
			i++;
		}
		return headerField;
	}
	/**
	 * */
	
	private String CSWPOST(String query, URL url) throws KettleException{    	   	
    	try { 			
			// Send request		 
    			
    		HttpURLConnection conn=(HttpURLConnection) url.openConnection();			
			conn.setRequestMethod(method);
			if (useLoginService==true){
				conn.setRequestProperty("Cookie", CatalogAuthentication());
			}
			
			conn.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"");			
			conn.setDoOutput(true);

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()); 
			wr.write(query); 
			wr.flush(); 
				      
			// Get the response 
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream())); 
			String response = "";
			String line; 	
			while ((line = rd.readLine()) != null) response += line;			 			
			wr.close(); 
			rd.close();
			return response;
		}catch (Exception e) { 
			throw new KettleException("Error connecting to CSW catalog using POST method...", e);
		}
	}
	
	
	/*private String CSWSOAP(String query, URL url) throws KettleException{    	   	
    	try { 			
			// Send request			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "soap+xml; charset=\"utf-8\"");
			conn.setDoOutput(true);

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()); 
			wr.write(query); 
			wr.flush(); 
				      
			// Get the response 
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream())); 
			String response = "";
			String line; 	
			while ((line = rd.readLine()) != null) response += line;			 			
			wr.close(); 
			rd.close();
			return response;
		}catch (Exception e) { 
			throw new KettleException("Error connecting to CSW catalog using SOAP method...", e);
		}
	}*/
	
	/**
	 * build spatial query
	 * */
	private String buildSpatialQuery(){
		//WITHIN(ows:BoundingBox,ENVELOPE(3.19,7.26,53.59,50.67))
		String q=null;
		double xmin=BBOX.get("WEST");
		double ymin=BBOX.get("NORTH");
		double xmax=BBOX.get("EAST");
		double ymax=BBOX.get("SOUTH");
		if (spatialOperator.equalsIgnoreCase("bbox")){			
			q=spatialOperator+"(BoundingBox,"+xmin+","+ymin+","+xmax+","+ymax+")";
		}else{
			q=spatialOperator+"(BoundingBox,ENVELOPE("+xmin+","+ymin+","+xmax+","+ymax+"))";
		}
		
		//
		
		return q;
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

	/**
	 * @param xMLRequestResult the xMLRequestResult to set
	 */
	public void setXMLRequestResult(Document xMLRequestResult) {
		XMLRequestResult = xMLRequestResult;
	}

	/**
	 * @return the xMLRequestResult
	 */
	public Document getXMLRequestResult() {
		return XMLRequestResult;
	}

	/**
	 * @param advancedRequestParam the advancedRequestParam to set
	 */
	public void setAdvancedRequestParam(ArrayList<String[]> advancedRequestParam) {
		this.advancedRequestParam = advancedRequestParam;
	}

	/**
	 * @return the advancedRequestParam
	 */
	public ArrayList<String[]> getAdvancedRequestParam() {
		return advancedRequestParam;
	}

	/**
	 * @return the capabilitiesDoc
	 */
	public String getCapabilitiesDoc() {
		return capabilitiesDoc;
	}

	/**
	 * @param capabilitiesDoc the capabilitiesDoc to set
	 */
	public void setCapabilitiesDoc(String capabilitiesDoc) {
		this.capabilitiesDoc = capabilitiesDoc;
	}

	

	/**
	 * @param profile the profile to set
	 */
	public void setProfile(String profile) {
		this.profile = profile;
	}

	/**
	 * @return the profile
	 */
	public String getProfile() {
		return profile;
	}

	/**
	 * @param columnField the columnField to set
	 */
	public void setColumnField(RowMetaInterface columnField) {
		this.columnField = columnField;
	}

	/**
	 * @return the columnField
	 */
	public RowMetaInterface getColumnField() {
		return columnField;
	}

	/**
	 * @param colsName the colsName to set
	 */
	public void setColsName(ArrayList<String> colsName) {
		ColsName = colsName;
	}

	/**
	 * @return the colsName
	 */
	public ArrayList<String> getColsName() {
		return ColsName;
	}

	/**
	 * @return the useLoginService
	 */
	public boolean isUseLoginService() {
		return useLoginService;
	}

	/**
	 * @param useLoginService the useLoginService to set
	 */
	public void setUseLoginService(boolean useLoginService) {
		this.useLoginService = useLoginService;
	}

	/**
	 * @param spatialOperator the spatialOperator to set
	 */
	public void setSpatialOperator(String spatialOperator) {
		this.spatialOperator = spatialOperator;
	}

	/**
	 * @return the spatialOperator
	 */
	public String getSpatialOperator() {
		return spatialOperator;
	}

	/**
	 * @param outputSchemaList the outputSchemaList to set
	 */
	public void setOutputSchemaList(String[] outputSchemaList) {
		this.outputSchemaList = outputSchemaList;
	}

	/**
	 * @return the outputSchemaList
	 */
	public String[] getOutputSchemaList() {
		return outputSchemaList;
	}

	/**
	 * @param queryableElement the queryableElement to set
	 */
	public void setQueryableElement(String[] queryableElement) {
		this.queryableElement = queryableElement;
	}

	/**
	 * @return the queryableElement
	 */
	public String[] getQueryableElement() {
		return queryableElement;
	}

	/**
	 * @param comparisonOperator the comparisonOperator to set
	 */
	public void setComparisonOperator(String[] comparisonOperator) {
		this.comparisonOperator = comparisonOperator;
	}

	/**
	 * @return the comparisonOperator
	 */
	public String[] getComparisonOperator() {
		return comparisonOperator;
	}

	/**
	 * @param enableSpatialSearch the enableSpatialSearch to set
	 */
	public void setEnableSpatialSearch(boolean enableSpatialSearch) {
		this.enableSpatialSearch = enableSpatialSearch;
	}

	/**
	 * @return the enableSpatialSearch
	 */
	public boolean isEnableSpatialSearch() {
		return enableSpatialSearch;
	}
	

}
