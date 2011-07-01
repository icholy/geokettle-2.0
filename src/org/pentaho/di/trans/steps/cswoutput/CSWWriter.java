/**
 * 
 */
package org.pentaho.di.trans.steps.cswoutput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.pentaho.di.core.exception.KettleException;
import org.xml.sax.InputSource;

/**
 * @author O.Mamadou
 *
 */
public class CSWWriter {
	
	public static final String ENTETE_TRANSACTION_INSERT="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
				"<csw:Transaction service=\"CSW\" version=\"2.0.2\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\">"+
				"<csw:Insert>";
	public static final String FOOTPAGE_TRANSACTION_INSERT="</csw:Insert></csw:Transaction>";
	public static final String CSWBRIEF_XML="<csw:Record xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\" xmlns:geonet=\"http://www.fao.org/geonetwork\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">"+
		"<dc:identifier>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</dc:identifier>"+
		"<dc:title>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</dc:title>"+
		" <dc:type>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</dc:type>"+
		"<dc:subject>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</dc:subject>"+
		"<dc:format>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</dc:format>"+
		"<dct:abstract>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</dct:abstract>"+
		"<dc:language>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</dc:language>"+
		"<dc:creator>SPATIALYTICS</dc:creator>"+
		"<dc:publisher>SPATIALYTICS</dc:publisher>"+
		"<dc:rights>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</dc:rights>"+
		"<dc:date>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</dc:date>"+
		"<dc:source>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</dc:source>"+
		"<dc:description>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</dc:description>"+
		"<ows:BoundingBox crs=\"::INFO-PROJECTION\">"+
		"<ows:LowerCorner>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</ows:LowerCorner>"+
		"<ows:UpperCorner>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</ows:UpperCorner>"+
		"</ows:BoundingBox>"+
		"</csw:Record>";
	private static final String HTTP_METHOD = "POST";
		
	private URL cswUrl;
	private URL loginUrl;
	private String username;
	private String password;
	private String schema;
	
	/**
	 * @throws KettleException 
	 * 
	 * */
	public String cswINSERTTransaction(String query) throws KettleException{
		try {
			String response;
			String q=ENTETE_TRANSACTION_INSERT;
			q +=query;
			q +=FOOTPAGE_TRANSACTION_INSERT;
			
			
			
			response=CSWPOST(q);
			//System.out.println(response);
			return response;
		} catch (KettleException e) {			
			throw new KettleException(e);
		}
	}
	
	public String setElementTextUsingQueryString(String query,String elementName,String text) throws KettleException, ServletException, IOException{
		String st=null;
		Document doc= fromStringToJDOMDocument(query);
		Element rootElement=doc.getRootElement();		
		rootElement=findANDReplaceElementText(rootElement, elementName,text);
		XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
	    st=sortie.outputString(rootElement);
	    return st;
	}
	
	/**
	 *find element from JDOM document 
	 * **/
	public Element findANDReplaceElementText(Element element, String elementName,String text)throws ServletException, IOException{		
		boolean trouve=false;
		List<?> list=element.getChildren();	
		Iterator<?> it=list.iterator();
		
		Element el = null;
		while (it.hasNext()&& (trouve==false)){
			Element courant=(Element)it.next();
			if (courant.getName().equalsIgnoreCase(elementName)){
				trouve=true;
				el=courant;
				el.setText(text);
			}
			if ((trouve==false)&&(courant!=null)) 
				
				findANDReplaceElementText(courant, elementName,text);
		}		
		
		return element;
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
	
	private String CSWPOST(String query) throws KettleException{    	   	
    	try { 			
			// Send request			
			HttpURLConnection conn = (HttpURLConnection) cswUrl.openConnection(); 
			conn.setRequestMethod(HTTP_METHOD);			
			conn.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"");	
			conn.setRequestProperty("Cookie", CatalogAuthentication());
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
	
	/**
	 * this method allow connection to csw catalog using login service 
	 * */
	private String CatalogAuthentication() throws KettleException{  
		String cookieInformation=null;
		
		try {
			URL url=this.loginUrl;
			// Send request
    		String userPassword=URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
    		userPassword+="&"+URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
    		    					
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(HTTP_METHOD);
			//conn.setRequestProperty("Cookie", CatalogAuthentication());
						
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
	 * @param cswUrl the cswUrl to set
	 * @throws MalformedURLException 
	 */
	public void setCswUrl(String cswUrl) throws MalformedURLException {		
		this.cswUrl = new URL(cswUrl);
	}
	/**
	 * @return the cswUrl
	 */
	public URL getCswUrl() {
		return cswUrl;
	}
	/**
	 * @param string the loginUrl to set
	 * @throws MalformedURLException 
	 */
	public void setLoginUrl(String loginurl) throws MalformedURLException {
		this.loginUrl = new URL(loginurl);
	}
	/**
	 * @return the loginUrl
	 */
	public URL getLoginUrl() {
		return loginUrl;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param schema the schema to set
	 */
	public void setSchema(String schema) {
		this.schema = schema;
	}

	/**
	 * @return the schema
	 */
	public String getSchema() {
		return schema;
	}
	
}
