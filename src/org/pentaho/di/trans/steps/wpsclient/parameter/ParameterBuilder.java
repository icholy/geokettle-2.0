package org.pentaho.di.trans.steps.wpsclient.parameter;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.jdom.JDOMException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.wpsclient.parameter.format.AbstractFormat;
import org.pentaho.di.trans.steps.wpsclient.parameter.format.BoundingBoxFormat;
import org.pentaho.di.trans.steps.wpsclient.parameter.format.ComplexFormat;
import org.pentaho.di.trans.steps.wpsclient.parameter.format.LiteralFormat;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

public class ParameterBuilder {
	private static final String INPUT_EXPR = "//*[local-name() = 'Input']";
	private static final String OUTPUT_EXPR = "//*[local-name() = 'Output']";	
	private static final String COMPLEX_INPUT_EXPR = ".//*[local-name() = 'ComplexData']";
	private static final String LITERAL_INPUT_EXPR = ".//*[local-name() = 'LiteralData']";
	private static final String BOUNDINGBOX_INPUT_EXPR = ".//*[local-name() = 'BoundingBoxData']";
	private static final String COMPLEX_OUTPUT_EXPR = ".//*[local-name() = 'ComplexOutput']";
	private static final String LITERAL_OUTPUT_EXPR = ".//*[local-name() = 'LiteralOutput']";
	private static final String BOUNDINGBOX_OUTPUT_EXPR = ".//*[local-name() = 'BoundingBoxOutput']";
	private static final String SUPPORTED_FORMATS_EXPR = "./*[local-name() = 'Supported']//*[local-name() = 'Format']";
	private static final String DEFAULT_FORMAT_EXPR = "./*[local-name() = 'Default']/*[local-name() = 'Format']";
	private static final String NAME_EXPR = "./*[local-name() = 'Title']";
	private static final String ID_EXPR = "./*[local-name() = 'Identifier']";
	private static final String ABSTRACT_EXPR = "./*[local-name() = 'Abstract']";
	private static final String MIMETYPE_EXPR = "./*[local-name() = 'MimeType']";
	private static final String SCHEMA_EXPR = "./*[local-name() = 'Schema']";
	private static final String ENCODING_EXPR = "./*[local-name() = 'Encoding']";
	private static final String DATATYPE_EXPR = "./*[local-name() = 'DataType']";
	private static final String SUPPORTED_CRSS_EXPR = "./*[local-name() = 'CRSs']/*[local-name() = 'Supported']//*[local-name() = 'CRS']";
	private static final String DEFAULT_CRS_EXPR = "./*[local-name() = 'CRSs']/*[local-name() = 'Default']/*[local-name() = 'CRS']";
	private static final String SUPPORTED_UOMS_EXPR = "./*[local-name() = 'UOMs']/*[local-name() = 'Supported']//*[local-name() = 'UOM']";
	private static final String DEFAULT_UOM_EXPR = "./*[local-name() = 'UOMs']/*[local-name() = 'Default']/*[local-name() = 'UOM']";
	private static final String DEFAULT_VALUE_EXPR = "./*[local-name() = 'DefaultValue']";
	
	private static final String MINOCCURS = "minOccurs";
	private static final String MAXOCCURS = "maxOccurs";
	
	private static final String EMPTY = "";
	
	private String root_expr;
	private String complex_expr;
	private String literal_expr;
	private String bbox_expr;
	
	public ParameterBuilder(boolean isInput){
		if(isInput){
			root_expr = INPUT_EXPR;
			complex_expr = COMPLEX_INPUT_EXPR;
			literal_expr = LITERAL_INPUT_EXPR;
			bbox_expr = BOUNDINGBOX_INPUT_EXPR;
		}else{
			root_expr = OUTPUT_EXPR;
			complex_expr = COMPLEX_OUTPUT_EXPR;
			literal_expr = LITERAL_OUTPUT_EXPR;
			bbox_expr = BOUNDINGBOX_OUTPUT_EXPR;
		}
	}

	public Map<String, Parameter> parseParameters(String response) throws KettleException{ 
		Map<String, Parameter> parameters = new HashMap<String, Parameter>(); 
		try{
		    DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		    dfactory.setNamespaceAware(false);
		    Document doc = dfactory.newDocumentBuilder().parse(new InputSource(new StringReader(response)));
		    	      
		    Node n;
		    NodeIterator nodeIt = XPathAPI.selectNodeIterator(doc, root_expr);
		    while ((n = nodeIt.nextNode())!= null){ 	    	
		    	parameters.put(XPathAPI.eval(n, NAME_EXPR).str(), parseParameter(n));
		    }
		} catch (Exception e) {
			throw new KettleException("Error retrieving parameters from WPS response...", e);
		}   
		return parameters;
	}
	
	private AbstractFormat buildLiteralFormat(Node n) throws TransformerException, JDOMException, KettleException{		
		String datatype = XPathAPI.eval(n, DATATYPE_EXPR).str();			
		if(datatype == null)
			datatype = EMPTY;
		List<String> uoms = new ArrayList<String>();
		
		String default_uom = XPathAPI.eval(n, DEFAULT_UOM_EXPR).str();
		if(default_uom == null)
			default_uom = EMPTY;
		
		Node uom_node;
		NodeIterator nodeIt = XPathAPI.selectNodeIterator(n, SUPPORTED_UOMS_EXPR);
	    while ((uom_node = nodeIt.nextNode())!= null){ 
	    	if(uom_node.getTextContent() != null)
	    		uoms.add(uom_node.getTextContent());
	    }	
	    
	    boolean alreadyExists = false;
	    for(String uom: uoms){
	    	if(uom.equals(default_uom)){
	    		alreadyExists = true;
	    		break;
	    	}			
	    }

	    if(!alreadyExists && !default_uom.equals(EMPTY))
	    	uoms.add(default_uom);
	    
		return new LiteralFormat(datatype, uoms, default_uom, XPathAPI.eval(n, DEFAULT_VALUE_EXPR).str());
	}
	
	private AbstractFormat buildBoundingBoxFormat(Node n) throws JDOMException, KettleException, DOMException, TransformerException{
		List<String> crss = new ArrayList<String>();
		String default_crs = XPathAPI.eval(n, DEFAULT_CRS_EXPR).str();
		if(default_crs == null)
			default_crs = EMPTY;
		
		Node crs_node;
		NodeIterator nodeIt = XPathAPI.selectNodeIterator(n, SUPPORTED_CRSS_EXPR);
	    while ((crs_node = nodeIt.nextNode())!= null){ 	    	
	    	crss.add(crs_node.getNodeValue());
	    }	
	    
	    boolean alreadyExists = false;
	    for(String crs: crss){
	    	if(crs.equals(default_crs)){
	    		alreadyExists = true;
	    		break;
	    	}			
	    }

	    if(!alreadyExists && !default_crs.equals(EMPTY))
	    	crss.add(default_crs);
	    return new BoundingBoxFormat(crss, default_crs);
	}
	
	private void getComplexFormats(Parameter param, Node n) throws JDOMException, KettleException, DOMException, TransformerException{		
		Node complex_default =  XPathAPI.selectSingleNode(n, DEFAULT_FORMAT_EXPR);
		AbstractFormat defaultFormat = null;
		if(complex_default!= null){ 	    	
	    	defaultFormat = buildComplexFormat(complex_default);
			param.setDefaultFormat(defaultFormat);
	    }

	    Node complex_supported;
	    NodeIterator nodeIt = XPathAPI.selectNodeIterator(n, SUPPORTED_FORMATS_EXPR);
	    while ((complex_supported = nodeIt.nextNode())!= null){ 	    	
	    	param.addSupportedFormat(buildComplexFormat(complex_supported));
	    }
	    
	    if(param.getSupportedFormats().isEmpty() && defaultFormat != null)
	    	param.addSupportedFormat(defaultFormat);	    	
	}
	
	private void getLiteralFormats(Parameter param, Node n) throws JDOMException, KettleException, TransformerException{
		param.setHasLiteralFormat(true);		
		param.setDefaultFormat(buildLiteralFormat(n));
	}
	
	private void getBoundingBoxFormats(Parameter param, Node n) throws JDOMException, KettleException, DOMException, TransformerException{
		param.setHasBoundingBoxFormat(true);
		param.setDefaultFormat(buildBoundingBoxFormat(n));
	}

	private Parameter buildParameter(Node n) throws JDOMException, KettleException, TransformerException{		
		return new Parameter(checkMandatory(n), XPathAPI.eval(n, ID_EXPR).str(), XPathAPI.eval(n, ABSTRACT_EXPR).str());
	}
	
	private boolean checkMandatory(Node n){	
		boolean isMandatory = false;
		Element el = (Element) n;
		String minOccurs = el.getAttribute(MINOCCURS);
		String maxOccurs = el.getAttribute(MAXOCCURS);
		if(!Const.isEmpty(minOccurs) && !Const.isEmpty(maxOccurs) && minOccurs.equals("1") && maxOccurs.equals("1"))
			isMandatory = true;	
		return isMandatory;
	}
	
	private Parameter parseParameter(Node n) throws JDOMException, KettleException, TransformerException{
		Parameter param = buildParameter(n);	
		Node complex_node = XPathAPI.selectSingleNode(n, complex_expr);
		if (complex_node != null)
			getComplexFormats(param, complex_node);	
		else{		
			Node bbox_node =  XPathAPI.selectSingleNode(n, bbox_expr);	
			if (bbox_node != null)
				getBoundingBoxFormats(param, bbox_node);	
			else{		
				Node literal_node =  XPathAPI.selectSingleNode(n, literal_expr);	
				if (literal_node != null)
					getLiteralFormats(param, literal_node);							
			}
	    }	
		return param;
	}
		
	private AbstractFormat buildComplexFormat(Node n) throws JDOMException, KettleException, TransformerException{				
		return new ComplexFormat(XPathAPI.eval(n, MIMETYPE_EXPR).str(), XPathAPI.eval(n, ENCODING_EXPR).str(), XPathAPI.eval(n, SCHEMA_EXPR).str());
	}
}
