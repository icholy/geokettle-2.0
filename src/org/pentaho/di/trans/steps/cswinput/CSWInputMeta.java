/**
 * 
 */
package org.pentaho.di.trans.steps.cswinput;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.jdom.Element;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
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
import org.pentaho.di.trans.steps.sosinput.Messages;


import org.w3c.dom.Node;

/**
 * @author O.Mamadou
 *
 */
public class CSWInputMeta extends BaseStepMeta implements StepMetaInterface {
	
	public static final String DEFAULT_PROFILE = "http://www.opengis.net/cat/csw/2.0.2";
	private static final String CSWBRIEFRECORD = "csw:BriefRecord";
	private static final String CSWSUMMARYRECORD = "csw:SummaryRecord";
	private static final String CSWFULLRECORD = "csw:Record";
	public static final String ISOTC211_2005_PROFILE = "http://www.isotc211.org/2005/gmd";
	private static final String GMD_MD_Metadata = "gmd:MD_Metadata";
	
	private CSWReader cswParam;
	private RowMetaInterface fieds;
	//private String keyword;
	
	

	/**
	 * 
	 */
	public CSWInputMeta() {
		// TODO Auto-generated constructor stub
		super();
		cswParam= new CSWReader();
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#check(java.util.List, org.pentaho.di.trans.TransMeta, org.pentaho.di.trans.step.StepMeta, org.pentaho.di.core.row.RowMetaInterface, java.lang.String[], java.lang.String[], org.pentaho.di.core.row.RowMetaInterface)
	 */
	@Override
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
			StepMeta stepMeta, RowMetaInterface prev, String[] input,
			String[] output, RowMetaInterface info) {
		// 
		CheckResult cr;
		if (Const.isEmpty(cswParam.getCatalogUrl().toString())){
	    	cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SOSInputMeta.CheckResult.NoURLSpecified"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
	    }
	    if (Const.isEmpty(cswParam.getVersion())){
	    	cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SOSInputMeta.CheckResult.NoSOSVersionSpecified"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
	    }
	    if (Const.isEmpty(cswParam.getConstraintLanguage())){
	    	cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SOSInputMeta.CheckResult.NoSOSVersionSpecified"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
	    }
	    if (input.length > 0){
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SOSInputMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }else{
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SOSInputMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }

	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#getStep(org.pentaho.di.trans.step.StepMeta, org.pentaho.di.trans.step.StepDataInterface, int, org.pentaho.di.trans.TransMeta, org.pentaho.di.trans.Trans)
	 */
	@Override
	public StepInterface getStep(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans) {
		
		return new CSWInput(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#getStepData()
	 */
	@Override
	public StepDataInterface getStepData() {
		// 
		 return new CSWInputData();
	}
	
	public Object clone(){
		CSWInputMeta retval = (CSWInputMeta)super.clone();

		return retval;
	}	
	
	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#loadXML(org.w3c.dom.Node, java.util.List, java.util.Map)
	 */
	@Override
	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			Map<String, Counter> counters) throws KettleXMLException {
		
		readData(stepnode);

	}
	
	private void readData(Node stepnode)throws KettleXMLException{
		try{
			cswParam.setCatalogUrl(XMLHandler.getTagValue(stepnode, "url")) ; //$NON-NLS-1$ 
			cswParam.setMethod(XMLHandler.getTagValue(stepnode, "method"));
			cswParam.setVersion(XMLHandler.getTagValue(stepnode, "cswVersion")) ;
			cswParam.setKeyword(XMLHandler.getTagValue(stepnode, "keyword"));
			cswParam.setElementSet(XMLHandler.getTagValue(stepnode, "elementset"));
			cswParam.setStartDate(XMLHandler.getTagValue(stepnode, "startdate"));
			cswParam.setEndDate(XMLHandler.getTagValue(stepnode, "enddate"));
			cswParam.setSimpleSearch("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "simplesearch")));
			cswParam.setUseLoginService("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "activatelogin")));
			
			cswParam.setUsername(XMLHandler.getTagValue(stepnode, "username"));
			cswParam.setPassword(XMLHandler.getTagValue(stepnode, "password"));
			cswParam.setLoginServiceUrl(XMLHandler.getTagValue(stepnode, "loginurl"));
			cswParam.setOutputSchema(XMLHandler.getTagValue(stepnode, "outputschema"));
			cswParam.setTitle(XMLHandler.getTagValue(stepnode, "title"));
			cswParam.setSpatialOperator(XMLHandler.getTagValue(stepnode, "spatialoperator"));
			
			cswParam.setConstraintLanguage(XMLHandler.getTagValue(stepnode, "constraintlanguage"));
			
			
			try{
				cswParam.setStartPosition(Integer.parseInt(XMLHandler.getTagValue(stepnode, "startposition")));
			}catch(NumberFormatException e){
				cswParam.setStartPosition(1);
				//e.printStackTrace();
			}
			try{
				cswParam.setMaxRecords(Integer.parseInt(XMLHandler.getTagValue(stepnode, "maxrecords")));
			}catch(NumberFormatException e){
				cswParam.setMaxRecords(10);
				//e.printStackTrace();
			}
			
			
			Iterator<String> it= cswParam.getBBOX().keySet().iterator();
			HashMap<String, Double> bbox=new HashMap<String, Double>();
			while (it.hasNext()){
				String courant=it.next();
				String chaine=XMLHandler.getTagValue(stepnode, "BBOX_"+courant);
				//
				bbox.put(courant, Double.parseDouble(chaine));
				
			}
			cswParam.setBBOX(bbox);			
			///
			Node queriesNode = XMLHandler.getSubNode(stepnode, "queries");
			int nrQuery = XMLHandler.countNodes(queriesNode, "query");

			//
			ArrayList<String[]> queryList= new ArrayList<String[]>();
			

			for (int i = 0; i < nrQuery; i++) {
				String[] s=new String[3];
				Node onode = XMLHandler.getSubNodeByNr(queriesNode, "query", i);
				s[0]=XMLHandler.getTagValue(onode, "operand");
				s[1]=XMLHandler.getTagValue(onode, "operator");
				s[2]=XMLHandler.getTagValue(onode, "value");
				queryList.add(s);
				
			}
			
			cswParam.setAdvancedRequestParam(queryList);
			///
			
			Node outputlistNode = XMLHandler.getSubNode(stepnode, "outputschemalist");
			int nrOutput = XMLHandler.countNodes(outputlistNode, "outputschema");

			//
			String[] outputSchemaList= new String[nrOutput];
			for (int i = 0; i < nrOutput; i++) {				
				Node onode = XMLHandler.getSubNodeByNr(outputlistNode, "outputschema", i);
				String s=XMLHandler.getTagValue(onode, "id");				
				outputSchemaList[i]=s;				
			}
			cswParam.setOutputSchemaList(outputSchemaList);
			
			//query element list
			Node queryElementlistNode = XMLHandler.getSubNode(stepnode, "queryelementlist");
			int nrqueryelement = XMLHandler.countNodes(queryElementlistNode, "queryelement");

			//
			String[] queryElementList= new String[nrqueryelement];
			for (int i = 0; i < nrqueryelement; i++) {				
				Node onode = XMLHandler.getSubNodeByNr(queryElementlistNode, "queryelement", i);
				String s=XMLHandler.getTagValue(onode, "id");				
				queryElementList[i]=s;				
			}
			cswParam.setQueryableElement(queryElementList);
			
			//comparison element list
			Node comparisonOperatorlistNode = XMLHandler.getSubNode(stepnode, "comparisonoperators");
			int nrcomparisonoperator = XMLHandler.countNodes(comparisonOperatorlistNode, "operator");

			//
			String[] operatorList= new String[nrcomparisonoperator];
			for (int i = 0; i < nrcomparisonoperator; i++) {				
				Node onode = XMLHandler.getSubNodeByNr(comparisonOperatorlistNode, "operator", i);
				String s=XMLHandler.getTagValue(onode, "id");				
				operatorList[i]=s;				
			}
			cswParam.setComparisonOperator(operatorList);
			
		}
		catch(Exception e){
			throw new KettleXMLException(Messages.getString("CSWInputMeta.Exception.UnableToReadStepInformationFromXML"), e); //$NON-NLS-1$
		}
	}

	
	public String getXML(){
		StringBuffer retval = new StringBuffer();
		retval.append("    " + XMLHandler.addTagValue("url",   cswParam.getCatalogUrl().toString())); //$NON-NLS-1$ //$NON-NLS-2$		
		retval.append("    " + XMLHandler.addTagValue("method", cswParam.getMethod())); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("cswVersion", cswParam.getVersion())); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("keyword", cswParam.getKeyword())); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("elementset", cswParam.getElementSet())); //$NON-NLS-1$ //$NON-NLS-2$
		
		retval.append("    " + XMLHandler.addTagValue("startdate", cswParam.getStartDate())); 
		retval.append("    " + XMLHandler.addTagValue("enddate", cswParam.getEndDate())); 
		retval.append("    " + XMLHandler.addTagValue("simplesearch", cswParam.isSimpleSearch())); 
		retval.append("    " + XMLHandler.addTagValue("activatelogin", cswParam.isUseLoginService()));
		
		retval.append("    " + XMLHandler.addTagValue("title", cswParam.getTitle()));
		retval.append("    " + XMLHandler.addTagValue("username", cswParam.getUsername())); 
		retval.append("    " + XMLHandler.addTagValue("password", cswParam.getPassword()));
		retval.append("    " + XMLHandler.addTagValue("loginurl", cswParam.getLoginServiceUrl()));
		retval.append("    " + XMLHandler.addTagValue("outputschema", cswParam.getOutputSchema()));
		retval.append("    " + XMLHandler.addTagValue("constraintlanguage", cswParam.getConstraintLanguage()));
		retval.append("    " + XMLHandler.addTagValue("startposition", cswParam.getStartPosition()));
		retval.append("    " + XMLHandler.addTagValue("maxrecords", cswParam.getMaxRecords()));
		retval.append("    " + XMLHandler.addTagValue("spatialoperator", cswParam.getSpatialOperator()));
		
				
		Iterator<String> it=cswParam.getBBOX().keySet().iterator();
		while(it.hasNext()) {
			String bbName=it.next();				
			retval.append("    " +XMLHandler.addTagValue("BBOX_"+bbName, cswParam.getBBOX().get(bbName)));
			
		}
		
		//Writing outputschema list
		retval.append("    <outputschemalist>").append(Const.CR);
		if (cswParam.getOutputSchemaList()!=null){
			for(String s:cswParam.getOutputSchemaList()){
				retval.append("      <outputschema>").append(Const.CR);
				retval.append("        ").append(XMLHandler.addTagValue("id", s));
				retval.append("      </outputschema>").append(Const.CR);
			}
		}
		retval.append("    </outputschemalist>").append(Const.CR);
		
		//queryelement list
		retval.append("    <queryelementlist>").append(Const.CR);
		if (cswParam.getQueryableElement()!=null){
			for(String s:cswParam.getQueryableElement()){
				retval.append("      <queryelement>").append(Const.CR);
				retval.append("        ").append(XMLHandler.addTagValue("id", s));
				retval.append("      </queryelement>").append(Const.CR);
			}
		}
		retval.append("    </queryelementlist>").append(Const.CR);
		
		
		//comparison operator
		retval.append("    <comparisonoperators>").append(Const.CR);
		if (cswParam.getComparisonOperator()!=null){
			for(String s:cswParam.getComparisonOperator()){
				retval.append("      <operator>").append(Const.CR);
				retval.append("        ").append(XMLHandler.addTagValue("id", s));
				retval.append("      </operator>").append(Const.CR);
			}
		}
		retval.append("    </comparisonoperators>").append(Const.CR);
		
		//queries
		retval.append("    <queries>").append(Const.CR);
        if (cswParam.getAdvancedRequestParam()!=null){
			for (String[] s:cswParam.getAdvancedRequestParam()) {
				retval.append("      <query>").append(Const.CR);
				int j=0;
				String tagName=null;
				for (String c:s){
					if (j==0){
						tagName="operand";
					}else
					if (j==1){
						tagName="operator";
					}else
					if (j==2){
						tagName="value";
					}
					retval.append("        ").append(XMLHandler.addTagValue(tagName, c));
					j++;
				}
				
				retval.append("      </query>").append(Const.CR);
			}
        }
		retval.append("    </queries>").append(Const.CR);
        
		//		
		return retval.toString();
	}
	
	
	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#readRep(org.pentaho.di.repository.Repository, long, java.util.List, java.util.Map)
	 */
	@Override
	public void readRep(Repository rep, long idStep,
			List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleException {
		// ur= rep.getStepAttributeString (id_step, "url"); //
		try{
			cswParam.setCatalogUrl(rep.getStepAttributeString(idStep, "url")) ; 
			cswParam.setMethod(rep.getStepAttributeString(idStep, "method"));
			cswParam.setVersion(rep.getStepAttributeString(idStep, "cswVersion")) ;
			cswParam.setKeyword(rep.getStepAttributeString(idStep, "keyword"));
			cswParam.setElementSet(rep.getStepAttributeString(idStep, "elementset"));
			cswParam.setStartDate(rep.getStepAttributeString(idStep, "startdate"));
			cswParam.setEndDate(rep.getStepAttributeString(idStep, "enddate"));
			cswParam.setSimpleSearch("Y".equalsIgnoreCase(rep.getStepAttributeString(idStep, "simplesearch")));
			cswParam.setUseLoginService("Y".equalsIgnoreCase(rep.getStepAttributeString(idStep, "activatelogin")));
			
			cswParam.setUsername(rep.getStepAttributeString(idStep, "username"));
			cswParam.setPassword(rep.getStepAttributeString(idStep, "password"));
			cswParam.setLoginServiceUrl(rep.getStepAttributeString(idStep, "loginurl"));
			cswParam.setOutputSchema(rep.getStepAttributeString(idStep, "outputschema"));
			cswParam.setTitle(rep.getStepAttributeString(idStep, "title"));
			cswParam.setSpatialOperator(rep.getStepAttributeString(idStep, "spatialoperator"));
			
			cswParam.setConstraintLanguage(rep.getStepAttributeString(idStep, "constraintlanguage"));
			
			
			try{
				int val=(int) rep.getStepAttributeInteger(idStep, "startposition");
				cswParam.setStartPosition(val);
			}catch(NumberFormatException e){
				cswParam.setStartPosition(1);
				//e.printStackTrace();
			}
			try{
				int val=(int) rep.getStepAttributeInteger(idStep, "maxrecords");
				cswParam.setMaxRecords(val);
			}catch(NumberFormatException e){
				cswParam.setMaxRecords(10);
				//e.printStackTrace();
			}
			
			
			Iterator<String> it= cswParam.getBBOX().keySet().iterator();
			HashMap<String, Double> bbox=new HashMap<String, Double>();
			while (it.hasNext()){
				String courant=it.next();
				double chaine=rep.getStepAttributeInteger(idStep, "BBOX_"+courant);
				//
				//if (chaine!=null){
					bbox.put(courant, chaine);
				//}
				
				
			}
			cswParam.setBBOX(bbox);			
			
			int nrQuery = rep.countNrStepAttributes(idStep, "query_line");
			
			
			ArrayList<String[]> queryList= new ArrayList<String[]>();
			

			for (int i = 0; i < nrQuery; i++) {
				String ch=rep.getStepAttributeString(idStep,i, "query_line");
				String[] s=ch.split("@");
				queryList.add(s);				
			}
			
			cswParam.setAdvancedRequestParam(queryList);
		}
		catch(Exception e){
			throw new KettleXMLException(Messages.getString("CSWInputMeta.Exception.UnableToReadStepInformationFromRepository"), e); //$NON-NLS-1$
		}

	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#saveRep(org.pentaho.di.repository.Repository, long, long)
	 */
	@Override
	public void saveRep(Repository rep, long idTransformation, long idStep)
			throws KettleException {
		
		rep.saveStepAttribute(idTransformation, idStep,"url",   cswParam.getCatalogUrl().toString()); 		
		rep.saveStepAttribute(idTransformation, idStep,"method", cswParam.getMethod()); 
		rep.saveStepAttribute(idTransformation, idStep,"cswVersion", cswParam.getVersion()); 
		rep.saveStepAttribute(idTransformation, idStep,"keyword", cswParam.getKeyword()); 
		rep.saveStepAttribute(idTransformation, idStep,"elementset", cswParam.getElementSet()); 
		
		rep.saveStepAttribute(idTransformation, idStep,"startdate", cswParam.getStartDate()); 
		rep.saveStepAttribute(idTransformation, idStep,"enddate", cswParam.getEndDate()); 
		rep.saveStepAttribute(idTransformation, idStep,"simplesearch", cswParam.isSimpleSearch()); 
		rep.saveStepAttribute(idTransformation, idStep,"activatelogin", cswParam.isUseLoginService());
		
		rep.saveStepAttribute(idTransformation, idStep,"title", cswParam.getTitle());
		rep.saveStepAttribute(idTransformation, idStep,"username", cswParam.getUsername()); 
		rep.saveStepAttribute(idTransformation, idStep,"password", cswParam.getPassword());
		rep.saveStepAttribute(idTransformation, idStep,"loginurl", cswParam.getLoginServiceUrl());
		rep.saveStepAttribute(idTransformation, idStep,"outputschema", cswParam.getOutputSchema());
		rep.saveStepAttribute(idTransformation, idStep,"constraintlanguage", cswParam.getConstraintLanguage());
		rep.saveStepAttribute(idTransformation, idStep,"startposition", cswParam.getStartPosition());
		rep.saveStepAttribute(idTransformation, idStep,"maxrecords", cswParam.getMaxRecords());
		rep.saveStepAttribute(idTransformation, idStep,"spatialoperator", cswParam.getSpatialOperator());
		
				
		Iterator<String> it=cswParam.getBBOX().keySet().iterator();
		while(it.hasNext()) {
			String bbName=it.next();				
			rep.saveStepAttribute(idTransformation, idStep,"BBOX_"+bbName, cswParam.getBBOX().get(bbName));
			
		}
		
		
        if (cswParam.getAdvancedRequestParam()!=null){
        	int cpt=0;
			for (String[] s:cswParam.getAdvancedRequestParam()) {
				String temps="";
				int i=0;
				for (String ss:s){
					temps +=ss;
					if (i<2){
						temps +="@";
					}
					i++;
				}
				rep.saveStepAttribute(idTransformation, idStep,cpt,"query_line", temps);
				cpt++;
			}
        }		

	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#setDefault()
	 */
	@Override
	public void setDefault() {
		// 
		cswParam.setKeyword(null);
		cswParam.setVersion("2.0.0");
		cswParam.setMethod("GET");
		cswParam.setStartDate(null);
		cswParam.setEndDate(null);
		cswParam.setConstraintLanguage("CQL_TEXT");
		cswParam.setStartPosition(1);
		cswParam.setMaxRecords(10);
		cswParam.setAdvancedRequestParam(null);
		cswParam.setUseLoginService(false);
		cswParam.setSpatialOperator("BBOX");
		cswParam.setOutputSchema("http://www.opengis.net/cat/csw/2.0.2");
		cswParam.setOutputSchemaList(null);
		cswParam.setQueryableElement(null);
		cswParam.setComparisonOperator(null);
		
		try {
			cswParam.setCatalogUrl("http://catalog-server/CSW");
		} catch (MalformedURLException e) {
			
		}
		
		HashMap<String,Double> bbox=new HashMap<String, Double>();
		bbox.put("NORTH", new Double(90));
		bbox.put("SOUTH", new Double(-90));
		bbox.put("EAST", new Double(180));
		bbox.put("WEST", new Double(-180));
		cswParam.setBBOX(bbox);
		
		cswParam.setElementSet("Brief");

	}

	/**
	 * @param cswParam the cswParam to set
	 */
	public void setCswParam(CSWReader cswParam) {
		this.cswParam = cswParam;
	}

	/**
	 * @return the cswParam
	 */
	public CSWReader getCswParam() {
		return cswParam;
	}
	
	/**
	 * 
	 * */
	private void setProfileBasedOnOutputSchemaValue(){
		if(cswParam.getOutputSchema()!=null){
			String outputText=cswParam.getOutputSchema();
			
			//if output schema is default CSW profile 
			if (outputText.equalsIgnoreCase(DEFAULT_PROFILE)){
				if (cswParam.getElementSet().equalsIgnoreCase("brief")){
					cswParam.setProfile(CSWBRIEFRECORD);
				}else
				if (cswParam.getElementSet().equalsIgnoreCase("summary")){
					cswParam.setProfile(CSWSUMMARYRECORD);
				}else
				if(cswParam.getElementSet().equalsIgnoreCase("full")){
					cswParam.setProfile(CSWFULLRECORD);
				}
			}else
			if(outputText.equalsIgnoreCase(ISOTC211_2005_PROFILE)){
				cswParam.setProfile(GMD_MD_Metadata);
			}else{
				//TODO
				cswParam.setProfile(GMD_MD_Metadata);
			}
		}
	}
	
	public void getFields(RowMetaInterface row, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space){
    		
		RowMetaInterface columnField=null;
		String err;
		
		setProfileBasedOnOutputSchemaValue();
		String pattern=cswParam.getProfile();
		
		
			try {
				if (this.getCswParam().getXMLRequestResult()==null){								
				cswParam.setXMLRequestResult(cswParam.fromStringToJDOMDocument(cswParam.GetRecords()));
				}
				err=cswParam.checkIfReponseReturnException(cswParam.getXMLRequestResult().getRootElement());
				//System.out.println(err);
				if (err!=null){
					throw new KettleException(err);
					
				}
				if (cswParam.getOutputSchema().equalsIgnoreCase(DEFAULT_PROFILE)){
					columnField=getFieldsFromDefaultProfileDocument(row,pattern);
				}else
				if(cswParam.getOutputSchema().equalsIgnoreCase(ISOTC211_2005_PROFILE)){
					columnField=getFieldsFromISOTC2112005ProfileDocument(row,pattern);
				}else{
					columnField=getFieldsFromISOTC2112005ProfileDocument(row,pattern);
				}
				//
				//columnField.addValueMeta(new ValueMeta("Geom",ValueMetaInterface.TYPE_GEOMETRY));
				//
				//this.fieds=columnField;
				cswParam.setColumnField(columnField);
				
				
			} catch (KettleException e) {
				
			} catch (ServletException e) {
				
			} catch (IOException e) {
				
			}
		
	        
    	
	}
	
	/**
	 * this method retrieve metaField from document formatted using csw default profile
	 * */
	private RowMetaInterface getFieldsFromDefaultProfileDocument(RowMetaInterface row, String profile) throws KettleException, ServletException, IOException{
		ArrayList<String> colName;
		int nbrRecords=cswParam.getNumberOfRecord(cswParam.getXMLRequestResult(), "csw:SearchResults");
		if (nbrRecords>0){
			colName=new ArrayList<String>();
			Element el=this.cswParam.findSubElement(cswParam.getXMLRequestResult().getRootElement(),profile);
			Iterator<Element> it=cswParam.getColumns(el).iterator();
			while (it.hasNext()){
				Element c=it.next();
				if ((c.getName().equalsIgnoreCase("lowercorner"))||(c.getName().equalsIgnoreCase("uppercorner"))){
					row.addValueMeta(new ValueMeta(c.getParentElement().getName()+"_"+c.getName(),ValueMetaInterface.TYPE_GEOMETRY));
				}else
				row.addValueMeta(new ValueMeta(c.getParentElement().getName()+"_"+c.getName(), ValueMetaInterface.TYPE_STRING));
				
				colName.add(c.getParentElement().getName()+"_"+c.getName());
			}				
		//
			fieds=row;
			cswParam.setColsName(colName);
		}else{
			throw new KettleException("Zero Records return");
		}
		
		return fieds;
	}
	
	private RowMetaInterface getFieldsFromISOTC2112005ProfileDocument(RowMetaInterface row, String profile) throws KettleException, ServletException, IOException{
		ArrayList<String> colName;
		int nbrRecords=cswParam.getNumberOfRecord(cswParam.getXMLRequestResult(), "csw:SearchResults");
		if (nbrRecords>0){
			colName=new ArrayList<String>();
			Element el=this.cswParam.findSubElement(cswParam.getXMLRequestResult().getRootElement(),profile);
			Iterator<Element> it=cswParam.getColumns(el).iterator();
			while (it.hasNext()){
				Element c=it.next();					
				//row.addValueMeta(new ValueMeta(c.getParentElement().getName(), ValueMetaInterface.TYPE_STRING));
				row.addValueMeta(new ValueMeta(c.getParentElement().getParentElement().getName()+"_"+c.getParentElement().getName(), ValueMetaInterface.TYPE_STRING));
				//colName.add(c.getParentElement().getName());
				colName.add(c.getParentElement().getParentElement().getName()+"_"+c.getParentElement().getName());
			}				
		//
			fieds=row;
			cswParam.setColsName(colName);
		}else{
			throw new KettleException("Zero Records return");
		}
		return fieds;
	}

	/**
	 * @param fieds the fieds to set
	 */
	public void setMetaInterfaceFieds(RowMetaInterface fieds) {
		this.fieds = fieds; 
		
	}

	/**
	 * @return the fieds
	 */
	public RowMetaInterface getMetaInterfaceFieds() {
		
		return fieds;
	}
	
	

}
