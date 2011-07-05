package org.pentaho.di.trans.steps.gmlfileoutput;

import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.geospatial.GMLWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Writes data into a GML file.
 * 
 * @author jmathieu, tbadard
 * @since 10-may-2010
 */
public class GMLFileOutput extends BaseStep implements StepInterface {
	private GMLFileOutputMeta meta;
	private GMLFileOutputData data;

	public GMLFileOutput(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	private boolean createWriter(Object[] r) {
		closeFile();
		try {
			data.gmlWriter = new GMLWriter(data.file.getURL());
			data.gmlWriter.open();
		} catch (FileSystemException e) {
			logError("Cannot get file URL.", e);
			return false;
		} catch (KettleException e) {
			logError("Could not open reader.", e);
			return false;
		}
		try {
			data.gmlWriter.createFeatureType(data.outputRowMeta, r);
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
		meta = (GMLFileOutputMeta) smi;
		data = (GMLFileOutputData) sdi;

		Object[] r = getRowPreviousStep();

		if (r == null) {
			setOutputDone();
			return false;
		} else {
			if (first) {
				first = false;
				if (!meta.isFileNameInField()) {
					data.outputRowMeta = getInputRowMeta().clone();
					createWriter(r);
				} else
					data.outputRowMeta = data.rowSet.getRowMeta();
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

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (GMLFileOutputMeta) smi;
		data = (GMLFileOutputData) sdi;
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

	private boolean writeRowToFile(Object[] r) {
		try {
			data.gmlWriter.putRow(r);
		} catch (KettleException e) {
			logError("Could not write row.", e);
			return false;
		}
		return true;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		closeFile();
		super.dispose(smi, sdi);
	}

	private void closeFile() {
		try {
			if (data.file != null)
				data.file.close();
		} catch (FileSystemException e) {
			logError("Error closing file", e);
		}
		if (data.gmlWriter != null) {
			incrementLinesOutput();
			try {
				data.gmlWriter.close();
			} catch (KettleException e) {
				logError("Error closing writer.", e);
			}
		}
	}

	// Run is were the action happens!
	public void run() {
		try {
			logBasic(Messages.getString("GMLFileOutput.Log.StartingToRun")); //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data))
				;
		} catch (Exception e) {
			logError(Messages
					.getString("GMLFileOutput.Log.Error.UnexpectedError") + " : " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			logError(Const.getStackTracker(e));
			setErrors(1);
			stopAll();
		} finally {
			dispose(meta, data);
			markStop();
			logSummary();
		}
	}
}
