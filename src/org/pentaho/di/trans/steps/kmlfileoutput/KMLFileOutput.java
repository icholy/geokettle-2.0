package org.pentaho.di.trans.steps.kmlfileoutput;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.geospatial.KMLWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.kmlfileinput.Messages;

/**
 * Writes data to a KML file.
 * 
 * @author tpelletierdoisy, eleprince, tbadard
 * @since 29-oct-2009
 */
public class KMLFileOutput extends BaseStep implements StepInterface {
	private KMLFileOutputMeta meta;
	private KMLFileOutputData data;

	public KMLFileOutput(StepMeta stepMeta,StepDataInterface stepDataInterface, int copyNr,TransMeta transMeta, Trans trans) 
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)throws KettleException 
	{
		meta = (KMLFileOutputMeta) smi;
		data = (KMLFileOutputData) sdi;

		Object[] r = getRow(); // this also waits for a previous step to be
		// finished.
		if (r == null) // no more input to be expected...
		{
			try 
			{
				data.gtwriter.write();
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
				data.gtwriter.close();
			}
		}

		if (first) 
		{
			first = false;
			data.outputRowMeta = getInputRowMeta().clone();

			try 
			{//ajoute par tpelletierdoisy afin de verifier qu'il y a bien un chmap de type geometrie
				//et de reperer la position des champs interessants
				data.gtwriter.visionTable(data.outputRowMeta);
			} 
			catch (Exception e) 
			{
				logError("Because of an error, this step can't continue: ", e);
				data.gtwriter.close();
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}
		}

		try 
		{//ajoute par tpelletierdoisy afin de creer un nouveau placemark pour chaque entree presente au debut du step
			data.gtwriter.createNewPlacemark(r);
			incrementLinesOutput();
		} 
		catch (Exception e) 
		{
			logError("Because of an error, this step can't continue: ", e);
			data.gtwriter.close();
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

			try 
			{
				data.file_kml = KettleVFS.getFileObject(this.environmentSubstitute(meta.getKmlFileName())); 

				// Create file if it does not exist
				if (!data.file_kml.exists()) {
					data.file_kml.createFile();
				}

			} 			
			catch (IOException e) 
			{
				logError("IOException occured in KMLFileOutput.init():", e);
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
			data.gtwriter = new KMLWriter(data.file_kml.getURL());
			data.gtwriter.open();

			logBasic(Messages.getString("KMLFileOutput.Log.OpenedKMLFile") + " : [" + data.gtwriter + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$	     		        	
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
			logError(Messages.getString("KMLFileOutput.Log.Error.CouldNotOpenKMLFile1") + data.file_kml + Messages.getString("XBaseOutput.Log.Error.CouldNotOpenXBaseFile2") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
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
		logBasic(Messages.getString("KMLFileOutput.Log.FinishedReadingRecords")); //$NON-NLS-1$
		data.gtwriter.close();
	}

	// Run is were the action happens!
	public void run() 
	{
		try 
		{
			logBasic(Messages.getString("KMLFileOutput.Log.StartingToRun")); //$NON-NLS-1$
			openNextFile();
			while (!isStopped() && processRow(meta, data));
		} 
		catch (Exception e) 
		{
			logError(Messages.getString("KMLFileOutput.Log.Error.UnexpectedError") + " : " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			logError(Const.getStackTracker(e));
			setErrors(1);
			stopAll();
		} 
		finally 
		{ try {
			//ajouter par tpelletierdoisy afin de serialiser le Kml apres remplissage
			data.gtwriter.kml.marshal(data.gtwriter.file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			dispose(meta, data);
			markStop();
			logSummary();
		}
	}
}

