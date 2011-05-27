package org.pentaho.di.trans.steps.gisfileinput;

// import java.io.IOException;

import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.GeotoolsReader;
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
 * Reads data from an GIS file.
 * 
 * @author etdub,jmathieu & tbadard
 * @since 29-sep-2008
 */
public class GISFileInput extends BaseStep implements StepInterface
{
	private GISFileInputMeta meta;
	private GISFileInputData data;

	public GISFileInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean isFileAlreadyRead(FileObject fo){
		if (data.file_gis.isEmpty())
			return false;
		for (int i=0;i<data.file_gis.size();i++){
			if (fo.equals(data.file_gis.get(i)))
				return true;
		}	
		return false;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException{
		meta=(GISFileInputMeta)smi;
		data=(GISFileInputData)sdi;

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
					data.file_gis.add(fo); 
					fileIndex = data.file_gis.indexOf(fo);
					// Create file if it does not exist
					if (!data.file_gis.get(fileIndex).exists()) 
						data.file_gis.get(fileIndex).createFile();

					data.charset.add(meta.getGisFileCharset());

					createReader(fileIndex);

					if (first){ // we just got started
						first = false; 
						data.files = new FileInputList();
						data.files.addFile(fo);						
						data.outputRowMeta = meta.getOutputFields(data.files, getStepname());          
					}

					// Allocate the output row in advance, because we possibly want to add a few extra fields...
					Object[] row = data.gtreader.get(fileIndex).getRow( RowDataUtil.allocateRowData(data.outputRowMeta.size()) );

					while (row!=null){
						int outputIndex = data.fields.size();

						// Possibly add a row number...
						if (meta.isRowNrAdded()){
							row[outputIndex++] = new Long(getLinesInput());
						}

						putRow(data.outputRowMeta, RowDataUtil.addValueData(row, outputIndex, (String) r[getInputRowMeta().indexOfValue(meta.getFileNameField())]));        // fill the rowset(s). (wait for empty)				        

						if (checkFeedback(getLinesInput())) logBasic(Messages.getString("GISFileInput.Log.LineNr")+getLinesInput()); //$NON-NLS-1$

						if (meta.getRowLimit()>0 && getLinesInput()>=meta.getRowLimit())  // limit has been reached: stop now.
						{
							setOutputDone();
							return false;
						}
						row = data.gtreader.get(fileIndex).getRow( RowDataUtil.allocateRowData(data.outputRowMeta.size()) );
					}
				}else{
					logBasic(Messages.getString("GISFileInput.Log.GISFileAlreadyRead1")+" : ["+data.gtreader.get(fileIndex)+"]"+Messages.getString("GISFileInput.Log.GISFileAlreadyRead2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}

			}else{
				if (first){ // we just got started
					first = false; 
					data.outputRowMeta = meta.getOutputFields(data.files, getStepname());          
				}

				incrementLinesInput();
				Object[] row = data.gtreader.get(0).getRow( RowDataUtil.allocateRowData(data.outputRowMeta.size()) );

				while (row!=null){
					// OK, so we have read a line: increment the input counter
					int outputIndex = data.fields.size();

					// Possibly add a row number...
					if (meta.isRowNrAdded()){
						row[outputIndex++] = new Long(getLinesInput());
					}

					putRow(data.outputRowMeta, row);        // fill the rowset(s). (wait for empty)

					if (checkFeedback(getLinesInput())) logBasic(Messages.getString("GISFileInput.Log.LineNr")+getLinesInput()); //$NON-NLS-1$

					if (meta.getRowLimit()>0 && getLinesInput()>=meta.getRowLimit())  // limit has been reached: stop now.
					{
						setOutputDone();
						return false;
					}

					row = data.gtreader.get(0).getRow( RowDataUtil.allocateRowData(data.outputRowMeta.size()) );
				}      
				setOutputDone();  // signal end to receiver(s)
				return false; // end of data or error.	        
			}
		}catch (Exception e) {
			logError("Error reading gis file from field value", e);
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
		meta=(GISFileInputMeta)smi;
		data=(GISFileInputData)sdi;

		if (super.init(smi, sdi)) {
			try {
				data.file_gis = new ArrayList <FileObject>();
				data.gtreader = new ArrayList <GeotoolsReader>();
				data.charset = new ArrayList <String>();
				if(!meta.isFileNameInField()){        
					data.files  = meta.getTextFileList(this);				
					data.fileNr = 0;		            
					if (data.files.nrOfFiles()==0){
						logError(Messages.getString("GISFileInput.Log.Error.NoFilesSpecified"));
						return false;
					}		            
					data.file_gis.add(data.files.getFile(0));
					if (Const.isEmpty(meta.getGisFileCharset()))
						meta.setGisFileCharset("ISO-8859-1");

					data.charset.add(meta.getGisFileCharset());
					createReader(0);
				}
			} catch (Exception e) {
				logError("Cannot open/create file " + data.file_gis.get(0).getName().toString(), e);
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
			data.gtreader.add(new GeotoolsReader(data.file_gis.get(fileIndex).getURL(),data.charset.get(fileIndex)));
			data.gtreader.get(fileIndex).open();

			logBasic(Messages.getString("GISFileInput.Log.OpenedGISFile")+" : ["+data.gtreader.get(fileIndex)+" ("+data.charset.get(fileIndex)+")]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			data.fields = data.gtreader.get(fileIndex).getFields();
		}catch(Exception e){
			logError(Messages.getString("GISFileInput.Log.Error.CouldNotOpenGISFile1")+data.file_gis.get(fileIndex)+Messages.getString("GISFileInput.Log.Error.CouldNotOpenGISFile2")+e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			throw new KettleException(e);
		}
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi){
		closeFiles();     
		super.dispose(smi, sdi);
	}

	private void closeFiles() {
		logBasic(Messages.getString("GISFileInput.Log.FinishedReadingRecords")); //$NON-NLS-1$
		for (int i=0;i<data.gtreader.size();i++){
			data.gtreader.get(i).close();
		}
	}

	// Run is were the action happens!
	public void run(){
		try{
			logBasic(Messages.getString("GISFileInput.Log.StartingToRun"));		 //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data) );
		}catch(Exception e){
			logError(Messages.getString("GISFileInput.Log.Error.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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