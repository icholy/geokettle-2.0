package org.pentaho.di.trans.steps.kmlfileinput;

//import java.io.IOException;

import org.apache.commons.vfs.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.geospatial.KMLReader;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.kmlfileinput.KMLFileInputData;
import org.pentaho.di.trans.steps.kmlfileinput.KMLFileInputMeta;
import org.pentaho.di.trans.steps.kmlfileinput.Messages;

/**
 * Reads data from a KML file.
 * 
 * @author tpelletierdoisy, eleprince, tbadard
 * @since 14-sep-2009
 */

public class KMLFileInput extends BaseStep implements StepInterface{

	private KMLFileInputMeta meta;
	private KMLFileInputData data;
	
	public KMLFileInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(KMLFileInputMeta)smi;
		data=(KMLFileInputData)sdi;
        
        // See if we need to get a list of files from input...
        if (first) // we just got started
        {
            first = false;
            
            data.outputRowMeta = meta.getOutputFields(data.files, getStepname());
            
            // Open the first file & read the required rows in the buffer, stop
            // if it fails, exception will stop processLoop
           openNextFile();
         
                
        }
        
        // Allocate the output row in advance, because we possibly want to add a few extra fields...
        Object[] row = data.gtreader.getRow( RowDataUtil.allocateRowData(data.outputRowMeta.size()) );
       
//        while (row==null && data.fileNr < data.files.nrOfFiles()) // No more rows left in this file
//        {
//            openNextFile();
//            row = data.xbi.getRow(data.fields);
//        }
        
        if (row==null) 
        {           
            setOutputDone();  // signal end to receiver(s)
            return false; // end of data or error.
        }
        
        // OK, so we have read a line: increment the input counter
		incrementLinesInput();
		int outputIndex = data.fields.size();

        // Possibly add a filename...
//        if (meta.includeFilename())
//        {
//            Value inc = new Value(meta.getFilenameField(), data.file_dbf.getName().getURI());
//            inc.setLength(100);
//            row.addValue(inc);
//        }

        // Possibly add a row number...
        if (meta.isRowNrAdded())
        {
            row[outputIndex++] = new Long(getLinesInput());
        }

        putRow(data.outputRowMeta, row);        // fill the rowset(s). (wait for empty)

        if (checkFeedback(getLinesInput())) logBasic(Messages.getString("KMLFileInput.Log.LineNr")+getLinesInput()); //$NON-NLS-1$

        if (meta.getRowLimit()>0 && getLinesInput()>=meta.getRowLimit())  // limit has been reached: stop now.
        {
            setOutputDone();
            return false;
        }

		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(KMLFileInputMeta)smi;
		data=(KMLFileInputData)sdi;

	    if (super.init(smi, sdi))
	    {
	    	
            data.files  = meta.getTextFileList(this);
            data.fileNr = 0;
            
            if (data.files.nrOfFiles()==0 /* && !meta.isAcceptingFilenames() */)
            {
                logError(Messages.getString("KMLFileInput.Log.Error.NoFilesSpecified"));
                return false;
            }
            
            data.file_kml = data.files.getFile(0);
           
//	    	try {
//	    		// java.net.URL fileURL = (new java.io.File(meta.getGisFileName())).toURI().toURL();
//	    	}
	    	//catch(java.net.MalformedURLException urle) {
//	    	catch(Exception e) {
//	    		// TODO: change for another error message
//	    		logError(Messages.getString("GISFileInput.Log.Error.MalformedURL"));
//	    	}
	    	
            return true;
	    }
		return false;
	}
	
	private void openNextFile() throws KettleException
    {
        // Close the last file before opening the next...
        /*
		if (data.xbi!=null)
        {
            logBasic(Messages.getString("XBaseInput.Log.FinishedReadingRecords")); //$NON-NLS-1$
            data.xbi.close();
        }
        */
        
        // Replace possible environment variables...
        //data.file_dbf = data.files.getFile(data.fileNr);
        //data.fileNr++;
                
        try
        {
        	data.gtreader = new KMLReader(data.file_kml.getURL());
        	data.gtreader.open();

        	logBasic(Messages.getString("KMLFileInput.Log.OpenedKMLFile")+" : ["+data.gtreader+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        	data.fields = data.gtreader.getFields();

        	/*
            // Add this to the result file names...
            ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file_dbf, getTransMeta().getName(), getStepname());
            resultFile.setComment(Messages.getString("XBaseInput.ResultFile.Comment"));
            addResultFile(resultFile);
            */
        }
        catch(Exception e)
        {
            logError(Messages.getString("KMLFileInput.Log.Error.CouldNotOpenKMLFile1")+data.file_kml+Messages.getString("XBaseInput.Log.Error.CouldNotOpenXBaseFile2")+e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
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
        logBasic(Messages.getString("KMLFileInput.Log.FinishedReadingRecords")); //$NON-NLS-1$
        if (data.gtreader != null) {
        	data.gtreader.close();
        }
    }

    //
	// Run is were the action happens!
	//
	public void run()
	{
		try
		{
			logBasic(Messages.getString("KMLFileInput.Log.StartingToRun"));		 //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data) );
		}
		catch(Exception e)
		{
			logError(Messages.getString("KMLFileInput.Log.Error.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
