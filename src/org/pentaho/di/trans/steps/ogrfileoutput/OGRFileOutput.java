package org.pentaho.di.trans.steps.ogrfileoutput;

import java.io.IOException;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.geospatial.OGRWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.ogrfileinput.Messages;

/**
 * Write data to an OGR data destination.
 * 
 * @author tbadard
 * @since 11-jun-2010
 */
public class OGRFileOutput extends BaseStep implements StepInterface {
	private OGRFileOutputMeta meta;
	private OGRFileOutputData data;

	public OGRFileOutput(StepMeta stepMeta,StepDataInterface stepDataInterface, int copyNr,TransMeta transMeta, Trans trans) 
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)throws KettleException 
	{
		meta = (OGRFileOutputMeta) smi;
		data = (OGRFileOutputData) sdi;

		Object[] r = getRow(); // this also waits for a previous step to be
		// finished.
		if (r == null) // no more input to be expected...
		{
			try 
			{
				//data.ogrWriter.write();
				return false;
			} 
			catch (Exception e) 
			{
				logError("Because of an error, this step can't continue: ", e);
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}
			finally {
				data.ogrWriter.close();
			}
		}

		if (first) 
		{
			first = false;
			data.outputRowMeta = getInputRowMeta().clone();

			try 
			{
				data.ogrWriter.createLayer(data.outputRowMeta);
			} 
			catch (Exception e) 
			{
				logError("Because of an error, this step can't continue: ", e);
				data.ogrWriter.close();
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}
		}

		try 
		{
			data.ogrWriter.putRow(r,data.outputRowMeta);
			incrementLinesOutput();
		} 
		catch (Exception e) 
		{
			logError("Because of an error, this step can't continue: ", e);
			data.ogrWriter.close();
			setErrors(1);
			stopAll();
			setOutputDone(); // signal end to receiver(s)
			return false;
		}
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (OGRFileOutputMeta) smi;
		data = (OGRFileOutputData) sdi;

		if (super.init(smi, sdi)) {

			try 
			{
				data.file_gis = KettleVFS.getFileObject(this.environmentSubstitute(meta.getGisFileName())); 
				data.file_format = meta.getOgrOutputFormat();
				
				// Create file if it does not exist
//				if (!data.file_gis.exists()) {
//					data.file_gis.createFile();
//				}

			} 			
			catch (IOException e) 
			{
				logError("IOException occured in OGRFileOutput.init():", e);
				return false;
			}

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
			data.ogrWriter = new OGRWriter(data.file_gis.getURL().getPath(),data.file_format);
			data.ogrWriter.open();

			logBasic(Messages.getString("OGRFileOutput.Log.OpenedGISFile") + " : [" + data.ogrWriter + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$	     		        	
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
			logError(Messages.getString("OGRFileOutput.Log.Error.CouldNotOpenGISFile1") + data.file_gis + Messages.getString("XBaseOutput.Log.Error.CouldNotOpenXBaseFile2") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
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
		logBasic(Messages.getString("OGRFileOutput.Log.FinishedReadingRecords")); //$NON-NLS-1$
		data.ogrWriter.close();
	}

	// Run is were the action happens!
	public void run() 
	{
		try 
		{
			logBasic(Messages.getString("OGRFileOutput.Log.StartingToRun")); //$NON-NLS-1$
			openNextFile();
			while (!isStopped() && processRow(meta, data));
		} 
		catch (Exception e) 
		{
			logError(Messages.getString("OGRFileOutput.Log.Error.UnexpectedError") + " : " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
