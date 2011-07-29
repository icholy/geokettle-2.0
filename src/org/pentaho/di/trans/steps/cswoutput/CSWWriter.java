/**
 * 
 */
package org.pentaho.di.trans.steps.cswoutput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
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
	public static final String CSWBRIEF_XML="<csw:Record xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\"  xmlns:dct=\"http://purl.org/dc/terms/\" xmlns:geonet=\"http://www.fao.org/geonetwork\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">"+
		"<dc:identifier>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Identifier")+"</dc:identifier>"+
		"<dc:title>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Title")+"</dc:title>"+
		" <dc:type>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Type")+"</dc:type>"+
		"<dc:subject>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Subject")+"</dc:subject>"+
		"<dc:format>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Format")+"</dc:format>"+
		"<dct:abstract>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Abstract")+"</dct:abstract>"+
		"<dc:language>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Language")+"</dc:language>"+
		"<dc:creator>Spatialytics Inc</dc:creator>"+
		"<dc:publisher>Spatialytics Inc</dc:publisher>"+
		"<dc:rights>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Rights")+"</dc:rights>"+
		"<dc:date>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Date")+"</dc:date>"+
		"<dc:source>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</dc:source>"+
		"<dc:description>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Description")+"</dc:description>"+
		"<ows:BoundingBox crs=\"::INFO-PROJECTION\">"+
		"<ows:LowerCorner>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.LowCorner")+"</ows:LowerCorner>"+
		"<ows:UpperCorner>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.upcorner")+"</ows:UpperCorner>"+
		"</ows:BoundingBox>"+		
		"<geonet:info xmlns:gml=\"http://www.opengis.net/gml\">"+
		  "<id>107</id>"+ 
		  "<schema>iso19139</schema>"+ 
		  "<createDate>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Date")+"</createDate>"+ 
		  "<changeDate>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Date")+"</changeDate>"+ 
		  "<isTemplate>Y/N</isTemplate>"+ 
		  "<title>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Title")+"</title>"+ 
		  "<source>7ea582d4-9ddf-422e-b28f-29760a4c0147</source>"+ 
		  "<uuid>74d8c1de-81ed-48bf-8742-667b67b1364d</uuid>"+ 
		  "<isHarvested>Y/N</isHarvested>"+ 
		  "<popularity>770</popularity>"+ 
		  "<rating>0</rating>"+ 
		  "<groupowner>6</groupowner>"+ 
		  "<groupName>Spatialytics Inc.</groupName>"+ 
		  "<groupLabel>Spatialytics Corporation</groupLabel>"+ 
		  "<groupLogoUuid>11ef92b3-b547-4a35-af4e-7df6852c30b2</groupLogoUuid>"+ 
		  "<groupWebsite>http://www.spatialytics.com</groupWebsite>"+ 
		  "<view>true/false</view>"+ 
		  "<notify>true/false</notify>"+ 
		  "<download>true/false</download>"+ 
		  "</geonet:info>"+
		"</csw:Record>";
	
	public static String MD_METADATA_XML="<MD_Metadata xmlns=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">"+
    "<fileIdentifier>"+
     "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Identifier")+"</gco:CharacterString>"+
           " </fileIdentifier>"+
           " <language>"+
                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Language")+"</gco:CharacterString>"+
            "</language>"+
            "<hierarchyLevel>"+
                "<MD_ScopeCode codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode\" codeListValue=\"dataset\"/>"+
            "</hierarchyLevel>"+
            "<hierarchyLevelName>"+
                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+ "</gco:CharacterString>"+
            "</hierarchyLevelName>"+
            "<contact>"+
                "<CI_ResponsibleParty>"+
                    "<individualName>"+
                        "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.IndividualName")+ "</gco:CharacterString>"+
                    "</individualName>"+
                    "<organisationName>"+
                        "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.OrganisationName")+"</gco:CharacterString>"+
                    "</organisationName>"+
                    "<contactInfo>"+
                        "<CI_Contact>"+
                            "<address>"+
                                "<CI_Address>"+
                                    "<deliveryPoint>"+
                                        "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.City")+ "</gco:CharacterString>"+
                                    "</deliveryPoint>"+
                                    "<city>"+
                                        "<gco:CharacterString>" + Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.City") +"</gco:CharacterString>"+
                                    "</city>"+
                                    "<postalCode>"+
                                        "<gco:CharacterString>" +Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.PostalCode")+"</gco:CharacterString>"+
                                    "</postalCode>"+
                                    "<country>"+
                                        "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Country")+"</gco:CharacterString>"+
                                    "</country>"+
                                    "<electronicMailAddress>"+
                                        "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Email")+"</gco:CharacterString>"+
                                    "</electronicMailAddress>"+
                                "</CI_Address>"+
                            "</address>"+
                        "</CI_Contact>"+
                    "</contactInfo>"+
                    "<role>"+
                        "<CI_RoleCode codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode\" codeListValue=\"pointOfContact\"/>"+
                    "</role>"+
                "</CI_ResponsibleParty>"+
            "</contact>"+
    "<!-- Date the metadata was created -->"+
            "<dateStamp>"+
                "<gco:Date>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Date")+"</gco:Date>"+
            "</dateStamp>"+
            "<metadataStandardName>"+
                "<gco:CharacterString>ISO 19115 Geographic Information Metadata</gco:CharacterString>"+
            "</metadataStandardName>"+
            "<metadataStandardVersion>"+
                "<gco:CharacterString>2003/Cor.1:2006</gco:CharacterString>"+
            "</metadataStandardVersion>"+
    "<!-- REFERENCE SYSTEM INFORMATION -->"+
            "<referenceSystemInfo>"+
                "<MD_ReferenceSystem>"+
                    "<referenceSystemIdentifier>"+
                        "<RS_Identifier>"+
                            "<code>"+
                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.RefSystID")+"</gco:CharacterString>"+
                            "</code>"+
                            "<codeSpace>"+
                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.REFERENCE_SYSTEM.DEFAULT_VALUE")+"</gco:CharacterString>"+
                            "</codeSpace>"+
                        "</RS_Identifier>"+
                    "</referenceSystemIdentifier>"+
                "</MD_ReferenceSystem>"+
            "</referenceSystemInfo>"+
    "<!-- END REFERENCE SYSTEM INFORMATION -->"+
    "<!-- IDENTIFICATION INFORMATION -->"+
            "<identificationInfo>"+
            "    <MD_DataIdentification>"+
                    "<citation>"+
                        "<CI_Citation>"+
                            "<title>"+
                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Title")+"</gco:CharacterString>"+
                            "</title>"+
                            "<date>"+
                                "<CI_Date>"+
                                "    <date>"+
                                        "<gco:Date>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Date")+"</gco:Date>"+
                                    "</date>"+
                                    "<dateType>"+
                                        "<CI_DateTypeCode codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode\" codeListValue=\"publication\"/>"+
                                    "</dateType>"+
                                "</CI_Date>"+
                            "</date>"+
                            "<edition>"+
                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Edition")+"</gco:CharacterString>"+
                            "</edition>"+
                            "<identifier>"+
                                "<MD_Identifier>"+
                                    "<code>"+
                                        "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Identifier")+"</gco:CharacterString>"+
                                    "</code>"+
                                "</MD_Identifier>"+
                            "</identifier>"+
                            "<series>"+
                                "<CI_Series>"+
                                    "<name>"+
                                        "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</gco:CharacterString>"+
                                    "</name>"+
                                    "<issueIdentification>"+
                                        "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Issue")+"</gco:CharacterString>"+
                                    "</issueIdentification>"+
                                "</CI_Series>"+
                            "</series>"+
                            
                            "<otherCitationDetails>"+
                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</gco:CharacterString>"+
                            "</otherCitationDetails>"+
                            
                        "</CI_Citation>"+
                    "</citation>"+
                    "<abstract>"+
                        "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Abstract")+"</gco:CharacterString>"+
                    "</abstract>"+
                    "<purpose>"+
                        "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Purpose")+"</gco:CharacterString>"+
                    "</purpose>"+
                    "<status>"+
                        "<MD_ProgressCode codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#MD_ProgressCode\" codeListValue=\"completed\"/>"+
                    "</status>"+
                    "<graphicOverview>"+
                    "<MD_BrowseGraphic>"+
                       "<fileName>"+
                          "<gco:CharacterString>thumbnail_s.gif</gco:CharacterString>"+
                       "</fileName>"+
                       "<fileDescription>"+
                          "<gco:CharacterString>thumbnail</gco:CharacterString>"+
                       "</fileDescription>"+
                       "<fileType>"+
                          "<gco:CharacterString>gif</gco:CharacterString>"+
                       "</fileType>"+
                    "</MD_BrowseGraphic>"+
                 "</graphicOverview>"+
                    "<pointOfContact>"+
                        "<CI_ResponsibleParty>"+
                            "<individualName>"+
                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.IndividualName")+"</gco:CharacterString>"+
                            "</individualName>"+
                            "<organisationName>"+
                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.OrganisationName")+"</gco:CharacterString>"+
                            "</organisationName>"+
                            "<positionName>"+
                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</gco:CharacterString>"+
                            "</positionName>"+
                            "<contactInfo>"+
                                "<CI_Contact>"+
                                    "<phone>"+
                                        "<CI_Telephone>"+
                                            "<voice>"+
                                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Telephone")+"</gco:CharacterString>"+
                                            "</voice>"+
                                            "<facsimile>"+
                                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Telephone")+"</gco:CharacterString>"+
                                            "</facsimile>"+
                                        "</CI_Telephone>"+
                                    "</phone>"+
                                    "<address>"+
                                        "<CI_Address>"+
                                            "<deliveryPoint>"+
                                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Address")+"</gco:CharacterString>"+
                                            "</deliveryPoint>"+
                                            "<city>"+
                                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.City")+"</gco:CharacterString>"+
                                            "</city>"+
                                            "<administrativeArea>"+
                                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.City")+"</gco:CharacterString>"+
                                            "</administrativeArea>"+
                                            "<postalCode>"+
                                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.PostalCode")+"</gco:CharacterString>"+
                                            "</postalCode>"+
                                            "<country>"+
                                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Country")+"</gco:CharacterString>"+
                                            "</country>"+
                                            "<electronicMailAddress>"+
                                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Email")+"</gco:CharacterString>"+
                                            "</electronicMailAddress>"+
                                        "</CI_Address>"+
                                    "</address>"+
                                "</CI_Contact>"+
                            "</contactInfo>"+
                            "<role>"+
                                "<CI_RoleCode codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode\" codeListValue=\"pointOfContact\"/>"+
                            "</role>"+
                        "</CI_ResponsibleParty>"+
                    "</pointOfContact>"+
            "<!-- ***** KEYWORDS ***** -->"+
           " <!-- ***** DISCIPLINE ***** -->"+
                    "<descriptiveKeywords>"+
                    "    <MD_Keywords>"+
                            "<keyword>"+
                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Keyword")+"</gco:CharacterString>"+
                            "</keyword>"+                              
                            "<type>"+
                                "<MD_KeywordTypeCode codeList=\"http://metadata.dgiwg.org/codelistRegistry?MD_KeywordTypeCode\" codeListValue=\"theme\"/>"+
                            "</type>"+
                        "</MD_Keywords>"+
                    "</descriptiveKeywords>"+                    
                    "<spatialRepresentationType>"+
                        "<MD_SpatialRepresentationTypeCode codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#MD_SpatialRepresentationTypeCode\" codeListValue=\"vector\"/>"+
                    "</spatialRepresentationType>"+
                    "<language>"+
                        "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Language")+"</gco:CharacterString>"+
                    "</language>"+
                    "<topicCategory>"+
                        "<MD_TopicCategoryCode>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Category")+"</MD_TopicCategoryCode>"+
                    "</topicCategory>"+
            "<!-- Method one for indicating data coverage area -->"+
            "<!-- Use EX_GeographicBoundingBoxType to describe -->"+
            "<!-- the data coverage area using approximate coordinates -->"+
            "<!-- THIS METHOD IS MANDATORY IF THE HIERARCHY  -->"+
            "<!-- LEVEL OF THIS METADATA IS \"DATASET\" -->"+
                    "<extent>"+
                        "<EX_Extent>"+
                            "<geographicElement>"+
                                "<EX_GeographicBoundingBox>"+
                                    "<westBoundLongitude>"+
                                        "<gco:Decimal>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.BBOX")+"</gco:Decimal>"+
                                    "</westBoundLongitude>"+
                                    "<eastBoundLongitude>"+
                                        "<gco:Decimal>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.BBOX")+"</gco:Decimal>"+
                                    "</eastBoundLongitude>"+
                                    "<southBoundLatitude>"+
                                        "<gco:Decimal>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.BBOX")+"</gco:Decimal>"+
                                    "</southBoundLatitude>"+
                                    "<northBoundLatitude>"+
                                        "<gco:Decimal>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.BBOX")+"</gco:Decimal>"+
                                    "</northBoundLatitude>"+
                                "</EX_GeographicBoundingBox>"+
                            "</geographicElement>"+
                        "</EX_Extent>"+
                    "</extent>"+
                    "<extent>"+
                        "<EX_Extent>"+
                            "<temporalElement>"+
                                "<EX_TemporalExtent>"+
                                    "<extent>"+                                    
                                        "<gml:TimePeriod>"+
                                            "<gml:beginPosition>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Date")+"</gml:beginPosition>"+
                                            "<gml:endPosition>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Date")+"</gml:endPosition>"+
                                        "</gml:TimePeriod>"+
                                    "</extent>"+
                                "</EX_TemporalExtent>"+
                            "</temporalElement>"+
                        "</EX_Extent>"+
                    "</extent>"+
                    "<extent>"+
                        "<EX_Extent>"+
                            "<geographicElement>"+
                                "<EX_GeographicDescription>"+
                                    "<geographicIdentifier>"+
                                        "<RS_Identifier>"+
                                            "<code>"+
                                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.RefSystID")+"</gco:CharacterString>"+
                                            "</code>"+
                                        "</RS_Identifier>"+
                                    "</geographicIdentifier>"+
                                "</EX_GeographicDescription>"+
                            "</geographicElement>"+
                        "</EX_Extent>"+
                    "</extent>"+
                    "<supplementalInformation>"+
                        "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</gco:CharacterString>"+
                    "</supplementalInformation>"+
                "</MD_DataIdentification>"+
            "</identificationInfo>"+
            "<dataQualityInfo>"+
                "<DQ_DataQuality>"+
                    "<scope>"+
                        "<DQ_Scope>"+
                            "<level>"+
                                "<MD_ScopeCode codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode\" codeListValue=\"dataset\"/>"+
                            "</level>"+
                        "</DQ_Scope>"+
                    "</scope>"+
                    "<lineage>"+
                        "<LI_Lineage>"+
                            "<source>"+
                                "<LI_Source>"+
                                    "<scaleDenominator>"+
                                        "<MD_RepresentativeFraction>"+
                                            "<denominator>"+
                                                "<gco:Integer>24000</gco:Integer>"+
                                            "</denominator>"+
                                        "</MD_RepresentativeFraction>"+
                                    "</scaleDenominator>"+
                                    "<sourceStep>"+
                                        "<LI_ProcessStep>"+
                                            "<description>"+
                                                "<gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</gco:CharacterString>"+
                                            "</description>"+
                                        "</LI_ProcessStep>"+
                                    "</sourceStep>"+
                                "</LI_Source>"+
                            "</source>"+
                        "</LI_Lineage>"+
                    "</lineage>"+
                "</DQ_DataQuality>"+
            "</dataQualityInfo>"+
        "</MD_Metadata>";
	private static final String HTTP_METHOD = "POST";
		
	private URL cswUrl;
	private URL loginUrl;
	private String username;
	private String password;
	private String schema;
	private ArrayList<Element> parseResult;
	private ArrayList<String[]> mappingColumns;
	private String[] mapColList;
	
	/**
	 * @throws KettleException 
	 * @throws UnsupportedEncodingException 
	 * 
	 * */
	public String cswINSERTTransaction(String query) throws KettleException, UnsupportedEncodingException{
		try {
			String response;
			String q=ENTETE_TRANSACTION_INSERT;
			q +=query;
			q +=FOOTPAGE_TRANSACTION_INSERT;
			q=new String (q.getBytes(),"UTF-8");
					
			response=CSWPOST(q);
			//
			return response;
		} catch (KettleException e) {			
			throw new KettleException(e);
		}
	}
	
	public String setElementTextUsingQueryString(String query,String elementName,String text) throws KettleException, ServletException, IOException{
		String chaine=null;
		if (schema.equalsIgnoreCase(Messages.getString("CSWOutputDialog.Schema.CSWRECORD"))){
			//
			chaine=setElementTextUsingQueryStringfromCSWDocument(query,elementName,text);
		}else
		if (schema.equalsIgnoreCase(Messages.getString("CSWOutputDialog.Schema.MD_METADATA"))){
			//
			chaine=setElementTextUsingQueryStringfromISO19115Document(query,elementName,text);
		}
		return chaine;
	}
	
	private String setElementTextUsingQueryStringfromCSWDocument(String query,String elementName,String text) throws KettleException, ServletException, IOException{
		String st=null;
		Document doc= fromStringToJDOMDocument(query);
		Element rootElement=doc.getRootElement();		
		rootElement=findANDReplaceElementText(rootElement, elementName,text);
		XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
	    st=sortie.outputString(rootElement);
	    return st;
	}
	
	private String setElementTextUsingQueryStringfromISO19115Document(String query,String elementName,String text) throws KettleException, ServletException, IOException{
		String st=null;
		Document doc= fromStringToJDOMDocument(query);
		Element rootElement=doc.getRootElement();		
		rootElement=findANDReplaceElementTextUsingISO19115(rootElement, elementName,text);
		XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
	    st=sortie.outputString(rootElement);
	    return st;
	}
	
	/**
	 *find element from JDOM document and set element text
	 * **/
	public Element findANDReplaceElementText(Element element, String elementName,String text)throws ServletException, IOException{		
		boolean trouve=false;
		List<?> list=element.getChildren();	
		Iterator<?> it=list.iterator();
		
		Element el = null;
		while (it.hasNext()&& (trouve==false)){
			Element courant=(Element)it.next();
			if (courant.getParentElement()!=null){
				//if ((courant.getParentElement().getName()+"_"+courant.getName()).equalsIgnoreCase(elementName)){
				if ((courant.getName()).equalsIgnoreCase(elementName)){					
					trouve=true;
					el=courant;					
					el.setText(text);
				}
				if ((trouve==false)&&(courant!=null)){
					findANDReplaceElementText(courant, elementName,text);
				}
			}
		}		
		
		return element;
	}
	
	public Element findANDReplaceElementTextUsingISO19115(Element element, String elementName,String text)throws ServletException, IOException{		
		boolean trouve=false;
		List<?> list=element.getChildren();	
		Iterator<?> it=list.iterator();
		
		Element el = null;
		while (it.hasNext()&& (trouve==false)){
			Element courant=(Element)it.next();
			String cName=courant.getParentElement().getName();
			
			//(courant.getParentElement().getName()).equalsIgnoreCase(elementName)
				if (elementName.endsWith(cName)){				
					trouve=true;
					el=courant;
					el.setText(text);
				}
				if ((trouve==false)&&(courant!=null)){
					findANDReplaceElementTextUsingISO19115(courant, elementName,text);
				}
			//}	
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
			throw new KettleException("Error transforming string to JDOM Document...", e);
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

	/**
	 * @param mappingColumns the mappingColumns to set
	 */
	public void setMappingColumns(ArrayList<String[]> mappingColumns) {
		this.mappingColumns = mappingColumns;
		/*ArrayList<String> mappingCol= new ArrayList<String>();		
		for(String[] s:mappingColumns){
			if (s[1]!=null)
				mappingCol.add(s[1]);
		}
		this.mapColList=mappingCol.toArray(new String[mappingCol.size()]);*/
	}

	/**
	 * @return the mappingColumns
	 */
	public ArrayList<String[]> getMappingColumns() {
		return mappingColumns;
	}

	/**
	 * @return the mapColList
	 */
	public String[] getMapColList() {
		return mapColList;
	}

	/**
	 * @param mapColList the mapColList to set
	 */
	public void setMapColList(String[] mapColList) {
		this.mapColList = mapColList;
	}
	
}
