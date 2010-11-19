package org.pentaho.di.trans.steps.ogrfileinput;

// import java.io.IOException;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.geospatial.OGRReader;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Reads data from a data source format supported by OGR.
 * 
 * @author tbadard
 * @since 10-jun-2010
 */
public class OGRFileInput extends BaseStep implements StepInterface
{
	private OGRFileInputMeta meta;
	private OGRFileInputData data;
		
	public OGRFileInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(OGRFileInputMeta)smi;
		data=(OGRFileInputData)sdi;
        
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
        Object[] row = data.ogrReader.getRow( RowDataUtil.allocateRowData(data.outputRowMeta.size()) );
        
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

        if (checkFeedback(getLinesInput())) logBasic(Messages.getString("OGRFileInput.Log.LineNr")+getLinesInput()); //$NON-NLS-1$

        if (meta.getRowLimit()>0 && getLinesInput()>=meta.getRowLimit())  // limit has been reached: stop now.
        {
            setOutputDone();
            return false;
        }

		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(OGRFileInputMeta)smi;
		data=(OGRFileInputData)sdi;

	    if (super.init(smi, sdi))
	    {
	    	
            data.files  = meta.getTextFileList(this);
            data.fileNr = 0;
            
            if (data.files.nrOfFiles()==0 /* && !meta.isAcceptingFilenames() */)
            {
                logError(Messages.getString("OGRFileInput.Log.Error.NoFilesSpecified"));
                return false;
            }
            
            data.file_gis = data.files.getFile(0);
            
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
        	// TODO Handle more appropriately the OGR data source type (file vs. connection string) !
        	data.ogrReader = new OGRReader(data.file_gis.getURL().getPath());
        	data.ogrReader.open();

        	logBasic(Messages.getString("OGRFileInput.Log.OpenedGISFile")+" : ["+data.ogrReader+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        	data.fields = data.ogrReader.getFields();

        	/*
            // Add this to the result file names...
            ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file_dbf, getTransMeta().getName(), getStepname());
            resultFile.setComment(Messages.getString("XBaseInput.ResultFile.Comment"));
            addResultFile(resultFile);
            */
        }
        catch(Exception e)
        {
            logError(Messages.getString("OGRFileInput.Log.Error.CouldNotOpenGISFile1")+data.file_gis+Messages.getString("XBaseInput.Log.Error.CouldNotOpenXBaseFile2")+e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
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
        logBasic(Messages.getString("OGRFileInput.Log.FinishedReadingRecords")); //$NON-NLS-1$
        if (data.ogrReader != null) {
        	data.ogrReader.close();
        }
    }

    //
	// Run is were the action happens!
	//
	public void run()
	{
		try
		{
			logBasic(Messages.getString("OGRFileInput.Log.StartingToRun"));		 //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data) );
		}
		catch(Exception e)
		{
			logError(Messages.getString("OGRFileInput.Log.Error.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
