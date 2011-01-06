package org.pentaho.di.trans.steps.gmlfileinput;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.GMLReader;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
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


public class GMLFileInputMeta extends BaseStepMeta implements StepMetaInterface
{
	private String  fileName; 
	private boolean isFileNameInField;
	private String fileNameField;
	private int 	rowLimit;
	private boolean rowNrAdded;
	private String  rowNrField;

	public GMLFileInputMeta()
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
    
    /**
     * @return Returns the rowLimit.
     */
    public int getRowLimit()
    {
        return rowLimit;
    }
    
    /**
     * @param rowLimit The rowLimit to set.
     */
    public void setRowLimit(int rowLimit)
    {
        this.rowLimit = rowLimit;
    }
    
    /**
     * @return Returns the rowNrField.
     */
    public String getRowNrField()
    {
        return rowNrField;
    }
    
    /**
     * @param rowNrField The rowNrField to set.
     */
    public void setRowNrField(String rowNrField)
    {
        this.rowNrField = rowNrField;
    }
    
    /**
     * @return Returns the rowNrAdded.
     */
    public boolean isRowNrAdded()
    {
        return rowNrAdded;
    }
    
    /**
     * @param rowNrAdded The rowNrAdded to set.
     */
    public void setRowNrAdded(boolean rowNrAdded)
    {
        this.rowNrAdded = rowNrAdded;
    }


    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	public Object clone()
	{
		GMLFileInputMeta retval = (GMLFileInputMeta)super.clone();
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			fileNameField     = XMLHandler.getTagValue(stepnode, "filenamefield");
			isFileNameInField  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "isfilenameinfield"));			
			fileName    = XMLHandler.getTagValue(stepnode, "filename");
			rowLimit           = Const.toInt(XMLHandler.getTagValue(stepnode, "limit"), 0); //$NON-NLS-1$
			rowNrAdded         = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "add_rownr")); //$NON-NLS-1$ //$NON-NLS-2$
			rowNrField         = XMLHandler.getTagValue(stepnode, "field_rownr"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("GMLFileInputMeta.Exception.UnableToReadStepInformationFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		fileName    = null;
		fileNameField = null;
		isFileNameInField = false;
		rowLimit    = 0;
		rowNrAdded   = false;
		rowNrField = null;
	}
    
    @Override
    public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {   	
        FileObject fo;
        if (!isFileNameInField()){
			try {
					fo = KettleVFS.getFileObject(fileName);
			} catch (IOException e) {
				throw new KettleStepException(Messages.getString("GMLFileInputMeta.Exception.NoFilesFoundToProcess")); //$NON-NLS-1$
			}
	    	if (fo == null)
	        {
	            throw new KettleStepException(Messages.getString("GMLFileInputMeta.Exception.NoFilesFoundToProcess")); //$NON-NLS-1$
	        }
	
	        row.addRowMeta( getOutputFields(fo, name) );
        }
	}
    
	public RowMetaInterface getOutputFields(FileObject fo, String name)
		throws KettleStepException
	{
		RowMetaInterface rowMeta = new RowMeta();
				
        if (fo==null)
        {
            throw new KettleStepException(Messages.getString("GMLFileInputMeta.Exception.NoFilesFoundToProcess")); //$NON-NLS-1$
        }
        
        // Take the first file to determine what the layout is...
        //
        GMLReader gmlr = null;
		try
		{
			java.net.URL fileURL = fo.getURL();
			gmlr = new GMLReader(fileURL);
			gmlr.open();
			RowMetaInterface add = gmlr.getFields();
			for (int i=0;i<add.size();i++)
			{
				ValueMetaInterface v=add.getValueMeta(i);
				v.setOrigin(name);
			}
			rowMeta.addRowMeta( add );
		}
		catch(Exception ke)
	    {
			throw new KettleStepException(Messages.getString("GMLFileInputMeta.Exception.UnableToReadMetaDataFromGMLFile"), ke); //$NON-NLS-1$
	    }
      
	    
	    if (rowNrAdded && rowNrField!=null && rowNrField.length()>0)
	    {
	    	ValueMetaInterface rnr = new ValueMeta(rowNrField, ValueMetaInterface.TYPE_INTEGER);
	    	rnr.setOrigin(name);
	    	rowMeta.addValueMeta(rnr);
	    }
		
	    if (isFileNameInField)
        {
            ValueMetaInterface v = new ValueMeta(fileNameField, ValueMeta.TYPE_STRING);
            v.setLength(100, -1);
            v.setOrigin(name);
            rowMeta.addValueMeta(v);
        }
	    
		return rowMeta;
	}	

	public String getXML()
	{
		StringBuffer retval=new StringBuffer();
		
		retval.append("    " + XMLHandler.addTagValue("filename", fileName));
		retval.append("    " + XMLHandler.addTagValue("isfilenameinfield", isFileNameInField));
		retval.append("    " + XMLHandler.addTagValue("filenamefield", fileNameField));  	
		retval.append("    " + XMLHandler.addTagValue("limit",       rowLimit)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("add_rownr",   rowNrAdded)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("field_rownr", rowNrField)); //$NON-NLS-1$ //$NON-NLS-2$

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
			rowLimit              = (int)rep.getStepAttributeInteger(id_step, "limit"); //$NON-NLS-1$
			rowNrAdded             =      rep.getStepAttributeBoolean(id_step, "add_rownr"); //$NON-NLS-1$
			rowNrField           =      rep.getStepAttributeString (id_step, "field_rownr"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("GMLFileInputMeta.Exception.UnexpectedErrorReadingMetaDataFromRepository"), e); //$NON-NLS-1$
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
			rep.saveStepAttribute(id_transformation, id_step, "limit",           rowLimit); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "add_rownr",       rowNrAdded); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "field_rownr",     rowNrField); //$NON-NLS-1$
        }
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("GMLFileInputMeta.Exception.UnableToSaveMetaDataToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
			String[] input, String[] output, RowMetaInterface info)
	{
		CheckResult cr;
		if (!isFileNameInField){
			if (fileName ==null){
			    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GMLFileInputMeta.Remark.PleaseSelectFileToUse"), stepMeta); //$NON-NLS-1$
			    remarks.add(cr);
			}
		}else if (fileNameField == null){
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GMLFileInputMeta.Remark.PleaseSelectFileField"), stepMeta); //$NON-NLS-1$
		    remarks.add(cr);
		}else{	
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("GMLFileInputMeta.Remark.FileToUseIsSpecified"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
            if (input.length > 0)
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("GMLFileInputMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
            else
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GMLFileInputMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
        }
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new GMLFileInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new GMLFileInputData();
	}

    public FileInputList getTextFileList(VariableSpace space)
    {
        return FileInputList.createFileList(space, new String[] { fileName }, new String[] { null }, new String[] { "N" });
    }  
}
