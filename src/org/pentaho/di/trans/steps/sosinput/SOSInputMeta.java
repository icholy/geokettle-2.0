package org.pentaho.di.trans.steps.sosinput;
/*
 * Created on 18-february-2010
 * 
 * by: jmathieu
 *
 */
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
import org.w3c.dom.Node;

public class SOSInputMeta extends BaseStepMeta implements StepMetaInterface{
	
    private String  	url; 
    private String 		sosVersion;
    private String 		method;
    private String 		request;
    private String  	urlObs;
    private String 		offering; 
    private String[] 	observedProperties;  
    private String[] 	procedures; 
    private boolean 	differentUrlObs;
    private String 		time1;
    private boolean 	time1InField;
    private String  	time1Field;
    private String 		time2;  
    private boolean 	time2InField;
    private String  	time2Field;

    public SOSInputMeta(){
        super(); // allocate BaseStepMeta
    }
    
    public String getMethod(){
    	return method;
    }   
    
    public void setMethod(String method){
    	this.method=method;
    }
    
    public String getSOSVersion(){
    	return sosVersion;
    }   
    
    public void setSOSVersion(String version){
    	this.sosVersion=version;
    }
    
    public String getRequest(){
    	return request;
    }   
    
    public void setRequest(String request){
    	this.request=request;
    }
    
    public String getOffering(){
    	return offering;
    }
    
    public void setOffering(String offering){
    	this.offering=offering;
    }
    
    public String[] getProcedures(){
    	return procedures;
    }
    
    public void setProcedures(String[] procedures){
    	this.procedures=procedures;
    }
    
    public String[] getObservedProperties(){
    	return observedProperties;
    }
    
    public void setObservedProperties(String[] observedProperties){
    	this.observedProperties=observedProperties;
    }

    public String getTime1(){
    	return time1;
    }
    
    public void setTime1(String time1){
    	this.time1=time1;
    }
    
    public String getTime2(){
    	return time2;
    }
    
    public void setTime2(String time2){
    	this.time2=time2;
    }

    public String getUrl(){
        return url;
    }
    
    public void setUrlObs(String urlObs){
        this.urlObs=urlObs;
    }
    
    public String getUrlObs(){
        return urlObs;
    }
    
    public void setUrl(String url){
        this.url=url;
    }
    
    public boolean isDifferentUrlObs() {
		return differentUrlObs;
	}

	public void setDifferentUrlObs(boolean differentUrlObs) {
		this.differentUrlObs = differentUrlObs;
	}
	
    public boolean isTime1InField() {
		return time1InField;
	}

	public void setTime1InField(boolean time1InField) {
		this.time1InField = time1InField;
	}

	public String getTime1Field() {
		return time1Field;
	}
	
	public void setTime1Field(String time1Field) {
		this.time1Field = time1Field;
	}
	
    public boolean isTime2InField() {
		return time2InField;
	}

	public void setTime2InField(boolean time2InField) {
		this.time2InField = time2InField;
	}

	public String getTime2Field() {
		return time2Field;
	}
	
	public void setTime2Field(String time2Field) {
		this.time2Field = time2Field;
	}

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans){
        return new SOSInput(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData(){
        return new SOSInputData();
    }

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}
	
	private void readData(Node stepnode)throws KettleXMLException{
		try{
			url        	 	= XMLHandler.getTagValue(stepnode, "url"); //$NON-NLS-1$ 
			method        	 	= XMLHandler.getTagValue(stepnode, "method"); //$NON-NLS-1$  
			sosVersion        	= XMLHandler.getTagValue(stepnode, "sosVersion"); //$NON-NLS-1$  
			request        	 	= XMLHandler.getTagValue(stepnode, "request"); //$NON-NLS-1$ 
			urlObs       	 	= XMLHandler.getTagValue(stepnode, "urlobs"); //$NON-NLS-1$ 
			offering         	= XMLHandler.getTagValue(stepnode, "offering"); //$NON-NLS-1$  
			differentUrlObs 	= "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "differentUrlObs"));
			time1         	 	= XMLHandler.getTagValue(stepnode, "time1"); //$NON-NLS-1$  
			time1InField 	 	= "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "time1InField"));
			time1Field       	= XMLHandler.getTagValue(stepnode, "time1Field");
			time2         	 	= XMLHandler.getTagValue(stepnode, "time2"); //$NON-NLS-1$  
			time2InField 	 	= "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "time2InField"));
			time2Field       	= XMLHandler.getTagValue(stepnode, "time2Field");
			
			Node observedPropertiesNode = XMLHandler.getSubNode(stepnode, "observed_properties");
			int nrObsProps = XMLHandler.countNodes(observedPropertiesNode, "observed_property");

			allocateObsProps(nrObsProps);

			for (int i = 0; i < nrObsProps; i++) {
				Node onode = XMLHandler.getSubNodeByNr(observedPropertiesNode, "observed_property", i);
				observedProperties[i] = XMLHandler.getTagValue(onode, "id");
			}	
			
			Node proceduresNode = XMLHandler.getSubNode(stepnode, "procedures");
			int nrProcs = XMLHandler.countNodes(proceduresNode, "procedure");

			allocateProcs(nrProcs);

			for (int i = 0; i < nrProcs; i++) {
				Node pnode = XMLHandler.getSubNodeByNr(proceduresNode, "procedure", i);
				procedures[i] = XMLHandler.getTagValue(pnode, "id");
			}
		}
		catch(Exception e){
			throw new KettleXMLException(Messages.getString("SOSInputMeta.Exception.UnableToReadStepInformationFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault(){
		url = null;
		sosVersion = null;
		method = null;
		request=null;
		urlObs = null;
		offering=null;  
		observedProperties=null;  
		procedures=null; 
		differentUrlObs = false;
	    time1=null;
	    time1InField=false;
	    time1Field=null;
	    time2=null;  
	    time2InField=false;
	    time2Field=null;
	}
	
	public Object clone(){
		SOSInputMeta retval = (SOSInputMeta)super.clone();

		return retval;
	}	
	
	public void allocateProcs(int nrProcs) {
		procedures = new String[nrProcs];
	}
	
	public void allocateObsProps(int nrObsProps) {
		observedProperties = new String[nrObsProps];
	}
		
	public String getXML(){
		StringBuffer retval = new StringBuffer();
		retval.append("    " + XMLHandler.addTagValue("url",   url)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("request", request)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("method", method)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("sosVersion", sosVersion)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("urlobs",   urlObs)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("offering",   offering)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("differentUrlObs",  differentUrlObs));
		retval.append("    " + XMLHandler.addTagValue("time1",   time1)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("time1InField",  time1InField));
        retval.append("    " + XMLHandler.addTagValue("time1Field",  time1Field));
		retval.append("    " + XMLHandler.addTagValue("time2", time2)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("time2InField",  time2InField));
        retval.append("    " + XMLHandler.addTagValue("time2Field",  time2Field));
        retval.append("    <observed_properties>").append(Const.CR);
        if (observedProperties!=null){
			for (int i = 0; i < observedProperties.length; i++) {
				retval.append("      <observed_property>").append(Const.CR);
				retval.append("        ").append(XMLHandler.addTagValue("id", observedProperties[i]));
				retval.append("      </observed_property>").append(Const.CR);
			}
        }
		retval.append("    </observed_properties>").append(Const.CR);
        retval.append("    <procedures>").append(Const.CR);
		if (procedures!=null){
			for (int i = 0; i < procedures.length; i++) {
					retval.append("      <procedure>").append(Const.CR);
					retval.append("        ").append(XMLHandler.addTagValue("id", procedures[i]));
					retval.append("      </procedure>").append(Const.CR);
	
			}
		}
		retval.append("    </procedures>").append(Const.CR);
		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException{
		try{
			url             		= rep.getStepAttributeString (id_step, "url"); //$NON-NLS-1$
			request        			= rep.getStepAttributeString (id_step, "request"); //$NON-NLS-1$
			method        			= rep.getStepAttributeString (id_step, "method"); //$NON-NLS-1$
			sosVersion        		= rep.getStepAttributeString (id_step, "sosVersion"); //$NON-NLS-1$
			urlObs           		= rep.getStepAttributeString (id_step, "urlobs"); //$NON-NLS-1$
			offering        		= rep.getStepAttributeString(id_step, "offering"); //$NON-NLS-1$
			differentUrlObs 		= rep.getStepAttributeBoolean (id_step, "differentUrlObs");
			time1            		= rep.getStepAttributeString(id_step, "time1"); //$NON-NLS-1$
			time1InField 			= rep.getStepAttributeBoolean (id_step, "time1InField");
			time1Field				= rep.getStepAttributeString (id_step, "time1Field");
			time2          			= rep.getStepAttributeString (id_step, "time2"); //$NON-NLS-1$
			time2InField 			= rep.getStepAttributeBoolean (id_step, "time2InField");
			time2Field				= rep.getStepAttributeString (id_step, "time2Field");
			
			int nrObsProps = rep.countNrStepAttributes(id_step, "observed_property_id");

			allocateObsProps(nrObsProps);

			for (int i = 0; i < nrObsProps; i++) {
				observedProperties[i] = rep.getStepAttributeString(id_step, i,
						"observed_property_id");
			}
			
			int nrProcs = rep.countNrStepAttributes(id_step, "procedure_id");

			allocateProcs(nrProcs);

			for (int i = 0; i < nrProcs; i++) {
				procedures[i] = rep.getStepAttributeString(id_step, i,
						"procedure_id");
			}
		}
		catch(Exception e){
			throw new KettleException(Messages.getString("SOSInputMeta.Exception.UnexpectedErrorReadingMetaDataFromRepository"), e); //$NON-NLS-1$
		}
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)throws KettleException{
		try{
			rep.saveStepAttribute(id_transformation, id_step, "url",       			url); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "request",   			request); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "method",   			method); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "sosVersion",   			sosVersion); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "urlobs",       			urlObs); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "offering",  			offering); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "differentUrlObs",   differentUrlObs);
			rep.saveStepAttribute(id_transformation, id_step, "time1",       		time1); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "time1InField",   time1InField);
			rep.saveStepAttribute(id_transformation, id_step, "time1Field",   time1Field);
			rep.saveStepAttribute(id_transformation, id_step, "time2",     			time2); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "time2InField",   time2InField);
			rep.saveStepAttribute(id_transformation, id_step, "time2Field",   time2Field);
			for (int i = 0; i < observedProperties.length; i++) {
				rep.saveStepAttribute(id_transformation, id_step, i,
						"observed_property_id", observedProperties[i]);
			}
			for (int i = 0; i < procedures.length; i++) {
				rep.saveStepAttribute(id_transformation, id_step, i,
						"procedure_id", procedures[i]);
			}
		}
		catch(Exception e){
			throw new KettleException(Messages.getString("SOSInputMeta.Exception.UnableToSaveMetaDataToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info){
		 CheckResult cr;

		    if (Const.isEmpty(url)){
		    	cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SOSInputMeta.CheckResult.NoURLSpecified"), stepMeta); //$NON-NLS-1$
	            remarks.add(cr);
		    }
		    if (Const.isEmpty(sosVersion)){
		    	cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SOSInputMeta.CheckResult.NoSOSVersionSpecified"), stepMeta); //$NON-NLS-1$
	            remarks.add(cr);
		    }
		    if (Const.isEmpty(method)){
		    	cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SOSInputMeta.CheckResult.NoMethodSpecified"), stepMeta); //$NON-NLS-1$
	            remarks.add(cr);
		    }
		    if (Const.isEmpty(request)){
		    	cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SOSInputMeta.CheckResult.NoMethodSpecified"), stepMeta); //$NON-NLS-1$
	            remarks.add(cr);
		    }
		 
	        // See if we have input streams leading to this step!
	        if (input.length > 0){
	            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SOSInputMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
	            remarks.add(cr);
	        }else{
	            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SOSInputMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
	            remarks.add(cr);
	        }
	        
	        if (request.equals( Messages.getString("SOSInputDialog.Request.GetObservation"))){
	        	if(Const.isEmpty(offering))
	        		cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SOSInputMeta.CheckResult.NoOffering"), stepMeta); //$NON-NLS-1$
	        	if(observedProperties==null)
	        		cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SOSInputMeta.CheckResult.NoObservedProperty"), stepMeta); //$NON-NLS-1$
	        	if(differentUrlObs && Const.isEmpty(urlObs))
	        		cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SOSInputMeta.CheckResult.NoUrlObs"), stepMeta); //$NON-NLS-1$
	        }
	        remarks.add(cr);
	}
    
    public void getFields(RowMetaInterface row, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space){
    	if (request.equals("GetObservation")){
	        row.addValueMeta(new ValueMeta("Procedure", ValueMetaInterface.TYPE_STRING));
	        row.addValueMeta(new ValueMeta("Offering", ValueMetaInterface.TYPE_STRING));
	        row.addValueMeta(new ValueMeta("Observed property", ValueMetaInterface.TYPE_STRING));
	        row.addValueMeta(new ValueMeta("SamplingTime", ValueMetaInterface.TYPE_DATE));
	        row.addValueMeta(new ValueMeta("Feature id", ValueMetaInterface.TYPE_STRING)); 
	        row.addValueMeta(new ValueMeta("Feature name", ValueMetaInterface.TYPE_STRING));
	        row.addValueMeta(new ValueMeta("Feature geometry", ValueMetaInterface.TYPE_GEOMETRY)); 
	        row.addValueMeta(new ValueMeta("Measure", ValueMetaInterface.TYPE_NUMBER));
	        row.addValueMeta(new ValueMeta("uom", ValueMetaInterface.TYPE_STRING)); 
    	}
	}
}