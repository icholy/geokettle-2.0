package org.pentaho.di.trans.steps.kmlfileoutput;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
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

/**
 * Writes data to a KML file.
 * 
 * @author tpelletierdoisy, eleprince, tbadard, jmathieu
 * @since 29-oct-2009
 */
public class KMLFileOutput extends BaseStep implements StepInterface {
	private static final String STRING_EMPTY = "";
	private static final String PROP_NAME = "name";
	private static final String PROP_DESC = "description";
	
	private KMLFileOutputMeta meta;
	private KMLFileOutputData data;

	public KMLFileOutput(StepMeta stepMeta,StepDataInterface stepDataInterface, int copyNr,TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	private boolean createWriter(Object[] r) {
		closeFile();
		try {
			data.kmlWriter = new KMLWriter(data.file.getURL());
			data.kmlWriter.open();
		} catch (FileSystemException e) {
			logError("Cannot get file URL.", e);
			return false;
		} catch (KettleException e) {
			logError("Could not open reader.", e);
			return false;
		}
		try {
			data.kmlWriter.visionTable(data.outputRowMeta);
		} catch (KettleException e) {
			logError("Could not create feature type.", e);
			return false;
		}
		return true;
	}
	
	private Object[] getRowPreviousStep() throws KettleException {
		try {
			return !meta.isFileNameInField() ? getRow()
					: getRowFrom(data.rowSet);
		} catch (Exception e) {
			throw new KettleException("Cannot create file object.", e);
		}
	}
	 
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
	throws KettleException {
		meta = (KMLFileOutputMeta) smi;
		data = (KMLFileOutputData) sdi;
		
		Object[] r = getRowPreviousStep();
		
		if (r == null) {
			setOutputDone();
			return false;
		} else {
			if (first) {
				first = false;
				data.outputRowMeta = meta.isFileNameInField()?data.rowSet.getRowMeta():getInputRowMeta().clone();
				
				if (meta.isExportingFeatureName() && meta.getFeatureNameField()!=null){
					if (!meta.getFeatureNameField().equals(Messages.getString("KMLFileOutputDialog.NoField.Text")))
						data.outputRowMeta.setValueMeta(data.outputRowMeta.indexOfValue(meta.getFeatureNameField()), new ValueMeta(PROP_NAME, ValueMeta.TYPE_STRING));					
					else{
						if(data.outputRowMeta.searchValueMeta(PROP_NAME) == null)
							data.outputRowMeta.addValueMeta(new ValueMeta(PROP_NAME, ValueMeta.TYPE_STRING));
					}
				}
				
				if (meta.isExportingFeatureDesc() && meta.getFeatureDescField()!=null){
					if (!meta.getFeatureDescField().equals(Messages.getString("KMLFileOutputDialog.NoField.Text")))
						data.outputRowMeta.setValueMeta(data.outputRowMeta.indexOfValue(meta.getFeatureDescField()), new ValueMeta(PROP_DESC, ValueMeta.TYPE_STRING));					
					else{
						if(data.outputRowMeta.searchValueMeta(PROP_DESC)==null)
							data.outputRowMeta.addValueMeta(new ValueMeta(PROP_DESC, ValueMeta.TYPE_STRING));
					}
				}
				
				if (!meta.isFileNameInField())
					createWriter(r);
			}
		
			if (meta.isFileNameInField()
					&& openFile(r[data.outputRowMeta.indexOfValue(meta
							.getFileNameField())].toString()))
				createWriter(r);
		
			if (!writeRowToFile(r)) {
				setErrors(1);
				stopAll();
				return false;
			}
		
			putRow(data.outputRowMeta, r);
		
			if (checkFeedback(getLinesOutput()) && log.isBasic())
				logBasic("linenr " + getLinesOutput());
		
			return true;
		}
	}
	
	public boolean openFile(String fileName) {
		try {
			FileObject fo;
			try {
				fo = KettleVFS.getFileObject(fileName);
			} catch (IOException e) {
				logError("Cannot create file object.", e);
				return false;
			}
			if (data.file == null || !data.file.getURL().equals(fo.getURL())) {
				data.file = fo;
				return true;
			}
		} catch (FileSystemException e) {
			logError("Cannot open/create file ", e);
			return false;
		}
		return false;
	}
	
	private void closeFile() {
		try {
			if (data.file != null)
				data.file.close();
		} catch (FileSystemException e) {
			logError("Error closing file", e);
		}
		if (data.kmlWriter != null) {
			try {
				data.kmlWriter.write();
			} catch (KettleException e) {
				logError("Error writing to file", e);
			}
			try {
				data.kmlWriter.kml.marshal(data.kmlWriter.file);
			} catch (FileNotFoundException e) {
				logError("Error closing file.", e);
			}
			incrementLinesOutput();
			data.kmlWriter.close();
		}
	}
	
	private boolean writeRowToFile(Object[] r) {
		int index_name = data.outputRowMeta.indexOfValue(PROP_NAME);
		if(!meta.isExportingFeatureName()){
			if(data.outputRowMeta.searchValueMeta(PROP_NAME)!=null)		
				r[index_name]=null;
		}else if (meta.getFeatureNameField()!=null && meta.getFeatureNameField().equals(Messages.getString("KMLFileOutputDialog.NoField.Text")))
			r[index_name] = STRING_EMPTY;
		
		int index_desc = data.outputRowMeta.indexOfValue(PROP_DESC);
		if(!meta.isExportingFeatureDesc()){
			if(data.outputRowMeta.searchValueMeta(PROP_DESC)!=null)
				r[index_desc] = null;			
		}else if (meta.getFeatureDescField()!=null && meta.getFeatureDescField().equals(Messages.getString("KMLFileOutputDialog.NoField.Text")))
			r[index_desc] = STRING_EMPTY;
		
		try {
			data.kmlWriter.createNewPlacemark(r);
		} catch (FileNotFoundException e) {
			logError("Could not write row.", e);
			return false;
		}
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (KMLFileOutputMeta) smi;
		data = (KMLFileOutputData) sdi;
		if (super.init(smi, sdi)) {
			if (!meta.isFileNameInField()) {
				try {
					data.file = KettleVFS
							.getFileObject(environmentSubstitute(meta
									.getFileName()));
				} catch (IOException e) {
					logError("Cannot create file object.", e);
					return false;
				}
			} else {
				try {
					data.rowSet = findInputRowSet(meta.getAcceptingStepName());
				} catch (KettleStepException e) {
					logError("Could not retrieve filen", e);
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi){
		closeFile();
		super.dispose(smi, sdi);
	}

	// Run is were the action happens!
	public void run(){
		try{
			logBasic(Messages.getString("KMLFileOutput.Log.StartingToRun")); //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data));
		}catch (Exception e) {
			logError(Messages.getString("KMLFileOutput.Log.Error.UnexpectedError") + " : " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			logError(Const.getStackTracker(e));
			setErrors(1);
			stopAll();
		}finally{ 
			dispose(meta, data);
			markStop();
			logSummary();
		}
	}
}