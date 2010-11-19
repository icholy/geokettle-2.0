package org.pentaho.di.trans.steps.gmlfileoutput;

import java.io.File;
import java.io.IOException;

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

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)throws KettleException 
	{
		meta = (GMLFileOutputMeta) smi;
		data = (GMLFileOutputData) sdi;

		Object[] r = getRow(); // this also waits for a previous step to be
						// finished.
		if (r == null) // no more input to be expected...
		{
			try{
				data.gmlwriter.write();
				return false;
			} 
			catch (Exception e){
				logError("Because of an error, this step can't continue: ", e);
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}
		}

		if (first) 
		{
			first = false;
			data.outputRowMeta = getInputRowMeta().clone();

			try 
			{
				data.gmlwriter.createFeatureType(data.outputRowMeta, r,
						data.file_gml.getURL());
			} 
			catch (Exception e) 
			{
				logError("Because of an error, this step can't continue: ", e);
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}
			// meta.getFields(data.outputRowMeta, getStepname(), null, null,
			// this);
			// data.insertRowMeta = getInputRowMeta().clone();
		}

		try 
		{
			data.gmlwriter.putRow(r);
			/*
			 * Object[] outputRowData = writeToTable(getInputRowMeta(), r); if
			 * (outputRowData!=null) { putRow(data.outputRowMeta,
			 * outputRowData); // in case we want it go further...
			 * incrementLinesOutput(); } if (checkFeedback(getLinesRead())) {
			 * if(log.isBasic()) logBasic("linenr "+getLinesRead()); }
			 */
		} 
		catch (Exception e) 
		{
			logError("Because of an error, this step can't continue: ", e);
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
			data.files = meta.getTextFileList(this);
			data.fileNr = 0;

			if (data.files.nrOfFiles() != 0 /* && !meta.isAcceptingFilenames() */) {
				data.file_gml = data.files.getFile(0);

				// TODO: do you want to overwrite??
			}else{
				try{
					File file = new File(meta.getGmlFileName());

					// Create file if it does not exist
					boolean success = file.createNewFile();
					if (success) {
						// File did not exist and was created
					} else {
						// File already exists
					}
					FileObject fileObj = KettleVFS.getFileObject(meta
							.getGmlFileName());
					data.files.addFile(fileObj);
					data.file_gml = fileObj;
				} 			
				catch (IOException e) 
				{
				}
			}
			// try {
			// // java.net.URL fileURL = (new
			// java.io.File(meta.getGisFileName())).toURI().toURL();
			// }
			// catch(java.net.MalformedURLException urle) {
			// catch(Exception e) {
			// // TODO: change for another error message
			// logError(Messages.getString("GISFileOutput.Log.Error.MalformedURL"));
			// }
			return true;
		}
		return false;
	}

	private void openNextFile() throws KettleException {
		// Close the last file before opening the next...
		/*
		 * if (data.xbi!=null) {
		 * logBasic(Messages.getString("XBaseOutput.Log.FinishedReadingRecords"
		 * )); //$NON-NLS-1$ data.xbi.close(); }
		 */

		// Replace possible environment variables...
		// data.file_dbf = data.files.getFile(data.fileNr);
		// data.fileNr++;
		try 
		{
			// data.gtreader = new
			// be.ibridge.kettle.core.GeotoolsReader(data.file_gis.getURL());
			data.gmlwriter = new GMLWriter(data.file_gml.getURL());
			data.gmlwriter.open();

			logBasic(Messages.getString("GMLFileOutput.Log.OpenedGMLFile") + " : [" + data.gmlwriter + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$	     		        	
			/*
			 * // Add this to the result file names... ResultFile resultFile =
			 * new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file_dbf,
			 * getTransMeta().getName(), getStepname());
			 * resultFile.setComment(Messages
			 * .getString("XBaseOutput.ResultFile.Comment"));
			 * addResultFile(resultFile);
			 */
		} 
		catch (Exception e) 
		{
			logError(Messages.getString("GMLFileOutput.Log.Error.CouldNotOpenGMLFile1") + data.file_gml + Messages.getString("XBaseOutput.Log.Error.CouldNotOpenXBaseFile2") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			throw new KettleException(e);
		}
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		closeLastFile();
		super.dispose(smi, sdi);
	}

	private void closeLastFile() 
	{
		logBasic(Messages.getString("GMLFileOutput.Log.FinishedReadingRecords")); //$NON-NLS-1$
		// data.xbi.close();
		data.gmlwriter.close();
	}

	// Run is were the action happens!

	public void run() 
	{
		try 
		{
			logBasic(Messages.getString("GMLFileOutput.Log.StartingToRun")); //$NON-NLS-1$
			openNextFile();
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
