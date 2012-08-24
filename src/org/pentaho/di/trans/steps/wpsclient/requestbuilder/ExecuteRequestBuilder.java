package org.pentaho.di.trans.steps.wpsclient.requestbuilder;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringEscapeUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.wpsclient.Messages;
import org.pentaho.di.trans.steps.wpsclient.parameter.Parameter;
import org.pentaho.di.trans.steps.wpsclient.parameter.format.AbstractFormat;
import org.pentaho.di.trans.steps.wpsclient.parameter.format.BoundingBoxFormat;
import org.pentaho.di.trans.steps.wpsclient.parameter.format.ComplexFormat;
import org.pentaho.di.trans.steps.wpsclient.parameter.format.LiteralFormat;
import org.pentaho.reporting.libraries.formula.util.URLEncoder;

public class ExecuteRequestBuilder extends AbstractRequestBuilder{	
	private String processId;
	
	private Map<String, String> urls;
	
	private Map<String, Parameter> inputParameters;
	private Map<String, Parameter> outputParameters;
	
	private Map<String, Map<String, String>> inputValues;
	private Map<String, Map<String, String>> outputValues;
	
	private boolean hasRawDataOutput;
	
	public ExecuteRequestBuilder(Map<String, String> urls, String method, String version, String processId, Map<String, Parameter> inputParameters, Map<String, Map<String, String>> inputValues, Map<String, Parameter> outputParameters, Map<String, Map<String, String>> outputValues, boolean hasRawDataOutput){
		super(method, version);	
		this.urls = urls;
		this.processId = processId;
		this.inputParameters = inputParameters;
		this.inputValues = inputValues;
		this.outputParameters = outputParameters;
		this.outputValues = outputValues;
		this.hasRawDataOutput = hasRawDataOutput;
	}
	
	public String buildRequest() throws KettleException{
		String requestStr = "";
		String urlKey;
		if(method.equals(GET)){
			urlKey = "executeGETURL";
			if(urls.containsKey(urlKey)){
				requestStr = buildExecuteGetQuery();
				httpMethod = new GetMethod(urls.get(urlKey));	
			}else
				throw new KettleException(Messages.getString("WPSClient.Log.RequestNotSupported1") + "Execute" + Messages.getString("WPSClient.Log.RequestNotSupported2") + "GET" + Messages.getString("WPSClient.Log.RequestNotSupported3"));
		}else{
			urlKey = "executePOSTURL";
			if(urls.containsKey(urlKey)){
				requestStr = buildExecutePostQuery();
				httpMethod = new PostMethod(urls.get(urlKey));	
			}else
				throw new KettleException(Messages.getString("WPSClient.Log.RequestNotSupported1") + "Execute" + Messages.getString("WPSClient.Log.RequestNotSupported2") + "POST" + Messages.getString("WPSClient.Log.RequestNotSupported3"));
		}		
		return requestStr;				
	}
	
	public String buildExecuteGetQuery(){
		StringBuffer sb = new StringBuffer(buildBaseGetRequest("Execute"));
		sb.append("&IDENTIFIER=");
		sb.append(processId);
		sb.append(hasRawDataOutput?"&RawDataOutput=":"&ResponseDocument=");
		Iterator<String> it = outputParameters.keySet().iterator();
		if(it.hasNext()){
			String paramName = it.next();
			sb.append(buildResponseGet(outputParameters.get(paramName), paramName));
		}
		sb.append("&DataInputs=");
		for(String paramName: inputParameters.keySet()){
			sb.append(buildDataInputsGet(inputParameters.get(paramName), paramName));
		}
		if(sb.lastIndexOf(";") == sb.length()-1)
			sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	public String buildResponseGet(Parameter parameter, String paramName){
		StringBuffer sb = new StringBuffer(parameter.getId());
		Map<String, String> values = outputValues.get(paramName);
		Map<String, Object> paramProps = parameter.getDefaultFormat().getProperties();	
		if(parameter.hasLiteralFormat())
			sb.append(getPropertyValueGet(LiteralFormat.DEFAULT_UOM, "uom", values, paramProps, paramProps.get(LiteralFormat.DEFAULT_UOM)));										
		else if(parameter.hasBoundingBoxFormat())
			sb.append(getPropertyValueGet(BoundingBoxFormat.DEFAULT_CRS, "uom", values, paramProps, paramProps.get(BoundingBoxFormat.DEFAULT_CRS)));		
		else
			sb.append(getComplexProperties(values, paramProps));	
		return sb.toString();
	}

	private String getProp(String propertyName, Map<String, String> values, Map<String, Object> paramProps){
		String prop = "";
		if(values!=null && !Const.isEmpty(values.get(propertyName)))
			prop = values.get(propertyName);
		else if(paramProps!=null && paramProps.containsKey(propertyName)){
			Object property = paramProps.get(propertyName);
			if(property != null && !Const.isEmpty(property.toString()))
				prop = property.toString();
		}
		return prop;
	}
	
	private String getPropertyValueGet(String propertyName, String text, Map<String, String> values, Map<String, Object> paramProps, Object defaultValue){		
		StringBuffer sb = new StringBuffer();
		
		String prop = getProp(propertyName, values, paramProps);
		
		if(!Const.isEmpty(prop) && (defaultValue==null || (defaultValue!=null && !defaultValue.equals(prop)))){
			sb.append("@");
			sb.append(text);
			sb.append("=");
			sb.append(encode(prop));	
		}
		
		return sb.toString();
	}
	
	public String encode(String text){
		try {			
			return URLEncoder.encode(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			new KettleException(Messages.getString("WPSClient.Log.UnableToEncodeRequest") + text, e);
		}
		return "";
	}
	
	public String getComplexProperties(Map<String, String> values, Map<String, Object> paramProps){
		StringBuffer sb = new StringBuffer();
		Object defaultMimetypeValueObj = paramProps.get(ComplexFormat.MIMETYPE);
		Object defaultSchemaValueObj = paramProps.get(ComplexFormat.SCHEMA);
		Object defaultEncodingValueObj = paramProps.get(ComplexFormat.ENCODING);
		
		String defaultMimetypeValue = defaultMimetypeValueObj != null?defaultMimetypeValueObj.toString():"";		
		String defaultSchemaValue = defaultSchemaValueObj != null?defaultSchemaValueObj.toString():"";		
		String defaultEncodingValue = defaultEncodingValueObj != null?defaultEncodingValueObj.toString():"";
		
		if(!defaultMimetypeValue.equals(getProp(ComplexFormat.MIMETYPE, values, null)) || !defaultSchemaValue.equals(getProp(ComplexFormat.SCHEMA, values, null)) || !defaultEncodingValue.equals(getProp(ComplexFormat.ENCODING, values, null))){
			sb.append(getPropertyValueGet(ComplexFormat.MIMETYPE, "mimeType", values, null, null));
			sb.append(getPropertyValueGet(ComplexFormat.ENCODING, "encoding", values, null, null));
			sb.append(getPropertyValueGet(ComplexFormat.SCHEMA, "schema", values, null, null));
		}
		return sb.toString();
	}
	
	public String buildDataInputsGet(Parameter parameter, String paramName){
		StringBuffer sb = new StringBuffer();		
		Map<String, String> properties = inputValues.get(paramName);
		AbstractFormat default_format = parameter.getDefaultFormat();
		Map<String, Object> paramProps = default_format != null?default_format.getProperties():new HashMap<String, Object>();
		String value = parameter.getValue();
		if(!Const.isEmpty(value)){	
			if(parameter.hasLiteralFormat()){
				sb.append(encode(value));
				sb.append(getPropertyValueGet(LiteralFormat.DEFAULT_UOM, "uom", properties, paramProps, paramProps.get(LiteralFormat.DEFAULT_UOM)));
				sb.append(getPropertyValueGet(LiteralFormat.DATATYPE, "dataType", properties, paramProps, paramProps.get(LiteralFormat.DATATYPE)));
			}else if(parameter.hasBoundingBoxFormat()){
				sb.append(encode(value));
				sb.append(getPropertyValueGet(BoundingBoxFormat.DEFAULT_CRS, "crs", properties, paramProps, paramProps.get(BoundingBoxFormat.DEFAULT_CRS)));
			}else{
				try {
					new URL(value);					
					sb.append("@xlink:href=");				
				} catch (MalformedURLException e) {}
				
				sb.append(encode(value));
				sb.append(getComplexProperties(properties, paramProps));				
			}
		}	
		return parameter.getId()+"="+sb.toString()+";";
	}
	
	public String buildExecutePostQuery() throws KettleException{
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<Execute  version=\"");
		sb.append(version);
		sb.append("\" service=\"");
		sb.append(SERVICE);
		sb.append("\" xmlns:wfs=\"http://www.opengis.net/wfs/1.1.0\" xmlns=\"http://www.opengis.net/wps/1.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 ../wpsExecute_request.xsd\">");
		sb.append("<ows:Identifier>");
		sb.append(processId);
		sb.append("</ows:Identifier>");
		if(!inputParameters.isEmpty()){
			sb.append("<DataInputs>");
			for(String paramName: inputParameters.keySet()){
				sb.append(buildDataInputsPost(inputParameters.get(paramName), paramName));
			}
			sb.append("</DataInputs>");
		}
		sb.append("<ResponseForm>");
		sb.append(buildResponsePost(outputParameters, hasRawDataOutput));
		sb.append("</ResponseForm>");
		sb.append("</Execute>");
		return sb.toString();
	}

	public String buildResponsePost(Map<String, Parameter> outputParameters, boolean hasRawDataOutput){
		StringBuffer sb = new StringBuffer(hasRawDataOutput?"<RawDataOutput":"<ResponseDocument><Output");
		Iterator<String> it = outputParameters.keySet().iterator();
		if(it.hasNext()){
			String paramName = it.next();
			Parameter parameter = outputParameters.get(paramName);
			Map<String, String> properties = outputValues.get(paramName);
			Map<String, Object> paramProps = parameter.getDefaultFormat().getProperties();
			if(parameter.hasLiteralFormat())
				sb.append(getPropertyValuePost(LiteralFormat.DEFAULT_UOM, "uom", properties, paramProps));										
			else if(parameter.hasBoundingBoxFormat())
				sb.append(getPropertyValuePost(BoundingBoxFormat.DEFAULT_CRS, "uom", properties, paramProps));		
			else{						
				sb.append(getPropertyValuePost(ComplexFormat.MIMETYPE, "mimeType", properties, null));
				sb.append(getPropertyValuePost(ComplexFormat.ENCODING, "encoding", properties, null));
				sb.append(getPropertyValuePost(ComplexFormat.SCHEMA, "schema", properties, null));							
			} 
			sb.append("><ows:Identifier>");
			sb.append(parameter.getId());
			sb.append("</ows:Identifier>");
		}else
			sb.append(">");
		
		sb.append(hasRawDataOutput?"</RawDataOutput>":"</Output></ResponseDocument>");
		return sb.toString();
	}
	
	public String buildDataInputsPost(Parameter parameter, String paramName) throws KettleException{
		StringBuffer sb = new StringBuffer();		
		Map<String, String> properties = inputValues.get(paramName);
		AbstractFormat default_format = parameter.getDefaultFormat();
		Map<String, Object> paramProps = default_format != null?default_format.getProperties():new HashMap<String, Object>();
		String value = parameter.getValue();
		if(!Const.isEmpty(value)){
			sb.append("<Input><ows:Identifier>");	
			sb.append(parameter.getId());
			sb.append("</ows:Identifier>");
			if(parameter.hasLiteralFormat()){
				sb.append("<Data><LiteralData");
				sb.append(getPropertyValuePost(LiteralFormat.DEFAULT_UOM, "uom", properties, paramProps));
				sb.append(getPropertyValuePost(LiteralFormat.DATATYPE, "dataType", properties, paramProps));	
				sb.append(">");
				sb.append(StringEscapeUtils.escapeXml(value));
				sb.append("</LiteralData></Data>");											
			}else if(parameter.hasBoundingBoxFormat()){
				sb.append("<Data><BoundingBoxData");
				sb.append(getPropertyValuePost(BoundingBoxFormat.DEFAULT_CRS, "crs", properties, paramProps));	
				sb.append(">");
				sb.append("<LowerCorner>");
				sb.append(StringEscapeUtils.escapeXml(value));
				sb.append("</LowerCorner>");
				sb.append("<UpperCorner>");
				sb.append(StringEscapeUtils.escapeXml(value));
				sb.append("</UpperCorner>");
				sb.append("</BoundingBoxData></Data>");					
			}else{		
				StringBuffer properties_sb = new StringBuffer();
				properties_sb.append(getPropertyValuePost(ComplexFormat.MIMETYPE, "mimeType", properties, null));
				properties_sb.append(getPropertyValuePost(ComplexFormat.ENCODING, "encoding", properties, null));
				properties_sb.append(getPropertyValuePost(ComplexFormat.SCHEMA, "schema", properties, null));
				try {
					new URL(value);	
					if(value.contains("?")){
						try{
							sb.append("<Reference");
							sb.append(properties_sb.toString());
							sb.append(" xlink:href=\"");
							sb.append(StringEscapeUtils.escapeXml(value));
							sb.append("\" method=\"GET\"/>");							
						}catch(NullPointerException ne){
							throw new KettleException(Messages.getString("RequestBuilder.Log.UnableToEncodeHref"), ne);
						}					
					}				
				} catch (MalformedURLException e) {	
					sb.append("<Data><ComplexData");
					sb.append(properties_sb.toString());
					sb.append(">");
					sb.append(value);
					sb.append("</ComplexData></Data>");
				}										
			}
			sb.append("</Input>");
		}		
		return sb.toString();
	}
	
	private String getPropertyValuePost(String propertyName, String text, Map<String, String> properties, Map<String, Object> paramProps){		
		String prop = "";		
		
		if(properties!=null && !Const.isEmpty(properties.get(propertyName)))
			prop = properties.get(propertyName);
		else if(paramProps!=null && paramProps.containsKey(propertyName)){
			Object property = paramProps.get(propertyName);
			if(property != null && !Const.isEmpty(property.toString()))
				prop = property.toString();
		}
					
		StringBuffer sb = new StringBuffer();
		if(!Const.isEmpty(prop)){
			sb.append(" ");
			sb.append(text);
			sb.append("=\"");
			sb.append(StringEscapeUtils.escapeXml(prop));	
			sb.append("\"");
		}		
		return sb.toString();
	}
}
