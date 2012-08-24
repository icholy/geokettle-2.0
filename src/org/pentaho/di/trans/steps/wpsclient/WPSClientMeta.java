package org.pentaho.di.trans.steps.wpsclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.wpsclient.parameter.Parameter;
import org.pentaho.di.trans.steps.wpsclient.parameter.format.AbstractFormat;
import org.pentaho.di.trans.steps.wpsclient.parameter.format.BoundingBoxFormat;
import org.pentaho.di.trans.steps.wpsclient.parameter.format.ComplexFormat;
import org.pentaho.di.trans.steps.wpsclient.parameter.format.LiteralFormat;
import org.w3c.dom.Node;

public class WPSClientMeta extends BaseStepMeta implements StepMetaInterface{
	private static final String MIMETYPE = "Mime_Type";
	private static final String YES = "Y";
	
    private String  							url; 
    private String 								version;
    private String 								method;
    private String								process;
    private String								processId;
    
	private String  							resultFieldName;
    
    private Map<String, String>					urls;
    
    private boolean 							hasRawDataOutput;
    
    private Map<String, Parameter>  			inputParameters;
    private Map<String, Parameter>  			outputParameters;

    private Map<String, Map<String, String>>  	inputValues;
    private Map<String, Map<String, String>>  	outputValues;
    
    public WPSClientMeta(){
        super(); // allocate BaseStepMeta
    }
    
    public String getMethod(){
    	return method;
    }   
    
    public void setMethod(String method){
    	this.method=method;
    }
    
    public String getProcess(){
    	return process;
    }   
    
    public void setProcess(String process){
    	this.process = process;
    }
    
    public String getProcessId(){
    	return processId;
    }   
    
    public Map<String, String> getUrls() {
		return urls;
	}

	public void setUrls(Map<String, String> urls) {
		this.urls = urls;
	}

    public boolean hasRawDataOutput(){
    	return hasRawDataOutput;
    }
    
    public void setHasRawDataOutput(boolean hasRawDataOutput){
    	this.hasRawDataOutput = hasRawDataOutput;
    }
    
    public void setProcessId(String processId){
    	this.processId = processId;
    }
    
    public Map<String, Parameter> getInputParameters(){
    	return inputParameters;
    } 
    
    public Map<String, Map<String, String>> getInputValues(){
    	return inputValues;
    } 
    
    public Map<String, Map<String, String>> getOutputValues(){
    	return outputValues;
    } 
    
    public void setInputValues(Map<String, Map<String, String>> values){
    	inputValues = values;
    } 

    public void setOutputValues(Map<String, Map<String, String>> values){
    	outputValues = values;
    }
    
    public void setInputParameters(Map<String, Parameter> inputParameters){
    	this.inputParameters = inputParameters;
    }
    
    public Map<String, Parameter> getOutputParameters(){
    	return outputParameters;
    }   
    
    public void setOutputParameters(Map<String, Parameter> outputParameters){
    	this.outputParameters = outputParameters;
    }
    
    public String getVersion(){
    	return version;
    }   
    
    public void setVersion(String version){
    	this.version=version;
    }     

    public String getUrl(){
        return url;
    }
    
    public void setUrl(String url){
        this.url=url;
    }   

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans){
        return new WPSClient(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData(){
        return new WPSClientData();
    }
    
    public void allocateInputParameters(int nrInputParameters) {
		inputParameters = new HashMap<String, Parameter>(nrInputParameters);
	}
    
    public void allocateInputValues(int nrInputValues) {
    	inputValues = new HashMap<String, Map<String, String>>(nrInputValues);
	}
    
    public void allocateOutputValues(int nrOutputValues) {
    	outputValues = new HashMap<String, Map<String, String>>(nrOutputValues);
	}
    
    public void allocateOutputParameters(int nrOutputParameters) {
		outputParameters = new HashMap<String, Parameter>(nrOutputParameters);
		outputValues = new HashMap<String, Map<String, String>>(nrOutputParameters);
	}
    
    public String getResultFieldName(){
        return resultFieldName;
    }

    public void setResultFieldName(String resultFieldName){
        this.resultFieldName = resultFieldName;
    }
    
	private Parameter buildParameterFromData(Node parameterNode){
		String id = XMLHandler.getTagValue(parameterNode, "id");
		String abs = XMLHandler.getTagValue(parameterNode, "abstract");
		boolean isMandatory = "Y".equalsIgnoreCase(XMLHandler.getTagValue(parameterNode, "isMandatory"));
		boolean hasLiteralFormat = "Y".equalsIgnoreCase(XMLHandler.getTagValue(parameterNode, "hasLiteralFormat"));
		boolean hasBoundingBoxFormat = "Y".equalsIgnoreCase(XMLHandler.getTagValue(parameterNode, "hasBoundingBoxFormat"));
		
		Parameter parameter = new Parameter(isMandatory, id, abs);
		parameter.setValue(XMLHandler.getTagValue(parameterNode, "value"));
		parameter.setFieldName(XMLHandler.getTagValue(parameterNode, "fieldName"));
		
		AbstractFormat format;

		if(hasLiteralFormat || hasBoundingBoxFormat){	
			Node formatNode = XMLHandler.getSubNode(parameterNode, "defaultFormat");
			if(hasLiteralFormat){
				format = readLiteralFormatFromData(formatNode);						
				parameter.setHasLiteralFormat(hasLiteralFormat);
			}else{
				format = readBoundingBoxFormatFromData(formatNode);						
				parameter.setHasBoundingBoxFormat(hasBoundingBoxFormat);
			}									
			parameter.addSupportedFormat(format);
			parameter.setDefaultFormat(format);							
		}else{///// Complex Format //////	
			Node supportedFormatsNode = XMLHandler.getSubNode(parameterNode, "supportedFormats");
			for (int j = 0; j < XMLHandler.countNodes(supportedFormatsNode, "format"); j++) {
				parameter.addSupportedFormat(readComplexFormatFromData(XMLHandler.getSubNodeByNr(supportedFormatsNode, "format", j)));
			}
			Node defaultFormatNode = XMLHandler.getSubNode(parameterNode, "defaultFormat");
			parameter.setDefaultFormat(readComplexFormatFromData(defaultFormatNode));				
		}
		
		return parameter;
	}
	
	private ComplexFormat readComplexFormatFromData(Node formatNode){
		String mime_type = XMLHandler.getNodeValue(XMLHandler.getSubNode(formatNode, MIMETYPE));							
		String encoding = XMLHandler.getNodeValue(XMLHandler.getSubNode(formatNode, ComplexFormat.ENCODING));
		String schema = XMLHandler.getNodeValue(XMLHandler.getSubNode(formatNode, ComplexFormat.SCHEMA));		
		return new ComplexFormat(mime_type, encoding, schema);
	}
	
	private LiteralFormat readLiteralFormatFromData(Node formatNode){	
		Node supportedUOMsNodes = XMLHandler.getSubNode(formatNode, LiteralFormat.SUPPORTED_UOMS);	
		int count =  XMLHandler.countNodes(supportedUOMsNodes, "UOM");
		List<String> supported_uoms = new ArrayList<String>(count);
		for (int i = 0; i < count; i++) {
			supported_uoms.add(XMLHandler.getNodeValue(XMLHandler.getSubNodeByNr(supportedUOMsNodes, "UOM", i)));
		}
		String default_uom = XMLHandler.getNodeValue(XMLHandler.getSubNode(formatNode, LiteralFormat.DEFAULT_UOM));
		String default_value = XMLHandler.getNodeValue(XMLHandler.getSubNode(formatNode, LiteralFormat.DEFAULT_VALUE));
		String datatype = XMLHandler.getNodeValue(XMLHandler.getSubNode(formatNode, LiteralFormat.DATATYPE));				
		return new LiteralFormat(datatype, supported_uoms, default_uom, default_value);
	}
	
	private BoundingBoxFormat readBoundingBoxFormatFromData(Node formatNode){
		Node supportedCRSsNodes = XMLHandler.getSubNode(formatNode, BoundingBoxFormat.SUPPORTED_CRSS);
		int count = XMLHandler.countNodes(supportedCRSsNodes, "CRS");
		List<String> supported_crss = new ArrayList<String>(count);
		for (int i = 0; i < count; i++) {
			supported_crss.add(XMLHandler.getNodeValue(XMLHandler.getSubNodeByNr(supportedCRSsNodes, "CRS", i)));
		}
		String default_crs = XMLHandler.getNodeValue(XMLHandler.getSubNode(formatNode, BoundingBoxFormat.DEFAULT_CRS));		
		return new BoundingBoxFormat(supported_crss, default_crs);
	}
	
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}
	
	private void readData(Node stepnode)throws KettleXMLException{
		try{
			url        	 					= XMLHandler.getTagValue(stepnode, "url"); //$NON-NLS-1$ 
			method        					= XMLHandler.getTagValue(stepnode, "method"); //$NON-NLS-1$  
			version        					= XMLHandler.getTagValue(stepnode, "version"); //$NON-NLS-1$
			process		    				= XMLHandler.getTagValue(stepnode, "process"); //$NON-NLS-1$ 
			hasRawDataOutput 				= YES.equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "hasRawDataOutput")); //$NON-NLS-1$ 
			processId		    			= XMLHandler.getTagValue(stepnode, "processId"); //$NON-NLS-1$ 
			resultFieldName 				= XMLHandler.getTagValue(stepnode, "resultfieldName"); //$NON-NLS-1$ 	
			
			String describeProcessPOSTURL 	= XMLHandler.getTagValue(stepnode, "describeProcessPOSTURL"); //$NON-NLS-1$ 
			String describeProcessGETURL 	= XMLHandler.getTagValue(stepnode, "describeProcessGETURL"); //$NON-NLS-1$ 
			String executePOSTURL 			= XMLHandler.getTagValue(stepnode, "executePOSTURL"); //$NON-NLS-1$ 
			String executeGETURL 			= XMLHandler.getTagValue(stepnode, "executeGETURL"); //$NON-NLS-1$ 
			
			urls = new HashMap<String, String>(4);
			if(!Const.isEmpty(describeProcessPOSTURL))
				urls.put("describeProcessPOSTURL", describeProcessPOSTURL);
			if(!Const.isEmpty(describeProcessGETURL))
				urls.put("describeProcessGETURL", describeProcessGETURL);
			if(!Const.isEmpty(executePOSTURL))
				urls.put("executePOSTURL", executePOSTURL);
			if(!Const.isEmpty(executeGETURL))
				urls.put("executeGETURL", executeGETURL);
			
			Node inputParametersNode = XMLHandler.getSubNode(stepnode, "inputParameters");			
			int nrInputParameters= XMLHandler.countNodes(inputParametersNode, "parameter");
			allocateInputParameters(nrInputParameters);

			for (int i = 0; i < nrInputParameters; i++) {
				Node inputParameterNode = XMLHandler.getSubNodeByNr(inputParametersNode, "parameter", i);
				inputParameters.put(XMLHandler.getTagValue(inputParameterNode, "name"), buildParameterFromData(inputParameterNode));
			}
			
			Node outputParametersNode = XMLHandler.getSubNode(stepnode, "outputParameters");			
			int nrOutputParameters= XMLHandler.countNodes(outputParametersNode, "parameter");
			allocateOutputParameters(nrOutputParameters);

			for (int i = 0; i < nrOutputParameters; i++) {
				Node outputParameterNode = XMLHandler.getSubNodeByNr(outputParametersNode, "parameter", i);
				outputParameters.put(XMLHandler.getTagValue(outputParameterNode, "name"), buildParameterFromData(outputParameterNode));
			}
			
			Node inputValuesNode = XMLHandler.getSubNode(stepnode, "inputValues");			
			int nrInputValues= XMLHandler.countNodes(inputValuesNode, "parameter");
			allocateInputValues(nrInputValues);

			for (int i = 0; i < nrInputValues; i++) {
				Node inputValueNode = XMLHandler.getSubNodeByNr(inputValuesNode, "parameter", i);
				String paramName = XMLHandler.getTagValue(inputValueNode, "name");				
				inputValues.put(paramName, getSelectedValuesfromData(inputParameters.get(paramName), inputValueNode));
			}
			
			Node outputValuesNode = XMLHandler.getSubNode(stepnode, "outputValues");			
			int nrOutputValues= XMLHandler.countNodes(outputValuesNode, "parameter");
			allocateOutputValues(nrOutputValues);

			for (int i = 0; i < nrOutputValues; i++) {
				Node outputValueNode = XMLHandler.getSubNodeByNr(outputValuesNode, "parameter", i);
				String paramName = XMLHandler.getTagValue(outputValueNode, "name");				
				outputValues.put(paramName, getSelectedValuesfromData(outputParameters.get(paramName), outputValueNode));
			}
		}catch(Exception e){
			throw new KettleXMLException(Messages.getString("WPSClientMeta.Exception.UnableToReadStepInformationFromXML"), e); //$NON-NLS-1$
		}
	}
	
	private Map<String, String> getSelectedValuesfromData(Parameter parameter, Node node){
		Map<String, String> properties = new HashMap<String, String>();
		if(parameter.hasLiteralFormat())
			properties.put(LiteralFormat.SUPPORTED_UOMS, XMLHandler.getTagValue(node, LiteralFormat.SUPPORTED_UOMS));
		else if(parameter.hasBoundingBoxFormat())
			properties.put(BoundingBoxFormat.SUPPORTED_CRSS, XMLHandler.getTagValue(node, BoundingBoxFormat.SUPPORTED_CRSS));
		else{
			properties.put(ComplexFormat.SCHEMA, XMLHandler.getTagValue(node, ComplexFormat.SCHEMA));
			properties.put(ComplexFormat.MIMETYPE, XMLHandler.getTagValue(node, MIMETYPE));
			properties.put(ComplexFormat.ENCODING, XMLHandler.getTagValue(node, ComplexFormat.ENCODING));
		}
		return properties;
	}
	
	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException{
		try{
			url         					= rep.getStepAttributeString (id_step, "url"); //$NON-NLS-1$
			method      					= rep.getStepAttributeString (id_step, "method"); //$NON-NLS-1$
			version    	 					= rep.getStepAttributeString (id_step, "version"); //$NON-NLS-1$
			process							= rep.getStepAttributeString (id_step, "process"); //$NON-NLS-1$	
		
			hasRawDataOutput				= rep.getStepAttributeBoolean (id_step, "hasRawDataOutput");
			processId						= rep.getStepAttributeString (id_step, "processId"); //$NON-NLS-1$
			resultFieldName					= rep.getStepAttributeString (id_step, "resultFieldName"); //$NON-NLS-1$
			
			String describeProcessPOSTURL	= rep.getStepAttributeString (id_step, "describeProcessPOSTURL"); //$NON-NLS-1$
			String describeProcessGETURL	= rep.getStepAttributeString (id_step, "describeProcessGETURL"); //$NON-NLS-1$
			String executePOSTURL			= rep.getStepAttributeString (id_step, "executePOSTURL"); //$NON-NLS-1$
			String executeGETURL			= rep.getStepAttributeString (id_step, "executeGETURL"); //$NON-NLS-1$	
			
			urls = new HashMap<String, String>(4);
			if(!Const.isEmpty(describeProcessPOSTURL))
				urls.put("describeProcessPOSTURL", describeProcessPOSTURL);
			if(!Const.isEmpty(describeProcessGETURL))
				urls.put("describeProcessGETURL", describeProcessGETURL);
			if(!Const.isEmpty(executePOSTURL))
				urls.put("executePOSTURL", executePOSTURL);
			if(!Const.isEmpty(executeGETURL))
				urls.put("executeGETURL", executeGETURL);
			
			String paramType = "inputParameter";
			int nrInputParameters = rep.countNrStepAttributes(id_step, paramType + "name");
			allocateInputParameters(nrInputParameters);		
			for (int i = 0; i < nrInputParameters; i++) {
				String paramName = rep.getStepAttributeString(id_step, i, paramType + "name");
				inputParameters.put(paramName, buildParameterFromRep(rep, id_step, paramType + paramName));
			}
			
			paramType = "inputValue";
			int nrInputValues = rep.countNrStepAttributes(id_step, paramType + "name");
			allocateInputValues(nrInputValues);		
			for (int i = 0; i < nrInputValues; i++) {
				String paramName = rep.getStepAttributeString(id_step, i, paramType + "name");
				inputValues.put(paramName, buildValuesFromRep(true, rep, id_step, paramType + paramName, paramName));
			}
						
			paramType = "outputParameter";
			int nrOutputParameters = rep.countNrStepAttributes(id_step, paramType + "name");
			allocateOutputParameters(nrOutputParameters);		
			for (int i = 0; i < nrOutputParameters; i++) {
				String paramName = rep.getStepAttributeString(id_step, i, paramType + "name");
				outputParameters.put(paramName, buildParameterFromRep(rep, id_step, paramType + paramName));
			}
			
			paramType = "outputValue";
			int nrOutputValues = rep.countNrStepAttributes(id_step, paramType + "name");
			allocateOutputValues(nrOutputValues);		
			for (int i = 0; i < nrOutputValues; i++) {
				String paramName = rep.getStepAttributeString(id_step, i, paramType + "name");
				outputValues.put(paramName, buildValuesFromRep(false, rep, id_step, paramType + paramName, paramName));
			}
		}catch(Exception e){
			throw new KettleException(Messages.getString("WPSClientMeta.Exception.UnexpectedErrorReadingMetaDataFromRepository"), e); //$NON-NLS-1$
		}
	}
	
	private Map<String, String> buildValuesFromRep(boolean isInput, Repository rep, long id_step, String prefix, String paramName) throws KettleException{	
		Map<String, Parameter> parameters = isInput?inputParameters:outputParameters;
		Map<String, String> values = new HashMap<String, String>();
		Parameter parameter = parameters.get(paramName);
		if(parameter.hasLiteralFormat())
			values.put(LiteralFormat.SUPPORTED_UOMS, rep.getStepAttributeString (id_step, prefix + LiteralFormat.SUPPORTED_UOMS));
		else if(parameter.hasBoundingBoxFormat())
			values.put(BoundingBoxFormat.SUPPORTED_CRSS, rep.getStepAttributeString (id_step, prefix + BoundingBoxFormat.SUPPORTED_CRSS));
		else{
			values.put(ComplexFormat.SCHEMA, rep.getStepAttributeString (id_step, prefix + ComplexFormat.SCHEMA));
			values.put(ComplexFormat.MIMETYPE, rep.getStepAttributeString (id_step, prefix + MIMETYPE));
			values.put(ComplexFormat.ENCODING, rep.getStepAttributeString (id_step, prefix + ComplexFormat.ENCODING));
		}	
		return values;
	}
	
	private Parameter buildParameterFromRep(Repository rep, long id_step, String prefix) throws KettleException{
		boolean isMandatory = rep.getStepAttributeBoolean (id_step,  prefix + "isMandatory");
		String id = rep.getStepAttributeString (id_step, prefix + "id");
		String abs = rep.getStepAttributeString (id_step, prefix + "abstract");
		
		Parameter parameter = new Parameter(isMandatory, id, abs);
		parameter.setValue(rep.getStepAttributeString (id_step, prefix + "value"));
		parameter.setFieldName(rep.getStepAttributeString (id_step, prefix + "fieldName"));
		
		boolean hasLiteralFormat = rep.getStepAttributeBoolean (id_step, prefix + "hasLiteralFormat");
		boolean hasBoundingBoxFormat = rep.getStepAttributeBoolean (id_step, prefix + "hasBoundingBoxFormat");
		
		AbstractFormat format;

		if(hasLiteralFormat || hasBoundingBoxFormat){	
			if(hasLiteralFormat){
				format = readLiteralFormatFromRep(rep, id_step, prefix);						
				parameter.setHasLiteralFormat(hasLiteralFormat);
			}else{
				format = readBoundingBoxFormatFromRep(rep, id_step, prefix);						
				parameter.setHasBoundingBoxFormat(hasBoundingBoxFormat);
			}									
			parameter.addSupportedFormat(format);
			parameter.setDefaultFormat(format);							
		}else{///// Complex Format //////	
			for (int i = 0; i < rep.countNrStepAttributes(id_step, prefix + MIMETYPE); i++) {
				parameter.addSupportedFormat(readComplexFormatFromRep(rep, id_step, i, prefix));
			}		
			parameter.setDefaultFormat(readComplexFormatFromRep(rep, id_step, 0, prefix + "defaultFormat"));				
		}
		
		return parameter;
	}
	
	private LiteralFormat readLiteralFormatFromRep(Repository rep, long id_step, String prefix) throws KettleException{
		List<String> supported_uoms = new ArrayList<String>();
		for (int i = 0; i < rep.countNrStepAttributes(id_step, prefix + "UOM"); i++) {			
			supported_uoms.add(rep.getStepAttributeString(id_step, i, prefix + "UOM"));		
		}		
		String default_uom = rep.getStepAttributeString(id_step, prefix + LiteralFormat.DEFAULT_UOM);
		String default_value = rep.getStepAttributeString(id_step, prefix + LiteralFormat.DEFAULT_VALUE);
		String datatype = rep.getStepAttributeString(id_step, prefix + LiteralFormat.DATATYPE);	
		return new LiteralFormat(datatype, supported_uoms, default_uom, default_value);
	}
	
	private BoundingBoxFormat readBoundingBoxFormatFromRep(Repository rep, long id_step, String prefix) throws KettleException{
		List<String> supported_crss = new ArrayList<String>();
		for (int i = 0; i < rep.countNrStepAttributes(id_step, prefix + "CRS"); i++) {			
			supported_crss.add(rep.getStepAttributeString(id_step, i, prefix + "CRS"));		
		}
		String default_crs = rep.getStepAttributeString(id_step, prefix + BoundingBoxFormat.DEFAULT_CRS);	
		return new BoundingBoxFormat(supported_crss, default_crs);
	}
	
	private ComplexFormat readComplexFormatFromRep(Repository rep, long id_step, int i, String prefix) throws KettleException{
		String mime_type = rep.getStepAttributeString(id_step, i, prefix + MIMETYPE);						
		String encoding = rep.getStepAttributeString(id_step, i, prefix + ComplexFormat.ENCODING);
		String schema = rep.getStepAttributeString(id_step, i, prefix + ComplexFormat.SCHEMA);
		return new ComplexFormat(mime_type, encoding, schema);
	}
	
	public void setDefault(){
		url = null;
		version = null;
		method = "GET";
		process = null;
		hasRawDataOutput = false;
		resultFieldName = "";
		urls = new HashMap<String, String>(4);
	}
	
	public Object clone(){
		return (WPSClientMeta)super.clone();
	}	
		
	public String getXML(){
		StringBuffer retval = new StringBuffer();
		retval.append("    " + XMLHandler.addTagValue("url",   					url)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("method", 				method)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("version", 				version)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("process", 				process)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("hasRawDataOutput", 		hasRawDataOutput)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("processId", 				processId)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("resultFieldName", 		resultFieldName)); //$NON-NLS-1$ //$NON-NLS-2$
        
		if(urls.containsKey("describeProcessPOSTURL"))
			retval.append("    " + XMLHandler.addTagValue("describeProcessPOSTURL", urls.get("describeProcessPOSTURL"))); //$NON-NLS-1$ //$NON-NLS-2$
		if(urls.containsKey("describeProcessGETURL"))
			retval.append("    " + XMLHandler.addTagValue("describeProcessGETURL", urls.get("describeProcessGETURL"))); //$NON-NLS-1$ //$NON-NLS-2$
		if(urls.containsKey("executePOSTURL"))
			retval.append("    " + XMLHandler.addTagValue("executePOSTURL", urls.get("executePOSTURL"))); //$NON-NLS-1$ //$NON-NLS-2$
		if(urls.containsKey("executeGETURL"))
			retval.append("    " + XMLHandler.addTagValue("executeGETURL", urls.get("executeGETURL"))); //$NON-NLS-1$ //$NON-NLS-2$

        if (inputParameters!=null)
        	retval.append(getXMLInputParameters(inputParameters));
        
        if (inputValues!=null)
        	retval.append(getXMLValues(inputValues, "inputValues"));
        
        if (outputParameters!=null)
        	retval.append(getXMLOutputParameters(outputParameters));
        
        if (outputValues!=null)
        	retval.append(getXMLValues(outputValues, "outputValues"));
		
		return retval.toString();
	}
    
	private String getXMLValues(Map<String, Map<String, String>> values, String rootTag){
    	StringBuffer retval = new StringBuffer();
    	retval.append("    <" + rootTag + ">").append(Const.CR);    	
    	for (String paramName : values.keySet()) { 
    		retval.append("      <parameter>").append(Const.CR);
    		retval.append("        ").append(XMLHandler.addTagValue("name", paramName));
    		Map<String, String> properties = values.get(paramName);
    		for (String propertyName : properties.keySet()) { 
        		retval.append("        ").append(XMLHandler.addTagValue(propertyName.equals(ComplexFormat.MIMETYPE)?MIMETYPE:propertyName, properties.get(propertyName)));  
        	}
    		retval.append("      </parameter>").append(Const.CR);
    	}   	
    	retval.append("    </" + rootTag + ">").append(Const.CR);
    	return retval.toString();
    }
	
	@SuppressWarnings("unchecked")
	private String getXMLParameter(String parameterName, Parameter parameter){
		StringBuffer retval = new StringBuffer();
		retval.append("      <parameter>").append(Const.CR);
		retval.append("        ").append(XMLHandler.addTagValue("name", parameterName));
		retval.append("        ").append(XMLHandler.addTagValue("id", parameter.getId()));
		retval.append("        ").append(XMLHandler.addTagValue("abstract", parameter.getAbstract()));
		retval.append("        ").append(XMLHandler.addTagValue("value", parameter.getValue()));
		retval.append("        ").append(XMLHandler.addTagValue("fieldName", parameter.getFieldName()));
		retval.append("        ").append(XMLHandler.addTagValue("isMandatory", parameter.isMandatory()));
		retval.append("        ").append(XMLHandler.addTagValue("hasLiteralFormat", parameter.hasLiteralFormat()));
		retval.append("        ").append(XMLHandler.addTagValue("hasBoundingBoxFormat", parameter.hasBoundingBoxFormat()));
		
		Map<String, Object> properties;
	
		AbstractFormat default_format = parameter.getDefaultFormat();
		if(default_format != null){
			retval.append("        <defaultFormat>").append(Const.CR);
			if(parameter.hasLiteralFormat()){
				properties = default_format.getProperties();
				if(properties.containsKey(LiteralFormat.SUPPORTED_UOMS)){
					retval.append("          <supportedUOMs>").append(Const.CR);
					for(String supported_uom: (List<String>) properties.get(LiteralFormat.SUPPORTED_UOMS)){
						if(!Const.isEmpty(supported_uom))
							retval.append("          ").append(XMLHandler.addTagValue("UOM", supported_uom));
	    			}
					retval.append("          </supportedUOMs>").append(Const.CR);
				} 
				if(properties.containsKey(LiteralFormat.DEFAULT_UOM))
					retval.append("          ").append(XMLHandler.addTagValue(LiteralFormat.DEFAULT_UOM, properties.get(LiteralFormat.DEFAULT_UOM)!=null?properties.get(LiteralFormat.DEFAULT_UOM).toString():""));
				if(properties.containsKey(LiteralFormat.DATATYPE))
					retval.append("          ").append(XMLHandler.addTagValue(LiteralFormat.DATATYPE, properties.get(LiteralFormat.DATATYPE)!=null?properties.get(LiteralFormat.DATATYPE).toString():""));
				if(properties.containsKey(LiteralFormat.DEFAULT_VALUE))
					retval.append("          ").append(XMLHandler.addTagValue(LiteralFormat.DEFAULT_VALUE, properties.get(LiteralFormat.DEFAULT_VALUE)!=null?properties.get(LiteralFormat.DEFAULT_VALUE).toString():""));
				retval.append("        </defaultFormat>").append(Const.CR);
			}else if(parameter.hasBoundingBoxFormat()){
				properties = default_format.getProperties();
				if(properties.containsKey(BoundingBoxFormat.SUPPORTED_CRSS)){
					retval.append("        <supportedCRSs>").append(Const.CR);
					for(String supported_crs: (List<String>) properties.get(BoundingBoxFormat.SUPPORTED_CRSS)){
						if(!Const.isEmpty(supported_crs))
							retval.append("        ").append(XMLHandler.addTagValue("CRS", supported_crs));
	    			}
					retval.append("        </supportedCRSs>").append(Const.CR);
				} 
				if(properties.containsKey(BoundingBoxFormat.DEFAULT_CRS))
					retval.append("        ").append(XMLHandler.addTagValue(BoundingBoxFormat.DEFAULT_CRS, properties.get(BoundingBoxFormat.DEFAULT_CRS)!=null?properties.get(BoundingBoxFormat.DEFAULT_CRS).toString():""));
				retval.append("        </defaultFormat>").append(Const.CR);
			}else{
				retval.append(getXMLComplexFormat(default_format.getProperties()));
				retval.append("        </defaultFormat>").append(Const.CR);
				retval.append("        <supportedFormats>").append(Const.CR);
				for(AbstractFormat supported_format: parameter.getSupportedFormats()){
					if(supported_format != null){
						retval.append("          <format>").append(Const.CR);
						retval.append(getXMLComplexFormat(supported_format.getProperties()));
						retval.append("          </format>").append(Const.CR);
					}
				}
				retval.append("        </supportedFormats>").append(Const.CR);
			}
		}
		
		retval.append("      </parameter>").append(Const.CR);
		return retval.toString();
	}
	
	private String getXMLInputParameters(Map<String, Parameter> parameters){
    	StringBuffer retval = new StringBuffer();
    	retval.append("    <inputParameters>").append(Const.CR);
    	for (String parameterName : parameters.keySet()) {
    		retval.append(getXMLParameter(parameterName, parameters.get(parameterName)));
    	}
    	retval.append("    </inputParameters>").append(Const.CR);
    	return retval.toString();
    }
	
	
    private String getXMLOutputParameters(Map<String, Parameter> parameters){
    	StringBuffer retval = new StringBuffer();
    	retval.append("    <outputParameters>").append(Const.CR);
    	for (String parameterName : parameters.keySet()) {
    		retval.append(getXMLParameter(parameterName, parameters.get(parameterName)));
    	}
    	retval.append("    </outputParameters>").append(Const.CR);
    	return retval.toString();
    }
    
	private String getXMLComplexFormat(Map<String, Object> properties){
		StringBuffer retval = new StringBuffer();
		if(properties.containsKey(ComplexFormat.MIMETYPE) && properties.get(ComplexFormat.MIMETYPE) != null)
			retval.append("            ").append(XMLHandler.addTagValue(MIMETYPE, properties.get(ComplexFormat.MIMETYPE).toString()));
		if(properties.containsKey(ComplexFormat.SCHEMA) && properties.get(ComplexFormat.SCHEMA) != null)
			retval.append("            ").append(XMLHandler.addTagValue(ComplexFormat.SCHEMA, properties.get(ComplexFormat.SCHEMA).toString()));
		if(properties.containsKey(ComplexFormat.ENCODING) && properties.get(ComplexFormat.ENCODING) != null)
			retval.append("            ").append(XMLHandler.addTagValue(ComplexFormat.ENCODING, properties.get(ComplexFormat.ENCODING).toString()));
		return retval.toString();
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)throws KettleException{
		try{
			rep.saveStepAttribute(id_transformation, id_step, "url",       				url); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "method",   				method); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "version",   				version); //$NON-NLS-1$	
			rep.saveStepAttribute(id_transformation, id_step, "process",   				process); //$NON-NLS-1$					
			rep.saveStepAttribute(id_transformation, id_step, "hasRawDataOutput",   	hasRawDataOutput); //$NON-NLS-1$	
			rep.saveStepAttribute(id_transformation, id_step, "processId",   			processId); //$NON-NLS-1$	
			rep.saveStepAttribute(id_transformation, id_step, "resultFieldName",   		resultFieldName); //$NON-NLS-1$	
			
			if(urls.containsKey("describeProcessPOSTURL"))
				rep.saveStepAttribute(id_transformation, id_step, "describeProcessPOSTURL", urls.get("describeProcessPOSTURL")); //$NON-NLS-1$	
			if(urls.containsKey("describeProcessGETURL"))
				rep.saveStepAttribute(id_transformation, id_step, "describeProcessGETURL", urls.get("describeProcessGETURL")); //$NON-NLS-1$
			if(urls.containsKey("executePOSTURL"))
				rep.saveStepAttribute(id_transformation, id_step, "executePOSTURL", urls.get("executePOSTURL")); //$NON-NLS-1$
			if(urls.containsKey("executeGETURL"))
				rep.saveStepAttribute(id_transformation, id_step, "executeGETURL", urls.get("executeGETURL")); //$NON-NLS-1$
			
			int index;
			String paramType;
			
			if (inputParameters!=null){
				index = 0;
				paramType = "inputParameter";
				for(String key : inputParameters.keySet()){		
					rep.saveStepAttribute(id_transformation, id_step, index, paramType + "name", key);
					saveParameterToRep(rep, id_transformation, id_step, inputParameters.get(key), paramType + key);
					index++;
				}
			}
			
			if (inputValues!=null){
				index = 0;
				paramType = "inputValue";
				for(String key : inputValues.keySet()){		
					rep.saveStepAttribute(id_transformation, id_step, index, paramType + "name", key);
					saveValueToRep(rep, id_transformation, id_step, inputValues.get(key), paramType + key);
					index++;
				}
			}
			
			if (outputParameters!=null){
				index = 0;
				paramType = "outputParameter";
				for(String key : outputParameters.keySet()){		
					rep.saveStepAttribute(id_transformation, id_step, index, paramType + "name", key);
					saveParameterToRep(rep, id_transformation, id_step, outputParameters.get(key), paramType + key);
					index++;
				}
			}
			
			if (outputValues!=null){
				index = 0;
				paramType = "outputValue";
				for(String key : outputValues.keySet()){		
					rep.saveStepAttribute(id_transformation, id_step, index, paramType + "name", key);
					saveValueToRep(rep, id_transformation, id_step, outputValues.get(key), paramType + key);
					index++;
				}
			}
		}catch(Exception e){
			throw new KettleException(Messages.getString("WPSClientMeta.Exception.UnableToSaveMetaDataToRepository")+id_step, e); //$NON-NLS-1$
		}
	}
	
	private void saveValueToRep(Repository rep, long id_transformation, long id_step, Map<String, String> values, String prefix) throws KettleException{	
		for(String propertyName: values.keySet()){
			rep.saveStepAttribute(id_transformation, id_step, prefix + (propertyName.equals(ComplexFormat.MIMETYPE)?MIMETYPE:propertyName), values.get(propertyName));
		}
	}
	
	private void saveParameterToRep(Repository rep, long id_transformation, long id_step, Parameter parameter, String prefix) throws KettleException{	
		rep.saveStepAttribute(id_transformation, id_step, prefix + "id", parameter.getId());
		rep.saveStepAttribute(id_transformation, id_step, prefix + "abstract", parameter.getAbstract());
		rep.saveStepAttribute(id_transformation, id_step, prefix + "isMandatory", parameter.isMandatory());
		rep.saveStepAttribute(id_transformation, id_step, prefix + "value", parameter.getValue());
		rep.saveStepAttribute(id_transformation, id_step, prefix + "fieldName", parameter.getFieldName());
		rep.saveStepAttribute(id_transformation, id_step, prefix + "hasLiteralFormat", parameter.hasLiteralFormat());
		rep.saveStepAttribute(id_transformation, id_step, prefix + "hasBoundingBoxFormat", parameter.hasBoundingBoxFormat());
		if(parameter.hasLiteralFormat()){
			saveLiteralFormatToRep(rep, id_transformation, id_step, parameter.getDefaultFormat().getProperties(), prefix);
		}else if(parameter.hasBoundingBoxFormat()){
			saveBoundingBoxFormatToRep(rep, id_transformation, id_step, parameter.getDefaultFormat().getProperties(), prefix);
		}else{
			saveComplexFormatToRep(rep, id_transformation, id_step, parameter, prefix);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void saveLiteralFormatToRep(Repository rep, long id_transformation, long id_step, Map<String, Object> properties, String prefix) throws KettleException{	
		int i  = 0;
		for (String uom: (List<String>)properties.get(LiteralFormat.SUPPORTED_UOMS)) {			
			rep.saveStepAttribute(id_transformation, id_step, i, prefix + "UOM", uom);
			i++;
		}
		rep.saveStepAttribute(id_transformation, id_step, prefix + LiteralFormat.DEFAULT_UOM, (String) properties.get(LiteralFormat.DEFAULT_UOM));
		rep.saveStepAttribute(id_transformation, id_step, prefix + LiteralFormat.DEFAULT_VALUE, (String) properties.get(LiteralFormat.DEFAULT_VALUE));
		rep.saveStepAttribute(id_transformation, id_step, prefix + LiteralFormat.DATATYPE, (String) properties.get(LiteralFormat.DATATYPE));
	}
	
	@SuppressWarnings("unchecked")
	private void saveBoundingBoxFormatToRep(Repository rep, long id_transformation, long id_step, Map<String, Object> properties, String prefix) throws KettleException{	
		int i  = 0;
		for (String crs: (List<String>) properties.get(BoundingBoxFormat.SUPPORTED_CRSS)) {			
			rep.saveStepAttribute(id_transformation, id_step, i, prefix + "CRS", crs);
			i++;
		}
		rep.saveStepAttribute(id_transformation, id_step, prefix + BoundingBoxFormat.DEFAULT_CRS, (String) properties.get(BoundingBoxFormat.DEFAULT_CRS));
	}
	
	private void saveComplexFormatToRep(Repository rep, long id_transformation, long id_step, Parameter parameter, String prefix) throws KettleException{	
		int i  = 0;
		for(AbstractFormat format: parameter.getSupportedFormats()){
			rep.saveStepAttribute(id_transformation, id_step, i, prefix +  MIMETYPE, (String) format.getProperties().get(ComplexFormat.MIMETYPE));
			rep.saveStepAttribute(id_transformation, id_step, i, prefix + ComplexFormat.ENCODING, (String) format.getProperties().get(ComplexFormat.ENCODING));
			rep.saveStepAttribute(id_transformation, id_step, i, prefix + ComplexFormat.SCHEMA, (String) format.getProperties().get(ComplexFormat.SCHEMA));
			i++;
		}		
		AbstractFormat defaultFormat = parameter.getDefaultFormat();
		String defaultPrefix = prefix + "defaultFormat";
		rep.saveStepAttribute(id_transformation, id_step, defaultPrefix +  MIMETYPE, (String) defaultFormat.getProperties().get(ComplexFormat.MIMETYPE));
		rep.saveStepAttribute(id_transformation, id_step, defaultPrefix + ComplexFormat.ENCODING, (String) defaultFormat.getProperties().get(ComplexFormat.ENCODING));
		rep.saveStepAttribute(id_transformation, id_step, defaultPrefix + ComplexFormat.SCHEMA, (String) defaultFormat.getProperties().get(ComplexFormat.SCHEMA));
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info){
		CheckResult cr;

	    if (Const.isEmpty(url)){
	    	cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("WPSClientMeta.CheckResult.NoURLSpecified"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
	    }
	    
	    if (Const.isEmpty(version)){
	    	cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("WPSClientMeta.CheckResult.NoVersionSpecified"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
	    }
	    
	    if (Const.isEmpty(method)){
	    	cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("WPSClientMeta.CheckResult.NoMethodSpecified"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
	    }	
	    
	    if (Const.isEmpty(process)){
	    	cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("WPSClientMeta.CheckResult.NoProcessSpecified"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
	    }
	 
        if (input.length > 0){
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("WPSClientMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }else{
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("WPSClientMeta.CheckResult.NoInputReceived"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }

        remarks.add(cr);
	}
    
    public void getFields(RowMetaInterface row, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space){
        row.addValueMeta(new ValueMeta(!Const.isEmpty(resultFieldName)?resultFieldName:"result", ValueMeta.TYPE_STRING));
    }
}
