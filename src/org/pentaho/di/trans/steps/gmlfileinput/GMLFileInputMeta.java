package org.pentaho.di.trans.steps.gmlfileinput;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.deegree.framework.xml.XMLParsingException;
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
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.xbaseinput.Messages;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


public class GMLFileInputMeta extends BaseStepMeta implements StepMetaInterface
{
	private String 	gmlFileName;
	private int 	rowLimit;
	private boolean rowNrAdded;
	private String  rowNrField;
	
//    /** Are we accepting filenames in input rows?  */
//    private boolean acceptingFilenames;
//    
//    /** The field in which the filename is placed */
//    private String  acceptingField;
//
//    /** The stepname to accept filenames from */
//    private String  acceptingStepName;
//
//    /** The step to accept filenames from */
//    private StepMeta acceptingStep;
//
//    /** Flag indicating that we should include the filename in the output */
//    private boolean includeFilename;
//
//    /** The name of the field in the output containing the filename */
//    private String filenameField;


	public GMLFileInputMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	/**
     * @return Returns the gmlFileName.
     */
    public String getGmlFileName()
    {
        return gmlFileName;
    }
    
    /**
     * @param gisFileName The gisFileName to set.
     */
    public void setGmlFileName(String gmlFileName)
    {
        this.gmlFileName = gmlFileName;
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
			gmlFileName        = XMLHandler.getTagValue(stepnode, "file_gml"); //$NON-NLS-1$
			rowLimit           = Const.toInt(XMLHandler.getTagValue(stepnode, "limit"), 0); //$NON-NLS-1$
			rowNrAdded         = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "add_rownr")); //$NON-NLS-1$ //$NON-NLS-2$
			rowNrField         = XMLHandler.getTagValue(stepnode, "field_rownr"); //$NON-NLS-1$
            
//            includeFilename    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include"));
//            filenameField      = XMLHandler.getTagValue(stepnode, "include_field");
//            
//            acceptingFilenames = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "accept_filenames")); //$NON-NLS-1$
//            acceptingField     = XMLHandler.getTagValue(stepnode, "accept_field"); //$NON-NLS-1$
//            acceptingStepName  = XMLHandler.getTagValue(stepnode, "accept_stepname"); //$NON-NLS-1$

		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("GMLFileInputMeta.Exception.UnableToReadStepInformationFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		gmlFileName    = null;
		rowLimit    = 0;
		rowNrAdded   = false;
		rowNrField = null;
	}
	
//    public String getLookupStepname()
//    {
//        if (acceptingFilenames &&
//            acceptingStep!=null && 
//            !Const.isEmpty( acceptingStep.getName() )
//           ) 
//            return acceptingStep.getName();
//        return null;
//    }

//    public void searchInfoAndTargetSteps(ArrayList steps)
//    {
//        acceptingStep = StepMeta.findStep(steps, acceptingStepName);
//    }
//
//    public String[] getInfoSteps()
//    {
//        if (acceptingFilenames && acceptingStep!=null)
//        {
//            return new String[] { acceptingStep.getName() };
//        }
//        return super.getInfoSteps();
//    }
    
	public RowMetaInterface getOutputFields(FileInputList files, String name)
		throws KettleStepException
	{
		RowMetaInterface rowMeta = new RowMeta();
				
		// if (r==null) row=new Row(); // give back values
		// else         row=r;         // add to the existing row of values...

        // FileInputList fileList = getTextFileList();
		
        if (files.nrOfFiles()==0)
        {
            throw new KettleStepException(Messages.getString("GMLFileInputMeta.Exception.NoFilesFoundToProcess")); //$NON-NLS-1$
        }
        
        // Take the first file to determine what the layout is...
        //
        GMLReader gmlr = null;
		try
		{
			files.getFile(0).getName().getURI();
			java.net.URL fileURL = files.getFile(0).getURL();
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
        
//        if (includeFilename)
//        {
//            Value v = new Value(filenameField, Value.VALUE_TYPE_STRING);
//            v.setLength(100, -1);
//            v.setOrigin(name);
//            row.addValue(v);
//        }
		
		return rowMeta;
	}
	
    @Override
    public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {

    	FileInputList fileList = getTextFileList(space);
        if (fileList.nrOfFiles()==0)
        {
            throw new KettleStepException(Messages.getString("XBaseInputMeta.Exception.NoFilesFoundToProcess")); //$NON-NLS-1$
        }

        row.addRowMeta( getOutputFields(fileList, name) );
	}
	

	public String getXML()
	{
		StringBuffer retval=new StringBuffer();
		
		retval.append("    " + XMLHandler.addTagValue("file_gml",    gmlFileName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("limit",       rowLimit)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("add_rownr",   rowNrAdded)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("field_rownr", rowNrField)); //$NON-NLS-1$ //$NON-NLS-2$

//		retval.append("    " + XMLHandler.addTagValue("include", includeFilename));
//		retval.append("    " + XMLHandler.addTagValue("include_field", filenameField));
//
//
//		retval.append("    " + XMLHandler.addTagValue("accept_filenames", acceptingFilenames));
//		retval.append("    " + XMLHandler.addTagValue("accept_field", acceptingField));
//		retval.append("    " + XMLHandler.addTagValue("accept_stepname", (acceptingStep!=null?acceptingStep.getName():"") ));

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
		try
		{
			gmlFileName              =      rep.getStepAttributeString (id_step, "file_gml"); //$NON-NLS-1$
			rowLimit              = (int)rep.getStepAttributeInteger(id_step, "limit"); //$NON-NLS-1$
			rowNrAdded             =      rep.getStepAttributeBoolean(id_step, "add_rownr"); //$NON-NLS-1$
			rowNrField           =      rep.getStepAttributeString (id_step, "field_rownr"); //$NON-NLS-1$
            
//            includeFilename = rep.getStepAttributeBoolean(id_step, "include");
//            filenameField = rep.getStepAttributeString(id_step, "include_field");
//
//            acceptingFilenames = rep.getStepAttributeBoolean(id_step, "accept_filenames");
//            acceptingField     = rep.getStepAttributeString (id_step, "accept_field");
//            acceptingStepName  = rep.getStepAttributeString (id_step, "accept_stepname");

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
			rep.saveStepAttribute(id_transformation, id_step, "file_gml",        gmlFileName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "limit",           rowLimit); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "add_rownr",       rowNrAdded); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "field_rownr",     rowNrField); //$NON-NLS-1$
            
//            rep.saveStepAttribute(id_transformation, id_step, "include", includeFilename);
//            rep.saveStepAttribute(id_transformation, id_step, "include_field", filenameField);
//
//            rep.saveStepAttribute(id_transformation, id_step, "accept_filenames", acceptingFilenames); //$NON-NLS-1$
//            rep.saveStepAttribute(id_transformation, id_step, "accept_field", acceptingField); //$NON-NLS-1$
//            rep.saveStepAttribute(id_transformation, id_step, "accept_stepname", (acceptingStep!=null?acceptingStep.getName():"") ); //$NON-NLS-1$
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
		
		if (gmlFileName==null)
		{
            if ( false /* isAcceptingFilenames() */ ) 
            {
            	/*
        	     if ( Const.isEmpty(getAcceptingStepName()) ) 
           	     {
        	    	 cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XBaseInput.Log.Error.InvalidAcceptingStepName"), stepMeta); //$NON-NLS-1$
        	    	 remarks.add(cr);
                 }
           	
           	     if ( Const.isEmpty(getAcceptingField()) )
           	     {
           	    	cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XBaseInput.Log.Error.InvalidAcceptingFieldName"), stepMeta); //$NON-NLS-1$
           	    	remarks.add(cr);
                 }
                 */
            }
            else
            {		
			    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GMLFileInputMeta.Remark.PleaseSelectFileToUse"), stepMeta); //$NON-NLS-1$
			    remarks.add(cr);
            }
		}
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("GMLFileInputMeta.Remark.FileToUseIsSpecified"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);

            GMLReader gmlr = null;
            try
            {
            	gmlr = new GMLReader(getURLfromFileName(transMeta.environmentSubstitute(gmlFileName)));
            	try {
					gmlr.open();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XMLParsingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("GMLFileInputMeta.Remark.FileExistsAndCanBeOpened"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
                
                RowMetaInterface r = gmlr.getFields();
            
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, r.size()+Messages.getString("GMLFileInputMeta.Remark.OutputFieldsCouldBeDetermined"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
            catch(KettleException ke)
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GMLFileInputMeta.Remark.NoFieldsCouldBeFoundInFileBecauseOfError")+Const.CR+ke.getMessage(), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
//            catch (java.net.MalformedURLException urle)
//            {

        }
	}
	
//	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
//	{
//		return new GISFileInputDialog(shell, info, transMeta, name);
//	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new GMLFileInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new GMLFileInputData();
	}

    
//    public String[] getFilePaths()
//    {
//        return FileInputList.createFilePathList(new String[] { dbfFileName}, new String[] { null }, new String[] { "N" });
//    }
//    

    public FileInputList getTextFileList(VariableSpace space)
    {
        return FileInputList.createFileList(space, new String[] { gmlFileName }, new String[] { null }, new String[] { "N" });
    }

//    public String[] getUsedLibraries()
//    {
//        return new String[] { "javadbf.jar", };
//    }

//    public java.net.URL getGisFileNameURL() {
//    	return getURLfromFileName(getGisFileName());
//    }
    
    private java.net.URL getURLfromFileName(String filename) {
    	try {
    		return (new java.io.File(filename)).toURI().toURL();
    	}
    	catch (java.net.MalformedURLException urle) {
    		// logError(Messages.getString("GISFileInput.Log.Error.MalformedURL"));
    	}
    	return null;
    }
    
}
