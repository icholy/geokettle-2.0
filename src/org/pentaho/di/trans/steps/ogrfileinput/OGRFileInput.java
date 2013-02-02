package org.pentaho.di.trans.steps.ogrfileinput;

// import java.io.IOException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.OGRReader;
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

/**
 * Reads data from a data source format supported by OGR.
 * 
 * @author tbadard, jmathieu
 * @since 10-jun-2010
 */
public class OGRFileInput extends BaseStep implements StepInterface{
	private OGRFileInputMeta meta;
	private OGRFileInputData data;

	private TransMeta transmeta;
	
	public OGRFileInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans){
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		transmeta = transMeta;
	}
	
	public boolean process(Object[] r) throws KettleException{
		createReader();
		incrementLinesInput();	
		Object[] ogrRow = data.ogrReader.getRow(RowDataUtil.allocateRowData(data.outputRowMeta.size()));
		while (ogrRow!=null){
			int outputIndex = data.fields.size();

			Object[] row;

			if(meta.isPassingThruFields() && (meta.isSourceInField() || meta.isLayerNameInField())){
				Object[] gtRowTemp = new Object[data.readerRowSize];
				for(int i = 0; i< data.readerRowSize;i++){
					gtRowTemp[i] = ogrRow[i];
				}
				row = (Object[]) ArrayUtils.addAll(gtRowTemp, r);					
			}else
				row = ogrRow;
			
			// Possibly add a row number...
			if (meta.isRowNrAdded())
				row[outputIndex++] = new Long(getLinesInput());
			
			putRow(data.outputRowMeta, row);
			data.rowRead++;
			
			if (checkFeedback(getLinesInput())) 
				logBasic(Messages.getString("OGRFileInput.Log.LineNr")+data.rowRead);

			if (meta.getRowLimit()>0 && data.rowRead>=meta.getRowLimit()){
				closeReader();
				setOutputDone();
				return false;
			}
		
			ogrRow = data.ogrReader.getRow(RowDataUtil.allocateRowData(data.outputRowMeta.size()));
		}
		closeReader();
		return true;
	}
	
	private boolean addSource(String source) throws KettleException{
		if(meta.isFileSource()){
            try{ 
                FileObject fileObject = KettleVFS.getFileObject(source);
                data.files.addFile(fileObject);
                if (data.files.nrOfFiles()==0){
                    logError(Messages.getString("OGRFileInput.Log.Error.NoFilesSpecified")); 
                    return false;
                }
                handleMissingFiles();
            }catch(IOException e){
                logError(Messages.getString("OGRFileInput.Log.Error.UnableToCreateFileObject", source));
                return false;
            }
        }else                    	
        	data.cnxStrings.add(source);
		return true;
	}
	
	private boolean addInputRowInfos(Object[] r) throws KettleException{
		RowMetaInterface prevInfoFields = data.prevStepFields;
		if (meta.isLayerNameInField()){    	            	               
			int idx_layername = prevInfoFields.indexOfValue(meta.getLayerNameField());
            if (idx_layername<0){
                logError(Messages.getString("OGRFileInput.Log.Error.UnableToFindFilenameField", meta.getLayerNameField()));
                stopAll();
                return false;
            }                   
            data.layernames.add(prevInfoFields.getString(r, idx_layername));  
        }else
        	data.layernames.add(meta.getLayerName());  

		String source;
        if (meta.isSourceInField()){             
         	int idx_source = prevInfoFields.indexOfValue(meta.getSourceField());          
            if (idx_source<0){
                 logError(Messages.getString("OGRFileInput.Log.Error.UnableToFindFilenameField", meta.getSourceField()));
     			 stopAll();
                 return false;
            }
            source =prevInfoFields.getString(r, idx_source);           	           
        }else
        	source = meta.getSource();
        return addSource(source); 
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException{
		meta=(OGRFileInputMeta)smi;
		data=(OGRFileInputData)sdi;		

		List<Object[]> inputRows = null;
		
		if (first){
            first = false;
                      
            data.outputRowMeta = new RowMeta();
            RowMetaInterface[] infoStep = null;                       
 
            data.index = 0;
            
            if(meta.isFileSource())
            	data.files.getFiles().clear();          
            
            if (meta.isSourceInField() || meta.isLayerNameInField()) {
                data.layernames = new ArrayList<String>(); 
                data.cnxStrings = new ArrayList<String>();             
            	inputRows =  new ArrayList<Object[]>();
        		Object[] r = getRow();
        		data.prevStepFields = getInputRowMeta().clone();
        		boolean gotFields = false;   
        		while(r != null){	
        			if(!gotFields && meta.isPassingThruFields()){
        				gotFields = true;
                        infoStep = new RowMetaInterface[] {getInputRowMeta().clone()};
        			}
        			inputRows.add(r);
        			addInputRowInfos(r);
        			r = getRow();
        		}
            }else{
            	data.layernames = new ArrayList<String>(1); 
                data.cnxStrings = new ArrayList<String>(1);
            	if(!meta.isLayerNameInField())
            		data.layernames.add(meta.getLayerName());  
            	if(!meta.isSourceInField())           
            		addSource(meta.getSource());	            
            }             
 
            data.outputRowMeta = meta.getOutputFields(data.files, data.cnxStrings, data.layernames, getStepname());
            data.readerRowSize = data.outputRowMeta.size();
            for(int i = data.outputRowMeta.size()-1; i>=0;i--){
            	if(data.outputRowMeta.getFieldNames()[i]!=null)
            		break;
            	data.readerRowSize--;       	
            }             
            meta.getFields(data.outputRowMeta, getStepname(), infoStep, null, this); // get the metadata populated.  Simple and easy.                       
		}
				
		try {
			if(meta.isSourceInField() || meta.isLayerNameInField()){
				if(inputRows == null){
					setOutputDone();
					return false;
				}					
				for(Object[] r : inputRows){			
					if(meta.isFileSource()){
						//if(!isFileAlreadyRead(data.files.getFile(data.index))){
							boolean ok = process(r);
							data.files.getFile(data.index).close();
							if(!ok)
								return false;
						//}else
							//logBasic(Messages.getString("OGRFileInput.Log.OGRFileAlreadyRead1")+" : ["+data.ogrReader+"]"+Messages.getString("OGRFileInput.Log.OGRFileAlreadyRead2"));												
					}else{
						if (data.index >= data.cnxStrings.size()){ // no more input to be expected...
							setOutputDone();
							return false;
						}
						/*if(isConnectionAlreadyRead(data.cnxStrings.get(data.index)))
							logBasic(Messages.getString("OGRFileInput.Log.OGRFileAlreadyRead1")+" : ["+data.ogrReader+"]"+Messages.getString("OGRFileInput.Log.OGRFileAlreadyRead2"));							
						else */if(!process(r))
							return false;						
					}	        	
					data.index++;	
				}  
			}else{
        		process(null);
				setOutputDone();
				return false;
        	}				
		}catch (Exception e) {
			logError("Error reading file from field value", e);
			closeReader();
			setErrors(1);
			stopAll();
			setOutputDone(); // signal end to receiver(s)
			return false;
		}
		
		return true;
	}

	public boolean isFileAlreadyRead(FileObject fo){
		List<FileObject> fos = data.files.getFiles();
		if (!fos.isEmpty()){	
			for (int i=0;i<data.index;i++){
				if (fo.getName().equals(fos.get(i).toString()))
					return true;
			}	
		}
		return false;
	}
	
	public boolean isConnectionAlreadyRead(String cnxString){		
		if (!data.cnxStrings.isEmpty()){	
			for (int i=0;i<data.index;i++){
				//if (cnxString.equalsIgnoreCase(data.cnxStrings.get(i)))
				if (cnxString.equals(data.cnxStrings.get(i)))
					return true;
			}	
		}
		return false;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi){
		meta=(OGRFileInputMeta)smi;
		data=(OGRFileInputData)sdi;

		if (super.init(smi, sdi)){
			try{
				if(meta.isFileSource()){
					data.files = meta.getFileList(this);
					if ((transmeta.getPreviousResult()==null || transmeta.getPreviousResult().getResultFiles()==null || transmeta.getPreviousResult().getResultFiles().size()==0) && 
		                  data.files.nrOfMissingFiles() > 0 && !meta.isSourceInField()){
						logError(Messages.getString("OGRFileInput.Log.Error.NoFilesSpecified"));
						return false;
					}
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
	
	private void createReader() throws KettleException{   
		String ogr_path = null;
		try{		
			if(meta.isFileSource()){
				ogr_path = data.files.getFile(data.index).getURL().getPath();
				if (Const.isWindows())
					ogr_path = ogr_path.substring(3).replace('/', '\\'); 
				else 
					ogr_path = ogr_path.substring(2);
			}else if (data.cnxStrings.get(data.index)!=null && !(data.cnxStrings.get(data.index).trim().equals("")))
					ogr_path = data.cnxStrings.get(data.index);											

			data.ogrReader = new OGRReader(ogr_path, data.layernames.get(data.index), meta.getSpatialFilter(), meta.getAttributeFilter(), meta.isSkipFailureAdded());
			data.ogrReader.open();
			logBasic(Messages.getString("OGRFileInput.Log.OpenedSource")+" : " + ogr_path); 
			compareSourceFields();			
			data.fields = data.ogrReader.getFields();
		}catch(Exception e){
			logError(Messages.getString("OGRFileInput.Log.Error.CouldNotOpenSource1") + " : " + ogr_path + Messages.getString("OGRFileInput.Log.Error.CouldNotOpenSource2")+e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			throw new KettleException(e);
		}
	}

	private void compareSourceFields() throws KettleException{
		if(data.fields != null){
			String[] oldFields = data.fields.getFieldNamesAndTypes(0);
			String[] newFields = data.ogrReader.getFields().getFieldNamesAndTypes(0);
			int length = oldFields.length;
			if(length != newFields.length)
				throw new KettleException(Messages.getString("OGRFileInput.Log.Error.DifferentStructure"));	
			for(int i = 0 ; i < length ; i++){
				if(!oldFields[i].equalsIgnoreCase(newFields[i]))
					throw new KettleException(Messages.getString("OGRFileInput.Log.Error.DifferentStructure"));
			}			
		}		
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi){
		closeReader();     
		super.dispose(smi, sdi);
	}

	private void closeReader() {
		if(data.ogrReader!=null)
			data.ogrReader.close();
	}

	//
	// Run is were the action happens!
	//
	public void run(){
		try{
			logBasic(Messages.getString("OGRFileInput.Log.StartingToRun"));		 //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data) );
		}catch(Exception e){
			logError(Messages.getString("OGRFileInput.Log.Error.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
