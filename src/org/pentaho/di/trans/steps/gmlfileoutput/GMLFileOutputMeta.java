package org.pentaho.di.trans.steps.gmlfileoutput;

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
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.gmlfileoutput.Messages;
import org.w3c.dom.Node;


public class GMLFileOutputMeta extends BaseStepMeta implements StepMetaInterface
{
	private  String  fileName; 
	private boolean isFileNameInField;
	private String fileNameField;
	private String  acceptingStepName;
	private StepMeta acceptingStep;

	public GMLFileOutputMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	public String getFileName(){
        return fileName;
    }
    
    public void setFileName(String  fileName){
        this.fileName = fileName;
    }
    
    public String getFileNameField(){
        return fileNameField;
    }
    
    public void setFileNameField(String fileNameField){
        this.fileNameField = fileNameField;
    }
    
    public boolean isFileNameInField(){
        return isFileNameInField;
    }
    
    public void setFileNameInField(boolean isfileNameInField){
        this.isFileNameInField = isfileNameInField;
    }   

	public String getLookupStepname(){
		if (isFileNameInField && acceptingStep!=null && !Const.isEmpty(acceptingStep.getName())) 
			return acceptingStep.getName();
		return null;
	}
	
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	public Object clone()
	{
		GMLFileOutputMeta retval = (GMLFileOutputMeta)super.clone();
		return retval;
	}
	
	public void searchInfoAndTargetSteps(List<StepMeta> steps){
		acceptingStep = StepMeta.findStep(steps, acceptingStepName);
	}

	public String[] getInfoSteps(){
		if (isFileNameInField && acceptingStep!=null)
			return new String[] { acceptingStep.getName() };		
		return super.getInfoSteps();
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			fileNameField     = XMLHandler.getTagValue(stepnode, "filenamefield");
			isFileNameInField  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "isfilenameinfield"));			
			fileName    = XMLHandler.getTagValue(stepnode, "filename");
			acceptingStepName = XMLHandler.getTagValue(stepnode, "accept_stepname");
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("GMLFileOutputMeta.Exception.UnableToReadStepInformationFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		fileName    = null;
		fileNameField = null;
		isFileNameInField = false;
	}

	public String getXML()
	{
		StringBuffer retval=new StringBuffer();
		
		retval.append("    " + XMLHandler.addTagValue("filename", fileName));
		retval.append("    " + XMLHandler.addTagValue("isfilenameinfield", isFileNameInField));
		retval.append("    " + XMLHandler.addTagValue("filenamefield", fileNameField));  
		retval.append("    ").append(XMLHandler.addTagValue("accept_stepname", (acceptingStep!=null?acceptingStep.getName():"") ));		
		
		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
		try
		{
			fileName    = rep.getStepAttributeString (id_step, "filename");
			isFileNameInField   = rep.getStepAttributeBoolean(id_step, "isfilenameinfield");	
			fileNameField     = rep.getStepAttributeString (id_step, "filenamefield");
			acceptingStepName  = rep.getStepAttributeString (id_step, "accept_stepname");
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("GMLFileOutputMeta.Exception.UnexpectedErrorReadingMetaDataFromRepository"), e); //$NON-NLS-1$
		}
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "filenamefield", fileNameField);
			rep.saveStepAttribute(id_transformation, id_step, "filename", fileName);
			rep.saveStepAttribute(id_transformation, id_step, "isfilenameinfield", isFileNameInField);
			rep.saveStepAttribute(id_transformation, id_step, "accept_stepname", (acceptingStep!=null?acceptingStep.getName():"") );
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("GMLFileOutputMeta.Exception.UnableToSaveMetaDataToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
			String[] input, String[] output, RowMetaInterface info)
	{
		CheckResult cr;
		
		if (!isFileNameInField){
			if (fileName ==null){
			    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GMLFileOutputMeta.Remark.PleaseSelectFileToUse"), stepMeta); //$NON-NLS-1$
			    remarks.add(cr);
			}
		}else if (fileNameField == null){
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GMLFileOutputMeta.Remark.PleaseSelectFileField"), stepMeta); //$NON-NLS-1$
		    remarks.add(cr);
		}else{	
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("GMLFileOutputMeta.Remark.FileToUseIsSpecified"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
            if (input.length > 0)
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("GMLFileOutputMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
            else
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GMLFileOutputMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
        }
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new GMLFileOutput(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new GMLFileOutputData();
	}

	public void setAcceptingStep(StepMeta acceptingStep) {
		this.acceptingStep = acceptingStep;
	}

	public StepMeta getAcceptingStep() {
		return acceptingStep;
	}

	public void setAcceptingStepName(String acceptingStepName) {
		this.acceptingStepName = acceptingStepName;
	}

	public String getAcceptingStepName() {
		return acceptingStepName;
	}  
}
