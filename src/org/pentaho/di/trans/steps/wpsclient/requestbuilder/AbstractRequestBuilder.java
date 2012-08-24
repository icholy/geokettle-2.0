package org.pentaho.di.trans.steps.wpsclient.requestbuilder;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.wpsclient.Messages;

public abstract class AbstractRequestBuilder {
	protected static final String GET = "GET";
	protected static final String SERVICE = "WPS";
	
	protected String method;
	protected String version;
	
	protected HttpMethod httpMethod;

	protected AbstractRequestBuilder(String method, String version) {
		this.method = method;
		this.version = version;
	}
	
	public void setMethod(String method){
		this.method = method;
	}
	
	protected String buildBaseGetRequest(String request){
		StringBuffer sb = new StringBuffer();
		sb.append("SERVICE=");
		sb.append(SERVICE);
		sb.append("&REQUEST=");
		sb.append(request);
		sb.append("&VERSION=");		
		sb.append(version);
		return sb.toString();
	}	
	
	public String sendRequest() throws KettleException{ 
		String request = buildRequest();
		if(method.equals(GET))
			httpMethod.setQueryString(request);
		else
			((PostMethod) httpMethod).setRequestEntity(new StringRequestEntity(request));
		
		try { 					
    		 HttpClient httpclient = new HttpClient();
    		    		
    		 httpclient.executeMethod(httpMethod);
             
             InputStream is = httpMethod.getResponseBodyAsStream();
             
             StringBuffer sb = new StringBuffer();
             
             int c;
             
             while ((c=is.read())!=-1) 
            	 sb.append((char)c);
             
             is.close();

    		 return sb.toString();
		}catch (IOException e) { 
			throw new KettleException(Messages.getString("RequestBuilder.Log.UnableToConnect"), e);
		}finally{
			httpMethod.releaseConnection();
		}
	}
	
	abstract String buildRequest() throws KettleException;
}
