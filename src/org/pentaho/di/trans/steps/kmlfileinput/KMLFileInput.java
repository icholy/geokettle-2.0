package org.pentaho.di.trans.steps.kmlfileinput;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.KMLReader;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
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
 * Reads data from a KML file.
 * 
 * @author tpelletierdoisy, eleprince, tbadard, jmathieu
 * @since 14-sep-2009
 */

public class KMLFileInput extends BaseStep implements StepInterface{
	private KMLFileInputMeta meta;
	private KMLFileInputData data;
	
	private TransMeta transmeta;
	private KMLReader kmlReader;
	
	public KMLFileInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		this.transmeta = transMeta;	
	}
	
	public boolean isFileAlreadyRead(FileObject fo){
		List<FileObject> fos = data.files.getFiles();
		if (!fos.isEmpty()){	
			for (int i=0;i<data.fileIndex;i++){
				if (fo.getName().equals(fos.get(i).toString()))
					return true;
			}	
		}
		return false;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException{
		meta=(KMLFileInputMeta)smi;
		data=(KMLFileInputData)sdi;
	
		if (first){
            first = false;        
            Object[] r;
            
            data.outputRowMeta = new RowMeta();
            RowMetaInterface[] infoStep = null;
            
            if (meta.isFileNameInField()){
                // Read the files from the specified input stream...
                data.files.getFiles().clear();
                
                int idx = -1;
                data.rowSet = findInputRowSet(meta.getAcceptingStepName());
                
                r = getRowFrom(data.rowSet);
                while (r!=null){
                    RowMetaInterface prevInfoFields = data.rowSet.getRowMeta();
                    if (idx<0){
                        if (meta.isPassingThruFields()){
                            data.passThruFields = new HashMap<FileObject, Object[]>();
                            infoStep = new RowMetaInterface[] { prevInfoFields };
                            data.nrPassThruFields = prevInfoFields.size();
                        }
                        idx = prevInfoFields.indexOfValue(meta.getFileNameField());
                        if (idx<0){
                            logError(Messages.getString("KMLFileInput.Log.Error.UnableToFindFilenameField", meta.getFileNameField()));
                            setErrors(1);
                            stopAll();
                            return false;
                        }
                    }
                    String fileValue = prevInfoFields.getString(r, idx);
                    try{
                        FileObject fileObject = KettleVFS.getFileObject(fileValue);
                        data.files.addFile(fileObject);
                        if (meta.isPassingThruFields())
                            data.passThruFields.put(fileObject, r);
                    }catch(IOException e){
                        logError(Messages.getString("KMLFileInput.Log.Error.UnableToCreateFileObject", fileValue));
                    }
                    
                    // Grab another row
                    r = getRowFrom(data.rowSet);
                }
                
                if (data.files.nrOfFiles()==0){
                    if (log.isDetailed()) logDetailed(Messages.getString("KMLFileInput.Log.Error.NoFilesSpecified"));
                    setOutputDone();
                    return false;
                }
                data.outputRowMeta = meta.getOutputFields(data.files, getStepname());
                data.readerRowSize = data.outputRowMeta.size();
                for(int i = data.outputRowMeta.size()-1; i>=0;i--){
                	if(data.outputRowMeta.getFieldNames()[i]==null)
                		 data.readerRowSize--;
                	else
                		break;         	
                }           
            }

            meta.getFields(data.outputRowMeta, getStepname(), infoStep, null, this); // get the metadata populated.  Simple and easy.
            
            handleMissingFiles();
		}
		
		try {			
			if (data.fileIndex >= data.files.getFiles().size()){ // no more input to be expected...
				setOutputDone();
				return false;
			}

			FileObject fo = data.files.getFile(data.fileIndex);
			if(!isFileAlreadyRead(fo)){			
				createReader(data.fileIndex);
				incrementLinesInput();						

				Object[] gtRow = kmlReader.getRow(RowDataUtil.allocateRowData(data.outputRowMeta.size()));
				while (gtRow!=null){
					int outputIndex = data.fields.size();

					Object[] row;

					if(meta.isPassingThruFields() && meta.isFileNameInField()){
						Object[] gtRowTemp = new Object[data.readerRowSize];
						for(int i = 0; i< data.readerRowSize;i++){
							gtRowTemp[i] = gtRow[i];
						}
						row = (Object[]) ArrayUtils.addAll(gtRowTemp, data.passThruFields.get(data.files.getFile(data.fileIndex)));					
					}else
						row = gtRow;
					
					// Possibly add a row number...
					if (meta.isRowNrAdded())
						row[outputIndex++] = new Long(getLinesInput());
					
					putRow(data.outputRowMeta, row);
					data.rowRead++;
					
					if (checkFeedback(getLinesInput())) 
						logBasic(Messages.getString("KMLFileInput.Log.LineNr")+data.rowRead);

					if (meta.getRowLimit()>0 && data.rowRead>=meta.getRowLimit()){
						closeReader();
						setOutputDone();
						return false;
					}
				
					gtRow = kmlReader.getRow(RowDataUtil.allocateRowData(data.outputRowMeta.size()));
				}
			}else
				logBasic(Messages.getString("KMLFileInput.Log.KMLFileAlreadyRead1")+" : ["+kmlReader+"]"+Messages.getString("KMLFileInput.Log.KMLFileAlreadyRead2"));
			
			data.files.getFile(data.fileIndex).close();
			closeReader();
			
			if (!meta.isFileNameInField()){ // no more input to be expected...
				setOutputDone();
				return false;
			}
			
			data.fileIndex++;
		}catch (Exception e) {
			logError("Error reading kml file from field value", e);
			closeReader();
			setErrors(1);
			stopAll();
			setOutputDone(); // signal end to receiver(s)
			return false;
		}      			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi){
		meta=(KMLFileInputMeta)smi;
		data=(KMLFileInputData)sdi;

		if (super.init(smi, sdi)){
			try{
				data.files = meta.getFileList(this);
				if ((transmeta.getPreviousResult()==null || transmeta.getPreviousResult().getResultFiles()==null || transmeta.getPreviousResult().getResultFiles().size()==0) && 
	                  data.files.nrOfMissingFiles() > 0 && !meta.isFileNameInField()){
					logError(Messages.getString("KMLFileInput.Log.Error.NoFilesSpecified"));
					return false;
				}	
			}catch (Exception e){
				logError("Cannot open/create file ", e);
				return false;
			} 
			return true;
		}
		return false;
	}
	
	private void handleMissingFiles() throws KettleException{
		List<FileObject> nonExistantFiles = data.files.getNonExistantFiles();
		if (nonExistantFiles.size() != 0){
			String message = FileInputList.getRequiredFilesDescription(nonExistantFiles);
			if(log.isBasic()) 
				log.logBasic("Required files", "WARNING: Missing " + message);
			throw new KettleException("Following required files are missing: " + message);		
		}

		List<FileObject> nonAccessibleFiles = data.files.getNonAccessibleFiles();
		if (nonAccessibleFiles.size() != 0){
			String message = FileInputList.getRequiredFilesDescription(nonAccessibleFiles);
			if(log.isBasic()) 
				log.logBasic("Required files", "WARNING: Not accessible " + message);		
			throw new KettleException("Following required files are not accessible: " + message);			
		}
	}
	
	private void createReader(int fileIndex) throws KettleException{                
		try{
			kmlReader = new KMLReader(data.files.getFile(fileIndex).getURL());
			kmlReader.open();
			logBasic(Messages.getString("KMLFileInput.Log.OpenedKMLFile")+" : " + data.files.getFile(fileIndex).getName().toString()); 
			data.fields = kmlReader.getFields();
		}catch(Exception e){
			logError(Messages.getString("KMLFileInput.Log.Error.CouldNotOpenKMLFile1") + " : " + data.files.getFile(fileIndex).getName().toString() + Messages.getString("KMLFileInput.Log.Error.CouldNotOpenKMLFile2")+e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			throw new KettleException(e);
		}
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi){
		closeReader();     
		super.dispose(smi, sdi);
	}

	private void closeReader() {
		if(kmlReader!=null)
			kmlReader.close();
	}

	public void run(){
		try{
			logBasic(Messages.getString("KMLFileInput.Log.StartingToRun"));	
			while (!isStopped() && processRow(meta, data) );
		}catch(Exception e){
			logError(Messages.getString("KMLFileInput.Log.Error.UnexpectedError")+" : "+e.toString());
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