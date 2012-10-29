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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.pentaho.di.core.exception.KettleException;
import org.xml.sax.InputSource;

/**
 * @author mouattara,jmathieu,tbadard
 *
 */
public class CSWWriter {

	private static SimpleDateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
	private static String TODAY = dfm.format(new Date()) ;
	
	
	public static final String ENTETE_TRANSACTION_INSERT="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
				"<csw:Transaction service=\"CSW\" version=\"2.0.2\" xmlns:ogc=\"http://http://www.opengis.net/ogc\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">"+
				"<csw:Insert>";
	public static final String FOOTPAGE_TRANSACTION_INSERT="</csw:Insert></csw:Transaction>";
	
	public static final String ENTETE_TRANSACTION_UPDATE="<?xml version=\"1.0\" encoding=\"UTF-8\"?><csw:Transaction service=\"CSW\" version=\"2.0.2\" xmlns:apiso=\"http://www.opengis.net/cat/csw/apiso/1.0\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:ogc=\"http://http://www.opengis.net/ogc\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\"><csw:Update>";
	public static final String FOOTPAGE_TRANSACTION_UPDATE="</csw:Update></csw:Transaction>";
	
	public static final String ENTETE_TRANSACTION_DELETE="<?xml version=\"1.0\" encoding=\"UTF-8\"?><csw:Transaction service=\"CSW\" version=\"2.0.2\" xmlns:apiso=\"http://www.opengis.net/cat/csw/apiso/1.0\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:ogc=\"http://http://www.opengis.net/ogc\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:dct=\"http://purl.org/dc/terms/\"><csw:Delete>";
	public static final String FOOTPAGE_TRANSACTION_DELETE="</csw:Delete></csw:Transaction>";
	
	public static final String CSWBRIEF_XML="<csw:Record xmlns:dct=\"http://purl.org/dc/terms/\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:geonet=\"http://www.fao.org/geonetwork\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:ows=\"http://www.opengis.net/ows\">"+
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
		"<dc:date>"+TODAY+"</dc:date>"+
		"<dc:source>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE")+"</dc:source>"+
		"<dc:description>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Description")+"</dc:description>"+
		"<ows:BoundingBox crs=\"::INFO-PROJECTION\">"+
		"<ows:LowerCorner>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.LowCorner")+"</ows:LowerCorner>"+
		"<ows:UpperCorner>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.upcorner")+"</ows:UpperCorner>"+
		"</ows:BoundingBox>"+		
		"</csw:Record>";
	
	public static String MD_METADATA_XML="<MD_Metadata xmlns=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">"+
    "<fileIdentifier><gco:CharacterString>"+Messages.getString("CSWOutput.Transaction.DEFAULT_VALUE.Identifier")+"</gco:CharacterString></fileIdentifier>"+
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
                "<gco:Date>"+TODAY+"</gco:Date>"+
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
                                        "<gco:Date>"+TODAY+"</gco:Date>"+
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
                                            "<gml:beginPosition>"+TODAY+"</gml:beginPosition>"+
                                            "<gml:endPosition>"+TODAY+"</gml:endPosition>"+
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
	private String request;
	private String schema;
	private ArrayList<Element> parseResult;
	private ArrayList<String[]> mappingColumns;
	private String[] mapColList;
	private String[] prevColumnList;
	private boolean useLoginService;
	
	private String getIdentifier(Element root) {
		String result;
		result = schema.equals(Messages.getString("CSWOutputDialog.Schema.MD_METADATA")) ? root.getChild("fileIdentifier", Namespace.getNamespace("http://www.isotc211.org/2005/gmd")).getChildTextNormalize("CharacterString", Namespace.getNamespace("http://www.isotc211.org/2005/gco")) : root.getChildTextNormalize("identifier", Namespace.getNamespace("http://purl.org/dc/elements/1.1/"));
		return result;
	}
	
	private String getFilter(Element query) {
		String name = "apiso:identifier";
		StringBuilder sb = new StringBuilder("<csw:Constraint version=\"1.0.0\"><Filter xmlns=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\"><PropertyIsEqualTo><PropertyName>");
		sb.append(name);
		sb.append("</PropertyName><Literal>");
		sb.append(getIdentifier(query));
		sb.append("</Literal></PropertyIsEqualTo></Filter></csw:Constraint>");
		return sb.toString();
	}
	
	public boolean isUseLoginService() {
		return useLoginService;
	}
	
	/**
	 * @throws KettleException 
	 * @throws UnsupportedEncodingException 
	 * 
	 * */
	private String cswINSERTTransaction(String query) throws KettleException, UnsupportedEncodingException{
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
	
	private String cswUPDATETransaction(String query) throws KettleException, UnsupportedEncodingException{
		try {
			
			StringBuilder st = new StringBuilder("<response>");
			Element rootElement=fromStringToJDOMDocument("<root>"+query+"</root>").getRootElement();
			List<?> list=rootElement.getChildren();
			final Iterator<?> it=list.iterator();
			while (it.hasNext()){
				Element courant1=(Element)it.next();
				Element courant=(Element) courant1.clone();
				StringBuilder sb = new StringBuilder(ENTETE_TRANSACTION_UPDATE);
				courant.detach();
				Format fmt = Format.getPrettyFormat();
				fmt.setOmitDeclaration(true);
				
			    XMLOutputter outputter = new XMLOutputter(fmt);
		        String xmlString = outputter.outputString(new Document(courant));
				sb.append(xmlString);
				sb.append(getFilter(courant));
				sb.append(FOOTPAGE_TRANSACTION_UPDATE);
				sb = new StringBuilder (new String(sb.toString().getBytes(),"UTF-8"));
				st.append(CSWPOST(sb.toString()));
				st.delete(st.indexOf("<?"),st.indexOf("?>")+2);
			}
			st.append("</response>");
			return st.toString();
			
		} catch (KettleException e) {			
			throw new KettleException(e);
		}
	}
	
	private String cswDELETETransaction(String query) throws KettleException, UnsupportedEncodingException{

		try {
			
			StringBuilder st = new StringBuilder("<response>");
			Element rootElement=fromStringToJDOMDocument("<root>"+query+"</root>").getRootElement();
			List<?> list=rootElement.getChildren();
			final Iterator<?> it=list.iterator();
			while (it.hasNext()){
				Element courant1=(Element)it.next();
				Element courant=(Element) courant1.clone();
				StringBuilder sb = new StringBuilder(ENTETE_TRANSACTION_DELETE);
				/*	courant.detach();
				Format fmt = Format.getPrettyFormat();
				fmt.setOmitDeclaration(true);
				
			    XMLOutputter outputter = new XMLOutputter(fmt);
		        String xmlString = outputter.outputString(new Document(courant));
				sb.append(xmlString);*/
				sb.append(getFilter(courant));
				sb.append(FOOTPAGE_TRANSACTION_DELETE);
				sb = new StringBuilder (new String(sb.toString().getBytes(),"UTF-8"));
				st.append(CSWPOST(sb.toString()));
				st.delete(st.indexOf("<?"),st.indexOf("?>")+2);
			}
			st.append("</response>");
			return st.toString();
			
		} catch (KettleException e) {			
			throw new KettleException(e);
		}
	}
	
	public String execute(String query) throws KettleException, UnsupportedEncodingException{
		String response;
		if(request.equals(Messages.getString("CSWOutputDialog.Request.Insert")))
			response = cswINSERTTransaction(query);
		else if(request.equals(Messages.getString("CSWOutputDialog.Request.Update")))
			response = cswUPDATETransaction(query);
		else
			response = cswDELETETransaction(query);
		return response;
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
				if ((courant.getParentElement().getName()+"_"+courant.getName()).equalsIgnoreCase(elementName)){
				//if ((courant.getName()).equalsIgnoreCase(elementName)){					
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
			
			if (this.useLoginService==true){
				conn.setRequestProperty("Cookie", CatalogAuthentication());
			}
			
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
			@SuppressWarnings("unused")
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
	 */
	public void setCswUrl(String cswUrl) {		
		try {
			this.cswUrl = new URL(cswUrl);
		} catch (MalformedURLException e) {
			this.cswUrl = null;
		}
	}
	/**
	 * @return the cswUrl
	 */
	public URL getCswUrl() {
		return cswUrl;
	}
	/**
	 * @param string the loginUrl to set
	 */
	public void setLoginUrl(String loginurl) {
		try {
			this.loginUrl = new URL(loginurl);
		} catch (MalformedURLException e) {
			this.loginUrl = null;
		}
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
	 * @param request
	 */
	public void setRequest(String request) {
		this.request = request;
	}
	
	public void setUseLoginService(boolean useLoginService) {
		this.useLoginService = useLoginService;
	}

	/**
	 * @return the request
	 */
	public String getRequest() {
		return request;
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

	/**
	 * @param prevColumnList the prevColumnList to set
	 */
	public void setPrevColumnList(String[] prevColumnList) {
		this.prevColumnList = prevColumnList;
	}

	/**
	 * @return the prevColumnList
	 */
	public String[] getPrevColumnList() {
		return prevColumnList;
	}
	
}
