package org.pentaho.di.trans.steps.kmlfileoutput;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.KMLWriter;
import org.pentaho.di.core.row.RowMetaInterface;
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


public class KMLFileOutputMeta extends BaseStepMeta implements StepMetaInterface
{
	private String 	kmlFileName;
	
//    /** Are we accepting filenames in Output rows?  */
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


	public KMLFileOutputMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	/**
     * @return Returns the kmlFileName.
     */
    public String getKmlFileName()
    {
        return kmlFileName;
    }
    
    /**
     * @param kmlFileName The kmlFileName to set.
     */
    public void setKmlFileName(String kmlFileName)
    {
        this.kmlFileName = kmlFileName;
    }

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	public Object clone()
	{
		KMLFileOutputMeta retval = (KMLFileOutputMeta)super.clone();
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			kmlFileName        = XMLHandler.getTagValue(stepnode, "file_kml"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("KMLFileOutputMeta.Exception.UnableToReadStepInformationFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		kmlFileName    = null;
	}

	public String getXML()
	{
		StringBuffer retval=new StringBuffer();
		
		retval.append("    " + XMLHandler.addTagValue("file_kml",    kmlFileName)); //$NON-NLS-1$ //$NON-NLS-2$
//		retval.append("    " + XMLHandler.addTagValue("include", includeFilename));
//		retval.append("    " + XMLHandler.addTagValue("include_field", filenameField));
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
			kmlFileName              =      rep.getStepAttributeString (id_step, "file_kml"); //$NON-NLS-1$
            
//            includeFilename = rep.getStepAttributeBoolean(id_step, "include");
//            filenameField = rep.getStepAttributeString(id_step, "include_field");
//
//            acceptingFilenames = rep.getStepAttributeBoolean(id_step, "accept_filenames");
//            acceptingField     = rep.getStepAttributeString (id_step, "accept_field");
//            acceptingStepName  = rep.getStepAttributeString (id_step, "accept_stepname");

		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("KMLFileOutputMeta.Exception.UnexpectedErrorReadingMetaDataFromRepository"), e); //$NON-NLS-1$
		}
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "file_kml",        kmlFileName); //$NON-NLS-1$
            
//            rep.saveStepAttribute(id_transformation, id_step, "include", includeFilename);
//            rep.saveStepAttribute(id_transformation, id_step, "include_field", filenameField);
//
//            rep.saveStepAttribute(id_transformation, id_step, "accept_filenames", acceptingFilenames); //$NON-NLS-1$
//            rep.saveStepAttribute(id_transformation, id_step, "accept_field", acceptingField); //$NON-NLS-1$
//            rep.saveStepAttribute(id_transformation, id_step, "accept_stepname", (acceptingStep!=null?acceptingStep.getName():"") ); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("KMLFileOutputMeta.Exception.UnableToSaveMetaDataToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
			String[] Output, String[] output, RowMetaInterface info)
	{
		CheckResult cr;
		
		if (kmlFileName==null)
		{
            if ( false /* isAcceptingFilenames() */ ) 
            {
            	/*
        	     if ( Const.isEmpty(getAcceptingStepName()) ) 
           	     {
        	    	 cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XBaseOutput.Log.Error.InvalidAcceptingStepName"), stepMeta); //$NON-NLS-1$
        	    	 remarks.add(cr);
                 }
           	
           	     if ( Const.isEmpty(getAcceptingField()) )
           	     {
           	    	cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XBaseOutput.Log.Error.InvalidAcceptingFieldName"), stepMeta); //$NON-NLS-1$
           	    	remarks.add(cr);
                 }
                 */
            }
            else
            {		
			    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("KMLFileOutputMeta.Remark.PleaseSelectFileToUse"), stepMeta); //$NON-NLS-1$
			    remarks.add(cr);
            }
		}
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("KMLFileOutputMeta.Remark.FileToUseIsSpecified"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);

            KMLWriter gtr = null;
            try
            {
            	gtr = new KMLWriter(getURLfromFileName(transMeta.environmentSubstitute(kmlFileName)));
            	gtr.open();
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("KMLFileOutputMeta.Remark.FileExistsAndCanBeOpened"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
                
//                RowMetaInterface r = gtr.getFields();
//                RowMetaInterface r = null;
//            
//                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, r.size()+Messages.getString("KMLFileOutputtMeta.Remark.OutputFieldsCouldBeDetermined"), stepMeta); //$NON-NLS-1$
//                remarks.add(cr);
            }
            catch(KettleException ke)
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("KMLFileOutputMeta.Remark.NoFieldsCouldBeFoundInFileBecauseOfError")+Const.CR+ke.getMessage(), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
//            catch (java.net.MalformedURLException urle)
//            {
//                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("KMLFileOutputMeta.Remark.NoFieldsCouldBeFoundInFileBecauseOfError")+Const.CR+urle.getMessage(), stepinfo); //$NON-NLS-1$
//                remarks.add(cr);
//            }
            finally
            {
            	if (gtr != null) gtr.close();
            }
        }
	}
	
//	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
//	{
//		return new KMLFileOutputDialog(shell, info, transMeta, name);
//	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new KMLFileOutput(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new KMLFileOutputData();
	}

    
//    public String[] getFilePaths()
//    {
//        return FileIutputList.createFilePathList(new String[] { dbfFileName}, new String[] { null }, new String[] { "N" });
//    }
//    

    public FileInputList getTextFileList(VariableSpace space)
    {
        return FileInputList.createFileList(space, new String[] { kmlFileName }, new String[] { null }, new String[] { "Y" });
    }

//    public String[] getUsedLibraries()
//    {
//        return new String[] { "javadbf.jar", };
//    }

//    public java.net.URL getKmlFileNameURL() {
//    	return getURLfromFileName(getKmlFileName());
//    }
    
    private java.net.URL getURLfromFileName(String filename) {
    	try {
    		return (new java.io.File(filename)).toURI().toURL();
    	}
    	catch (java.net.MalformedURLException urle) {
    		// logError(Messages.getString("KMLFileOutput.Log.Error.MalformedURL"));
    	}
    	return null;
    }   
}
