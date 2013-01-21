package org.pentaho.di.trans.steps.ogrfileoutput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.geospatial.OGRWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.ogrfileoutput.Messages;

/**
 * Write data to an OGR data source.
 * 
 * @author tbadard, jmathieu
 * @since 11-jun-2010
 */
public class OGRFileOutput extends BaseStep implements StepInterface {
	//private final String DEFAULT_ENCODING = "UTF-8";
	
	private OGRFileOutputMeta meta;
	private OGRFileOutputData data;
	
	private TransMeta transmeta;
	
	public OGRFileOutput(StepMeta stepMeta,StepDataInterface stepDataInterface, int copyNr,TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		transmeta = transMeta;
	}

	/*private void setEncoding(){
		String encoding = meta.getEncoding();
		if(!Const.isEmpty(encoding)){
			RowMetaInterface rmi = data.outputRowMeta;
			for(int i = 0; i < rmi.size(); i++){
				ValueMetaInterface value = rmi.getValueMeta(i);
				switch(value.getType()) {
					case ValueMeta.TYPE_NUMBER:
					case ValueMeta.TYPE_STRING:
						value.setStringEncoding(encoding);
						break;
					case ValueMeta.TYPE_DATE:
					case ValueMeta.TYPE_BOOLEAN:
					case ValueMeta.TYPE_INTEGER:
					case ValueMeta.TYPE_BIGNUMBER:
					case ValueMeta.TYPE_GEOMETRY:			
					case ValueMeta.TYPE_NONE:
					case ValueMeta.TYPE_SERIALIZABLE:
					case ValueMeta.TYPE_BINARY:
					default:
						value.setStringEncoding(encoding);
						break;
				}				
			}
		}
	}*/
	
	private boolean addSource(String source) throws KettleException{
		if(meta.isFileSource()){
            try{ 
                FileObject fileObject = KettleVFS.getFileObject(source);
                data.files.addFile(fileObject);
                if (data.files.nrOfFiles()==0){
                    logError(Messages.getString("OGRFileOutput.Log.Error.NoFilesSpecified")); 
                    return false;
                }
            }catch(IOException e){
                logError(Messages.getString("OGRFileOutput.Log.Error.UnableToCreateFileObject", source));
                return false;
            }
        }else                    	
        	data.cnxStrings.add(source);
		return true;
	}
	
	private boolean compareStrings(String s1, String s2){
		boolean same;
		try{
			same = s1.equalsIgnoreCase(s2);
		}catch(NullPointerException e){
			same = s1 == s2;
		}
		return same;		
	}
	
	private boolean addInputRowInfos(Object[] r) throws KettleException{
		boolean noErrors = true;
		RowMetaInterface prevInfoFields = data.outputRowMeta;
		if (meta.isLayerNameInField()){    	            	               
			int idx_layername = prevInfoFields.indexOfValue(meta.getLayerNameField());
		    if (idx_layername<0){
		        logError(Messages.getString("OGRFileOutput.Log.Error.UnableToFindFilenameField", meta.getLayerNameField()));
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
		         logError(Messages.getString("OGRFileOutputput.Log.Error.UnableToFindFilenameField", meta.getSourceField()));
				 stopAll();
		         return false;
		    }
		    source =prevInfoFields.getString(r, idx_source);           	           
		}else
			source = meta.getSource();
		
		noErrors = addSource(source);
		
		boolean newWriterFlag = false;
		
		if(data.index > 0){
			if(meta.isSourceInField()){
				if(meta.isFileSource()){
					if(!compareStrings(data.files.getFile(data.index).getName().toString(), data.files.getFile(data.index - 1).toString()))					
						newWriterFlag = true;					
				}else{
					if(!compareStrings(data.cnxStrings.get(data.index), data.cnxStrings.get(data.index - 1)))
						newWriterFlag = true;
				}					
			}else{
				if(!compareStrings(data.layernames.get(data.index), data.layernames.get(data.index - 1)))
					newWriterFlag = true;				
			}
									
		}else
			newWriterFlag = true;
		
		data.newWriterFlags.add(newWriterFlag);
		data.index++;
		return noErrors; 
	}
	
	private void checkForNewDataOutput() throws KettleException{
		if(data.newWriterFlags.get(data.index)){
			closeWriter();
			createWriter();
		}
	}
	
	private void process(Object[] r) throws KettleException{
		checkForNewDataOutput();
		data.ogrWriter.putRow(r,data.outputRowMeta);
		putRow(data.outputRowMeta, r); 
		incrementLinesOutput();
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)throws KettleException {
		meta = (OGRFileOutputMeta) smi;
		data = (OGRFileOutputData) sdi;

		List<Object[]> inputRows = null;
		
		Object[] r = getRow(); 
		if(r == null){
			setOutputDone(); // signal end to receiver(s)
			return false;
		}
		
		if (first){
            first = false;

            if(meta.isFileSource())
            	data.files.getFiles().clear();   
            
            data.outputRowMeta = getInputRowMeta().clone();               
        	data.index = 0;
        	inputRows =  new ArrayList<Object[]>();
    		        
    		while(r != null){	
    			inputRows.add(r);
    			addInputRowInfos(r);
    			r = getRow();
    		}   		                                       
		}
		
		try {		
			data.index = 0;
			for(Object[] row : inputRows){					
				process(row);										
				data.index++;						
			}  																				      				
		}catch (Exception e) {
			logError("Error writing into source", e);
			closeWriter();
			setErrors(1);
			stopAll();
			setOutputDone(); // signal end to receiver(s)
			return false;
		}
		
		setOutputDone();
		return false;	
	}
	
	private void closeWriter(){
		if(data.ogrWriter != null)
			data.ogrWriter.close();
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (OGRFileOutputMeta) smi;
		data = (OGRFileOutputData) sdi;

		if (super.init(smi, sdi)) {
			if(meta.isFileSource()){
				data.files = meta.getFileList(this);
				if ((transmeta.getPreviousResult()==null || transmeta.getPreviousResult().getResultFiles()==null || transmeta.getPreviousResult().getResultFiles().size()==0) && 
	                  data.files.nrOfMissingFiles() > 0 && !meta.isSourceInField()){
					logError(Messages.getString("OGRFileInput.Log.Error.NoFilesSpecified"));
					return false;
				}
			}					
			data.file_format = meta.getOgrOutputFormat();
			data.file_options = meta.getOgrOptions();
			data.file_geomtype = meta.getOgrGeomType();
			data.write_mode = meta.getOgrWriteMode();
			data.fid_field = meta.getOgrFIDField();
			data.preserve_fid_field = meta.isPreserveFIDField();
            data.layernames = new ArrayList<String>(); 
            data.cnxStrings = new ArrayList<String>(); 
            data.newWriterFlags = new ArrayList<Boolean>(); 
			//data.encoding = Const.isEmpty(meta.getEncoding()) ? DEFAULT_ENCODING : meta.getEncoding();
			return true;
		}
		return false;
	}

	private void createWriter() throws KettleException {
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
			//data.ogrWriter = new OGRWriter(ogr_path, meta.isFileSource(), data.file_format, data.file_options, data.file_geomtype, data.layerName, data.write_mode, data.fid_field, data.preserve_fid_field, data.encoding);
			data.ogrWriter = new OGRWriter(ogr_path, meta.isFileSource(), data.file_format, data.file_options, data.file_geomtype, data.layernames.get(data.index), data.write_mode, data.fid_field, data.preserve_fid_field);
			data.ogrWriter.open(); 
			data.ogrWriter.createLayer(data.outputRowMeta);
			logBasic(Messages.getString("OGRFileOutput.Log.OpenedGISFile") + " : [" + data.ogrWriter + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$	     		        	
		}catch (Exception e){
			logError(Messages.getString("OGRFileOutput.Log.Error.CouldCreateDataSource") + "\"" + ogr_path  + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			throw new KettleException(e);
		}
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi){
		logBasic(Messages.getString("OGRFileOutput.Log.FinishedReadingRecords"));
		closeWriter();
		super.dispose(smi, sdi);
	}

	// Run is were the action happens!
	public void run(){
		try {
			logBasic(Messages.getString("OGRFileOutput.Log.StartingToRun")); //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data));
		} catch (Exception e) {
			logError(Messages.getString("OGRFileOutput.Log.Error.UnexpectedError") + " : " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
