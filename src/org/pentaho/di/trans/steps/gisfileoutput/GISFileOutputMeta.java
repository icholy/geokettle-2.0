package org.pentaho.di.trans.steps.gisfileoutput;

import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.geospatial.GeotoolsWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.xbaseinput.Messages;
import org.pentaho.di.ui.trans.steps.gisfileoutput.GISFileOutputDialog;
import org.w3c.dom.Node;


public class GISFileOutputMeta extends BaseStepMeta implements StepMetaInterface{

	private  String  fileName; 
	private boolean isFileNameInField;
	private String fileNameField;
	private String directory;

	public GISFileOutputMeta(){
		super(); // allocate BaseStepMeta
	}
	
	public void setDirectory(String dir){
		this.directory=dir;
	}
	
	public String getDirectory(){
        return directory;
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

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	public Object clone(){
		GISFileOutputMeta retval = (GISFileOutputMeta)super.clone();
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException{
		try{	
			directory     = XMLHandler.getTagValue(stepnode, "directory");
			fileNameField     = XMLHandler.getTagValue(stepnode, "filenamefield");
			isFileNameInField  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "isfilenameinfield"));			
			fileName    = XMLHandler.getTagValue(stepnode, "filename");
		}
		catch(Exception e){
			throw new KettleXMLException(Messages.getString("GISFileOutputMeta.Exception.UnableToReadStepInformationFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault(){
		fileName    = null;
		fileNameField = null;
		directory=null;
		isFileNameInField = false;
	}

	public String getXML(){
		StringBuffer retval=new StringBuffer();	
		retval.append("    ").append(XMLHandler.addTagValue("filename", fileName));
		retval.append("    ").append(XMLHandler.addTagValue("isfilenameinfield", isFileNameInField));
		retval.append("    ").append(XMLHandler.addTagValue("filenamefield", fileNameField));  	
		retval.append("    ").append(XMLHandler.addTagValue("directory", directory)); 
        return retval.toString();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException{
		try{
			fileName    = rep.getStepAttributeString (id_step, "filename");
			isFileNameInField   = rep.getStepAttributeBoolean(id_step, "isfilenameinfield");	
			fileNameField     = rep.getStepAttributeString (id_step, "filenamefield");
			directory     = rep.getStepAttributeString (id_step, "directory");
		}
		catch(Exception e){
			throw new KettleException(Messages.getString("GISFileOutputMeta.Exception.UnexpectedErrorReadingMetaDataFromRepository"), e); //$NON-NLS-1$
		}
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException{
		try{
			rep.saveStepAttribute(id_transformation, id_step, "directory", directory);
			rep.saveStepAttribute(id_transformation, id_step, "filenamefield", fileNameField);
			rep.saveStepAttribute(id_transformation, id_step, "filename", fileName);
			rep.saveStepAttribute(id_transformation, id_step, "isfilenameinfield", isFileNameInField);
		}
		catch(Exception e){
			throw new KettleException(Messages.getString("GISFileOutputMeta.Exception.UnableToSaveMetaDataToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
			String[] Output, String[] output, RowMetaInterface info){
		CheckResult cr;
		
		if (directory == null){
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GISFileOutputMeta.Remark.PleaseSelectOutputDirectory"), stepMeta); //$NON-NLS-1$
		    remarks.add(cr);	
		}else if (!isFileNameInField){
			if (fileName ==null){
			    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GISFileOutputMeta.Remark.PleaseSelectFileToUse"), stepMeta); //$NON-NLS-1$
			    remarks.add(cr);
			}
		}else if (fileNameField == null){
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GISFileOutputMeta.Remark.PleaseSelectFileField"), stepMeta); //$NON-NLS-1$
		    remarks.add(cr);
		}else{	
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("GISFileOutputMeta.Remark.FileToUseIsSpecified"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);

            GeotoolsWriter gtr = null;
            try{       	
            	gtr = new GeotoolsWriter(getURLfromfileName(transMeta.environmentSubstitute(fileName)));
            	gtr.open();
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("GISFileOutputMeta.Remark.FileExistsAndCanBeOpened"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);          	
            }
            catch(KettleException ke){
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GISFileOutputMeta.Remark.NoFieldsCouldBeFoundInFileBecauseOfError")+Const.CR+ke.getMessage(), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
            finally{
            	if (gtr != null) gtr.close();
            }
        }
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name){
		return new GISFileOutputDialog(shell, info, transMeta, name);
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans){
		return new GISFileOutput(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData(){
		return new GISFileOutputData();
	} 
    
    public static String getFilename(FileObject fileObject)
    {
        FileName fileName = fileObject.getName();
        String root = fileName.getRootURI();
        if(!root.startsWith("file:")) return fileName.getURI();
        if(root.endsWith(":/"))
            root = root.substring(8, 10);
        else
            root = root.substring(7, root.length() - 1);
        String fileString = root + fileName.getPath();
        if(!"/".equals(Const.FILE_SEPARATOR)) fileString = Const.replace(fileString, "/", Const.FILE_SEPARATOR);
        return fileString;
    }
    
    private java.net.URL getURLfromfileName(String fileName) throws KettleException {
    	try {
    		return (new java.io.File(fileName)).toURI().toURL();
    	}
    	catch (java.net.MalformedURLException urle) {
    		throw new KettleException(Messages.getString("GISFileOutput.Log.Error.MalformedURL"), urle); //$NON-NLS-1$
    	}
    }   
}
