package org.pentaho.di.trans.steps.gisfileoutput;

import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.geospatial.GeotoolsWriter;
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
 * @author jmathieu, etdub
 * @since 29-sep-2008
 */
public class GISFileOutput extends BaseStep implements StepInterface {
	private GISFileOutputMeta meta;
	private GISFileOutputData data;

	public GISFileOutput(StepMeta stepMeta,StepDataInterface stepDataInterface, int copyNr,TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean isFileAlreadyCreated(FileObject fo){
		if (data.file_gis.isEmpty())
			return false;
		for (int i=0;i<data.file_gis.size();i++){
			if (fo.equals(data.file_gis.get(i)))
				return true;
		}	
		return false;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)throws KettleException {
		meta = (GISFileOutputMeta) smi;
		data = (GISFileOutputData) sdi;

		Object[] r = getRow(); // this also waits for a previous step to be finished.
		if (r == null){ // no more input to be expected...
			try {
				for (int i=0;i<data.gtwriter.size();i++){
					data.gtwriter.get(i).write();
				}
				return false;
			}catch (Exception e) {
				logError("Because of an error, this step can't continue: ", e);
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}finally {
				for (int i=0;i<data.gtwriter.size();i++){
					data.gtwriter.get(i).close();
				}
			}
		}
		int fileIndex = 0;
		
		if (first) {
			first = false;
			try {
				data.outputRowMeta = getInputRowMeta().clone();
				if(!meta.isFileNameInField())
					data.gtwriter.get(fileIndex).createSimpleFeatureType(data.outputRowMeta, r, data.file_gis.get(fileIndex).getURL());
			}catch (Exception e) {
				logError("Because of an error, this step can't continue: ", e);
				data.gtwriter.get(fileIndex).close();
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
					data.file_gis.add(fo); 
					fileIndex = data.file_gis.indexOf(fo);
					// Create file if it does not exist
					if (!data.file_gis.get(fileIndex).exists()) {
						data.file_gis.get(fileIndex).createFile();
					}
					openNextFile(fileIndex);
					data.outputRowMeta = getInputRowMeta().clone();
					data.gtwriter.get(fileIndex).createSimpleFeatureType(data.outputRowMeta, r, data.file_gis.get(fileIndex).getURL());
				}
				
				fileIndex = data.file_gis.indexOf(fo);			
				data.gtwriter.get(fileIndex).putRow(r);
				incrementLinesOutput();						
			}else{//if only one file, simply put row to the only geotools writer
				try {
					data.gtwriter.get(fileIndex).putRow(r);
					incrementLinesOutput();	
				}catch (Exception e) {
					logError("Because of an error, this step can't continue: ", e);
					data.gtwriter.get(fileIndex).close();
					setErrors(1);
					stopAll();
					setOutputDone(); // signal end to receiver(s)
					return false;
				}
			}
		}catch (Exception e) {
			logError("Error creating gis file from field value", e);
			data.gtwriter.get(fileIndex).close();
			setErrors(1);
			stopAll();
			setOutputDone(); // signal end to receiver(s)
			return false;
		} 
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (GISFileOutputMeta) smi;
		data = (GISFileOutputData) sdi;

		if (super.init(smi, sdi)) {
			try {
				data.file_gis = new ArrayList <FileObject>();
				data.gtwriter = new ArrayList <GeotoolsWriter>();
				if(!meta.isFileNameInField()){
					String fileName = meta.getFileName();
					data.file_gis.add(KettleVFS.getFileObject(fileName)); 
				
					// Create file if it does not exist
					if (!data.file_gis.get(0).exists()) {// 0 -> only one file
						data.file_gis.get(0).createFile();
					}
					openNextFile(0);
				}
			}catch (Exception e) {
				logError("Cannot open/create file " + data.file_gis.get(0).getName().toString(), e);
				return false;
			} 
			return true;
		}
		return false;
	}

	private void openNextFile(int fileIndex) throws KettleException {		
		try {
			data.gtwriter.add(new GeotoolsWriter(data.file_gis.get(fileIndex).getURL()));
			data.gtwriter.get(fileIndex).open();

			logBasic(Messages.getString("GISFileOutput.Log.OpenedGISFile") + " : [" + data.gtwriter.get(fileIndex) + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$	     			
		}catch (Exception e) {
			logError(Messages.getString("GISFileOutput.Log.Error.CouldNotOpenGISFile1") + data.file_gis.get(fileIndex) + Messages.getString("GISFileOutput.Log.Error.CouldNotOpenGISFile2") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			throw new KettleException(e);
		}
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi){
		closeFiles();
		super.dispose(smi, sdi);
	}

	private void closeFiles() {
		logBasic(Messages.getString("GISFileOutput.Log.FinishedReadingRecords")); //$NON-NLS-1$
		for (int i=0;i<data.gtwriter.size();i++){
			data.gtwriter.get(i).close();
		}
	}

	// Run is were the action happens!
	public void run() {
		try {
			logBasic(Messages.getString("GISFileOutput.Log.StartingToRun")); //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data));
		}catch (Exception e) {
			logError(Messages.getString("GISFileOutput.Log.Error.UnexpectedError") + " : " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			logError(Const.getStackTracker(e));
			setErrors(1);
			stopAll();
		}finally {
			dispose(meta, data);
			markStop();
			logSummary();
		}
	}
}
