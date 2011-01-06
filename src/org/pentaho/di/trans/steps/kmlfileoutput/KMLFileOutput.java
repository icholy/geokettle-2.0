package org.pentaho.di.trans.steps.kmlfileoutput;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.geospatial.KMLWriter;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.kmlfileoutput.Messages;

/**
 * Writes data to a KML file.
 * 
 * @author tpelletierdoisy, eleprince, tbadard, jmathieu
 * @since 29-oct-2009
 */

public class KMLFileOutput extends BaseStep implements StepInterface {
	private KMLFileOutputMeta meta;
	private KMLFileOutputData data;

	public KMLFileOutput(StepMeta stepMeta,StepDataInterface stepDataInterface, int copyNr,TransMeta transMeta, Trans trans) 
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean isFileAlreadyCreated(FileObject fo){
		if (data.file_kml.isEmpty())
			return false;
		for (int i=0;i<data.file_kml.size();i++){
			if (fo.equals(data.file_kml.get(i)))
				return true;
		}	
		return false;
	}
	 
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)throws KettleException 
	{
		meta = (KMLFileOutputMeta) smi;
		data = (KMLFileOutputData) sdi;

		Object[] r = getRow(); 
		
		if (r == null){ // no more input to be expected...
			try {
				for (int i=0;i<data.kmlwriter.size();i++){
					data.kmlwriter.get(i).write();
				}
				return false;
			}catch (Exception e) {
				logError("Because of an error, this step can't continue: ", e);
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}finally {
				try {
					for (int i=0;i<data.kmlwriter.size();i++){					
						data.kmlwriter.get(i).kml.marshal(data.kmlwriter.get(i).file);
						data.kmlwriter.get(i).close();
					}
				} catch (FileNotFoundException e) {
					logError(Messages.getString("KMLFileOutput.Log.Error.CouldNotOpenKMLFile1") + data.file_kml + Messages.getString("KMLFileOutput.Log.Error.CouldNotOpenXBaseFile2") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleException(e);
				}
			}
		}

		int fileIndex = 0;
		
		if (first) 
		{
			first = false;
			try{
				data.outputRowMeta = getInputRowMeta().clone();
				
				if (meta.isExportingFeatureName()){
					if (!meta.getFeatureNameField().equals(Messages.getString("KMLFileOutputDialog.NoField.Text")))
						data.outputRowMeta.setValueMeta(getInputRowMeta().indexOfValue(meta.getFeatureNameField()), new ValueMeta("name", ValueMeta.TYPE_STRING));					
					else{
						ValueMeta name = (ValueMeta) data.outputRowMeta.searchValueMeta("name");
						if(name==null)
							data.outputRowMeta.addValueMeta(new ValueMeta("name", ValueMeta.TYPE_STRING));
					}
				}
				
				if (meta.isExportingFeatureDesc()){
					if (!meta.getFeatureDescField().equals(Messages.getString("KMLFileOutputDialog.NoField.Text")))
						data.outputRowMeta.setValueMeta(getInputRowMeta().indexOfValue(meta.getFeatureDescField()), new ValueMeta("description", ValueMeta.TYPE_STRING));					
					else{
						ValueMeta description = (ValueMeta) data.outputRowMeta.searchValueMeta("description");
						if(description==null)
							data.outputRowMeta.addValueMeta(new ValueMeta("description", ValueMeta.TYPE_STRING));
					}
				}
				
				data.kmlwriter.get(fileIndex).visionTable(data.outputRowMeta);
				
			} catch (Exception e) {
				logError("Because of an error, this step can't continue: ", e);
				data.kmlwriter.get(fileIndex).close();
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
					data.file_kml.add(fo); 
					fileIndex = data.file_kml.indexOf(fo);
					// Create file if it does not exist
					if (!data.file_kml.get(fileIndex).exists()) {
						data.file_kml.get(fileIndex).createFile();
					}
					openNextFile(fileIndex);
					data.outputRowMeta = getInputRowMeta().clone();
				}
				fileIndex = data.file_kml.indexOf(fo);				
				
				if(!meta.isExportingFeatureName()){
					ValueMeta name = (ValueMeta) data.outputRowMeta.searchValueMeta("name");
					if (name!=null){
						int index = data.outputRowMeta.indexOfValue("name");
						r[index]=null;
					}
				}else if (meta.getFeatureNameField().equals(Messages.getString("KMLFileOutputDialog.NoField.Text"))){
					int index = data.outputRowMeta.indexOfValue("name");
					r[index]="";
				}
				
				if(!meta.isExportingFeatureDesc()){
					ValueMeta description = (ValueMeta) data.outputRowMeta.searchValueMeta("description");
					if (description!=null){
						int index = data.outputRowMeta.indexOfValue("description");
						r[index]=null;
					}
				}else if (meta.getFeatureDescField().equals(Messages.getString("KMLFileOutputDialog.NoField.Text"))){
					int index = data.outputRowMeta.indexOfValue("description");
					r[index]="";
				}

				
				data.kmlwriter.get(fileIndex).createNewPlacemark(r);
				incrementLinesOutput();		
			}else{//if only one file, simply put row to the only geotools writer
				try {
					if(!meta.isExportingFeatureName()){
						ValueMeta name = (ValueMeta) data.outputRowMeta.searchValueMeta("name");
						if (name!=null){
							int index = data.outputRowMeta.indexOfValue("name");
							r[index]=null;
						}
					}else if (meta.getFeatureNameField().equals(Messages.getString("KMLFileOutputDialog.NoField.Text"))){
						int index = data.outputRowMeta.indexOfValue("name");
						r[index]="";
					}
					
					if(!meta.isExportingFeatureDesc()){
						ValueMeta description = (ValueMeta) data.outputRowMeta.searchValueMeta("description");
						if (description!=null){
							int index = data.outputRowMeta.indexOfValue("description");
							r[index]=null;
						}
					}else if (meta.getFeatureDescField().equals(Messages.getString("KMLFileOutputDialog.NoField.Text"))){
						int index = data.outputRowMeta.indexOfValue("description");
						r[index]="";
					}
					data.kmlwriter.get(fileIndex).createNewPlacemark(r);
					incrementLinesOutput();
				}catch (Exception e) {
					logError("Because of an error, this step can't continue: ", e);
					data.kmlwriter.get(fileIndex).close();
					setErrors(1);
					stopAll();
					setOutputDone(); // signal end to receiver(s)
					return false;
				}
			}
		}catch (Exception e) {
			logError("Error creating kml file from field value", e);
			data.kmlwriter.get(fileIndex).close();
			setErrors(1);
			stopAll();
			setOutputDone(); // signal end to receiver(s)
			return false;
		} 	
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (KMLFileOutputMeta) smi;
		data = (KMLFileOutputData) sdi;

		if (super.init(smi, sdi)) {
			try {
				data.file_kml = new ArrayList <FileObject>();
				data.kmlwriter = new ArrayList <KMLWriter>();
				if(!meta.isFileNameInField()){
					String fileName = meta.getFileName();
					data.file_kml.add(KettleVFS.getFileObject(fileName)); 
				
					// Create file if it does not exist
					if (!data.file_kml.get(0).exists()) {// 0 -> only one file
						data.file_kml.get(0).createFile();
					}
					openNextFile(0);
				}
			}catch (Exception e) {
				logError("Cannot open/create file " + data.file_kml.get(0).getName().toString(), e);
				return false;
			} 
			return true;
		}
		return false;
	}

	private void openNextFile(int fileIndex) throws KettleException {
		try {
			data.kmlwriter.add(new KMLWriter(data.file_kml.get(fileIndex).getURL()));
			data.kmlwriter.get(fileIndex).open();

			logBasic(Messages.getString("KMLFileOutput.Log.OpenedKMLFile") + " : [" + data.kmlwriter.get(fileIndex) + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$	     		        	
		} 
		catch (Exception e) 
		{
			logError(Messages.getString("KMLFileOutput.Log.Error.CouldNotOpenKMLFile1") + data.file_kml.get(fileIndex) + Messages.getString("KMLFileOutput.Log.Error.CouldNotOpenKMLFile2") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
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
		logBasic(Messages.getString("KMLFileOutput.Log.FinishedReadingRecords")); //$NON-NLS-1$
		for (int i=0;i<data.kmlwriter.size();i++){
			data.kmlwriter.get(i).close();
		}
	}

	// Run is were the action happens!
	public void run() 
	{
		try 
		{
			logBasic(Messages.getString("KMLFileOutput.Log.StartingToRun")); //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data));
		} 
		catch (Exception e) 
		{
			logError(Messages.getString("KMLFileOutput.Log.Error.UnexpectedError") + " : " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			logError(Const.getStackTracker(e));
			setErrors(1);
			stopAll();
		} 
		finally{ 
			try {
				//ajouter par tpelletierdoisy afin de serialiser le Kml apres remplissage
				if (!meta.isFileNameInField())
					data.kmlwriter.get(0).kml.marshal(data.kmlwriter.get(0).file);
			} catch (FileNotFoundException e) {
				logError(Messages.getString("KMLFileOutput.Log.Error.CouldNotOpenKMLFile1") + data.file_kml + Messages.getString("KMLFileOutput.Log.Error.CouldNotOpenXBaseFile2") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			dispose(meta, data);
			markStop();
			logSummary();
		}
	}
}