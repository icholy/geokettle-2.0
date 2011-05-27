package org.pentaho.di.trans.steps.gmlfileinput;

import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.GMLReader;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Reads data from a GML file.
 * 
 * @author jmathieu, tbadard
 * @since 10-may-2010
 */
public class GMLFileInput extends BaseStep implements StepInterface{

	private GMLFileInputMeta meta;
	private GMLFileInputData data;
		
	public GMLFileInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean isFileAlreadyRead(FileObject fo){
		if (data.file_gml.isEmpty())
			return false;
		for (int i=0;i<data.file_gml.size();i++){
			if (fo.equals(data.file_gml.get(i)))
				return true;
		}	
		return false;
	}
	
	public void checkFirst() throws KettleStepException{
        // See if we need to get a list of files from input...
        if (first){
            first = false; 
            data.outputRowMeta = meta.getOutputFields(data.files, getStepname());          
        }
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(GMLFileInputMeta)smi;
		data=(GMLFileInputData)sdi;       
		
		int fileIndex = 0;

		try {
			if(meta.isFileNameInField()){	
				Object[] r = getRow(); // this also waits for a previous step to be finished.
				if (r == null){ // no more input to be expected...
					setOutputDone();
		            return false;
				}
				String fileName = (String) r[getInputRowMeta().indexOfValue(meta.getFileNameField())];
				FileObject fo = KettleVFS.getFileObject(fileName);
				if(!isFileAlreadyRead(fo)){
					data.file_gml.add(fo); 
					fileIndex = data.file_gml.indexOf(fo);
					// Create file if it does not exist
					if (!data.file_gml.get(fileIndex).exists()) {
						data.file_gml.get(fileIndex).createFile();
					}
					createReader(fileIndex);
					
					if (first){
				        first = false; 
				        data.files = new FileInputList();
						data.files.addFile(fo);						
						data.outputRowMeta = meta.getOutputFields(data.files, getStepname());           
				    }
					
					// Allocate the output row in advance, because we possibly want to add a few extra fields...
			        Object[] row = data.gmlreader.get(fileIndex).getRow( RowDataUtil.allocateRowData(data.outputRowMeta.size()) );
			        
			        while (row!=null){
						int outputIndex = data.fields.size();
						
						// Possibly add a row number...
				        if (meta.isRowNrAdded()){
				            row[outputIndex++] = new Long(getLinesInput());
				        }
				
				        putRow(data.outputRowMeta, RowDataUtil.addValueData(row, outputIndex, (String) r[getInputRowMeta().indexOfValue(meta.getFileNameField())]));        // fill the rowset(s). (wait for empty)			     
			        	
				        if (checkFeedback(getLinesInput())) logBasic(Messages.getString("GMLFileInput.Log.LineNr")+getLinesInput()); //$NON-NLS-1$
				
				        if (meta.getRowLimit()>0 && getLinesInput()>=meta.getRowLimit())  // limit has been reached: stop now.
				        {
				            setOutputDone();
				            return false;
				        }
				        row = data.gmlreader.get(fileIndex).getRow( RowDataUtil.allocateRowData(data.outputRowMeta.size()) );
			        }
				}else{
					logBasic(Messages.getString("GMLFileInput.Log.GMLFileAlreadyRead1")+" : ["+data.gmlreader.get(fileIndex)+"]"+Messages.getString("GMLFileInput.Log.GMLFileAlreadyRead2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}	
			}else{
				if (first){
			        first = false; 
			        data.outputRowMeta = meta.getOutputFields(data.files, getStepname());          
			    }
				
				incrementLinesInput();
				Object[] row = data.gmlreader.get(0).getRow( RowDataUtil.allocateRowData(data.outputRowMeta.size()) );
		        
				while (row!=null){
					// OK, so we have read a line: increment the input counter

					int outputIndex = data.fields.size();
			
			        // Possibly add a row number...
			        if (meta.isRowNrAdded()){
			            row[outputIndex++] = new Long(getLinesInput());
			        }
			
			        putRow(data.outputRowMeta, row);        // fill the rowset(s). (wait for empty)

			        if (checkFeedback(getLinesInput())) logBasic(Messages.getString("GMLFileInput.Log.LineNr")+getLinesInput()); //$NON-NLS-1$
			
			        if (meta.getRowLimit()>0 && getLinesInput()>=meta.getRowLimit())  // limit has been reached: stop now.
			        {
			            setOutputDone();
			            return false;
			        }
			        
			        row = data.gmlreader.get(0).getRow( RowDataUtil.allocateRowData(data.outputRowMeta.size()) );
				}      
		        setOutputDone();  // signal end to receiver(s)
		        return false; // end of data or error.	        
			}
		}catch (Exception e) {
			logError("Error reading gml file from field value", e);
			closeFiles();
			setErrors(1);
			stopAll();
			setOutputDone(); // signal end to receiver(s)
			return false;
		}        	
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(GMLFileInputMeta)smi;
		data=(GMLFileInputData)sdi;
		
		if (super.init(smi, sdi)) {
			try {
				data.file_gml = new ArrayList <FileObject>();
				data.gmlreader = new ArrayList <GMLReader>();
				if(!meta.isFileNameInField()){        
					data.files  = meta.getTextFileList(this);				
					data.fileNr = 0;		            
					if (data.files.nrOfFiles()==0){
						logError(Messages.getString("GMLFileInput.Log.Error.NoFilesSpecified"));
						return false;
					}		            
					data.file_gml.add(data.files.getFile(0));
					createReader(0);
				}
			}catch (Exception e) {
				logError("Cannot open/create file " + data.file_gml.get(0).getName().toString(), e);
				return false;
			} 
			return true;
		}
		return false;
	}
	
	private void createReader(int fileIndex) throws KettleException
    {                
        try
        {
        	data.gmlreader.add(new GMLReader(data.file_gml.get(fileIndex).getURL()));
        	data.gmlreader.get(fileIndex).open();

        	logBasic(Messages.getString("GMLFileInput.Log.OpenedGMLFile")+" : ["+data.gmlreader.get(fileIndex)+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        	data.fields = data.gmlreader.get(fileIndex).getFields();
        }
        catch(Exception e)
        {
            logError(Messages.getString("GMLFileInput.Log.Error.CouldNotOpenGMLFile1")+data.file_gml.get(fileIndex)+Messages.getString("GMLFileInput.Log.Error.CouldNotOpenGMLFile2")+e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            throw new KettleException(e);
        }
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
        closeFiles();      
		super.dispose(smi, sdi);
	}

	private void closeFiles()
    {
        logBasic(Messages.getString("GMLFileInput.Log.FinishedReadingRecords")); //$NON-NLS-1$       
    }

    //
	// Run is were the action happens!
	//
	public void run()
	{
		try
		{
			logBasic(Messages.getString("GMLFileInput.Log.StartingToRun"));		 //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data) );
		}
		catch(Exception e)
		{
			logError(Messages.getString("GMLFileInput.Log.Error.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(e));
            setErrors(1);
			stopAll();
		}
		finally
		{
		    dispose(meta, data);
			markStop();
		    logSummary();
		}
	}
	
}
