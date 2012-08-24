package org.pentaho.di.trans.steps.wpsclient.requestbuilder;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.pentaho.di.core.exception.KettleException;

public class GetCapabilitiesRequestBuilder extends AbstractRequestBuilder{
	private String url;
	
	public GetCapabilitiesRequestBuilder(String url, String method, String version){
		super(method, version);		
		this.url = url;
	}
	
	public String buildRequest() throws KettleException{
		String requestStr = "";
		if(method.equals(GET)){
			requestStr = buildGetCapabilitiesGetQuery();
			httpMethod = new GetMethod(url);
		}else{
			requestStr = buildGetCapabilitiesPostQuery();
			httpMethod = new PostMethod(url);		
		}
		return requestStr;				
	}

	private String buildGetCapabilitiesGetQuery(){
		return buildBaseGetRequest("GetCapabilities");
	}

	private String buildGetCapabilitiesPostQuery(){
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<GetCapabilities service=\"");
		sb.append(SERVICE);
		sb.append("\" ");
		sb.append("xmlns=\"http://www.opengis.net/wps/1.0.0\" ");
		sb.append("xmlns:xlink=\"http://www.w3.org/1999/xlink\" ");
		sb.append("xmlns:ows=\"http://www.opengis.net/ows/1.1\" ");
		sb.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		sb.append("xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 ../wpsGetCapabilities_request.xsd\">");	
		sb.append("<AcceptVersions>");
		sb.append("<ows:Version>");
		sb.append(version);
		sb.append("</ows:Version>");
		sb.append("</AcceptVersions>");
		sb.append("</GetCapabilities>");
		return sb.toString();
	}	
}
