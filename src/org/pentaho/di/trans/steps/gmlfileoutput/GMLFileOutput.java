package org.pentaho.di.trans.steps.gmlfileoutput;

import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.geospatial.GMLWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.gmlfileoutput.Messages;

/**
 * Writes data into a GML file.
 * 
 * @author jmathieu, tbadard
 * @since 10-may-2010
 */
public class GMLFileOutput extends BaseStep implements StepInterface {
	private GMLFileOutputMeta meta;
	private GMLFileOutputData data;

	public GMLFileOutput(StepMeta stepMeta,StepDataInterface stepDataInterface, int copyNr,TransMeta transMeta, Trans trans) 
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean isFileAlreadyCreated(FileObject fo){
		if (data.file_gml.isEmpty())
			return false;
		for (int i=0;i<data.file_gml.size();i++){
			if (fo.equals(data.file_gml.get(i)))
				return true;
		}	
		return false;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)throws KettleException 
	{
		meta = (GMLFileOutputMeta) smi;
		data = (GMLFileOutputData) sdi;

		Object[] r = getRow(); // this also waits for a previous step to be
						// finished.
		if (r == null){ // no more input to be expected...
			try {
				for (int i=0;i<data.gmlwriter.size();i++){
					data.gmlwriter.get(i).write();
				}
				return false;
			}catch (Exception e) {
				logError("Because of an error, this step can't continue: ", e);
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}finally {
				for (int i=0;i<data.gmlwriter.size();i++){
					data.gmlwriter.get(i).close();
				}
			}
		}

		int fileIndex = 0;
		
		if (first) 
		{
			first = false;
			try{
				data.outputRowMeta = getInputRowMeta().clone();
				if(!meta.isFileNameInField())
					data.gmlwriter.get(fileIndex).createFeatureType(data.outputRowMeta, r, data.file_gml.get(fileIndex).getURL());
			} catch (Exception e) {
				logError("Because of an error, this step can't continue: ", e);
				data.gmlwriter.get(fileIndex).close();
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}
		}

		try {
			if(meta.isFileNameInField()){	
				String fileName = (String) r[getInputRowMeta().indexOfValue(meta.getFileNameField())];
				FileObject fo = KettleVFS.getFileObject(fileName);
				if(!isFileAlreadyCreated(fo)){
					data.file_gml.add(fo); 
					fileIndex = data.file_gml.indexOf(fo);
					// Create file if it does not exist
					if (!data.file_gml.get(fileIndex).exists()) {
						data.file_gml.get(fileIndex).createFile();
					}
					openNextFile(fileIndex);
					data.outputRowMeta = getInputRowMeta().clone();
					data.gmlwriter.get(fileIndex).createFeatureType(data.outputRowMeta, r, data.file_gml.get(fileIndex).getURL());
				}
				fileIndex = data.file_gml.indexOf(fo);			
				data.gmlwriter.get(fileIndex).putRow(r);
				incrementLinesOutput();	
			}else{//if only one file, simply put row to the only gml writer
				try {
					data.gmlwriter.get(fileIndex).putRow(r);
					incrementLinesOutput();	
				}catch (Exception e) {
					logError("Because of an error, this step can't continue: ", e);
					data.gmlwriter.get(fileIndex).close();
					setErrors(1);
					stopAll();
					setOutputDone(); // signal end to receiver(s)
					return false;
				}
			}
		}catch (Exception e) {
			logError("Error creating gml file from field value", e);
			data.gmlwriter.get(fileIndex).close();
			setErrors(1);
			stopAll();
			setOutputDone(); // signal end to receiver(s)
			return false;
		} 
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (GMLFileOutputMeta) smi;
		data = (GMLFileOutputData) sdi;

		if (super.init(smi, sdi)) {
			try {
				data.file_gml = new ArrayList <FileObject>();
				data.gmlwriter = new ArrayList <GMLWriter>();
				if(!meta.isFileNameInField()){
					String fileName = meta.getFileName();
					data.file_gml.add(KettleVFS.getFileObject(fileName)); 
				
					// Create file if it does not exist
					if (!data.file_gml.get(0).exists()) {// 0 -> only one file
						data.file_gml.get(0).createFile();
					}
					openNextFile(0);
				}
			}catch (Exception e) {
				logError("Cannot open/create file " + data.file_gml.get(0).getName().toString(), e);
				return false;
			} 
			return true;
		}
		return false;
	}

	private void openNextFile(int fileIndex) throws KettleException {
		try {
			data.gmlwriter.add(new GMLWriter(data.file_gml.get(fileIndex).getURL()));
			data.gmlwriter.get(fileIndex).open();

			logBasic(Messages.getString("GMLFileOutput.Log.OpenedGMLFile") + " : [" + data.gmlwriter.get(fileIndex) + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$	     			
		}catch (Exception e) {
			logError(Messages.getString("GMLFileOutput.Log.Error.CouldNotOpenGMLFile1") + data.file_gml.get(fileIndex) + Messages.getString("GMLFileOutput.Log.Error.CouldNotOpenGMLFile2") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
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
		logBasic(Messages.getString("GMLFileOutput.Log.FinishedReadingRecords")); //$NON-NLS-1$
		for (int i=0;i<data.gmlwriter.size();i++){
			data.gmlwriter.get(i).close();
		}
	}

	// Run is were the action happens!

	public void run() 
	{
		try 
		{
			logBasic(Messages.getString("GMLFileOutput.Log.StartingToRun")); //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data));
		} 
		catch (Exception e) 
		{
			logError(Messages.getString("GMLFileOutput.Log.Error.UnexpectedError") + " : " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
