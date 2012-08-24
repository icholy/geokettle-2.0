package org.pentaho.di.trans.steps.wpsclient.requestbuilder;

import java.util.Map;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.wpsclient.Messages;

public class DescribeProcessRequestBuilder extends AbstractRequestBuilder{
	private Map<String, String> urls;
	private String processId;

	public DescribeProcessRequestBuilder(Map<String, String> urls, String method, String version, String processId){
		super(method, version);	
		this.urls = urls;	
		this.processId = processId;
	}
	
	public String buildRequest() throws KettleException{
		String requestStr = "";	
		String urlKey;
		if(method.equals(GET)){
			urlKey = "describeProcessGETURL";
			if(urls.containsKey(urlKey)){
				requestStr = buildDescribeProcessGetQuery();
				httpMethod = new GetMethod(urls.get(urlKey));									
			}else
				throw new KettleException(Messages.getString("WPSClient.Log.RequestNotSupported1") + "DescribeProcess" + Messages.getString("WPSClient.Log.RequestNotSupported2") + "GET" + Messages.getString("WPSClient.Log.RequestNotSupported3"));
		}else{
			urlKey = "describeProcessPOSTURL";
			if(urls.containsKey(urlKey)){
				requestStr = buildDescribeProcessPostQuery();
				httpMethod = new PostMethod(urls.get(urlKey));	
			}else
				throw new KettleException(Messages.getString("WPSClient.Log.RequestNotSupported1") + "DescribeProcess" + Messages.getString("WPSClient.Log.RequestNotSupported2") + "POST" + Messages.getString("WPSClient.Log.RequestNotSupported3"));
		}		
		return requestStr;
	}
	
	private String buildDescribeProcessGetQuery(){
		StringBuffer sb = new StringBuffer(buildBaseGetRequest("DescribeProcess"));
		sb.append("&IDENTIFIER=");
		sb.append(processId);
		return sb.toString();
	}

	private String buildDescribeProcessPostQuery(){
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<DescribeProcess service=\"");
		sb.append(SERVICE);
		sb.append("\" ");
		sb.append("xmlns=\"http://www.opengis.net/wps/1.0.0\" ");
		sb.append("xmlns:xlink=\"http://www.w3.org/1999/xlink\" ");
		sb.append("xmlns:ows=\"http://www.opengis.net/ows/1.1\" ");
		sb.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		sb.append("xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 ../wpsDescribeProcess_request.xsd\" ");
		sb.append("version=\"");
		sb.append(version);
		sb.append("\">");	
		sb.append("<ows:Identifier>");
		sb.append(processId);
		sb.append("</ows:Identifier>");
		sb.append("</DescribeProcess>");
		return sb.toString();
	}	
}
