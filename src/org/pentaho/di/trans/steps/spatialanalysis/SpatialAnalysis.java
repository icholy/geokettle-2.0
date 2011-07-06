package org.pentaho.di.trans.steps.spatialanalysis;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
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
import org.pentaho.di.trans.steps.sort.RowTempFile;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author jmathieu
 * @since nov-2010
 */
public class SpatialAnalysis extends BaseStep implements StepInterface{
	private SpatialAnalysisMeta meta;
	private SpatialAnalysisData data;
	
	public SpatialAnalysis(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans){
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private boolean addBuffer(RowMetaInterface rowMeta, Object[] r) throws KettleException{
		if (r != null){
			data.buffer.add(r);
			data.freeCounter++;
			
			boolean doWrite = data.buffer.size() == data.writeSize; // Buffer is full: dump to disk
			doWrite |= data.files.size() > 0 && r == null && data.buffer.size() > 0; // No more records: join from disk 
			doWrite |= data.freeMemoryPctLimit > 0 && data.freeMemoryPct < data.freeMemoryPctLimit && data.buffer.size() >= data.minWriteSize;
			
			if (doWrite){				
				DataOutputStream dos;
				GZIPOutputStream gzos;
			
				try{
					FileObject fileObject=KettleVFS.createTempFile("spatialAnalysis", ".tmp", environmentSubstitute(Messages.getString("System.Button.Browse")));
					
					data.files.add(fileObject); // Remember the files!
					
					OutputStream outputStream = KettleVFS.getOutputStream(fileObject, false);
					
					if (data.compressFiles){
						gzos = new GZIPOutputStream(new BufferedOutputStream(outputStream));
						dos = new DataOutputStream(gzos);
					}else{
						dos = new DataOutputStream(new BufferedOutputStream(outputStream, 50000));
						gzos = null;
					}                              							
	                
					// How many records do we have left?
					data.bufferSizes.add( data.buffer.size() );
	                
	                for (int p = 0; p < data.buffer.size(); p++){
	                    data.outputRowMeta.writeData(dos, data.buffer.get(p));
					}
	                
	                if (data.writeSize < 0 && data.buffer.size() > data.minWriteSize){
	            		data.minWriteSize = data.buffer.size(); // if we did it once, we can do it again.
	            		
	            		// Memory usage goes up over time, even with garbage collection
	            		// We need pointers, file handles, etc.
	            		// As such, we're going to lower the min sort size a bit
	            		data.minWriteSize = (int)Math.round((double)data.minWriteSize * 0.90);
	                }
	                              
	                // Clear the list
	                data.buffer.clear();
	                
					// Close temp-file
					dos.close();  // close data stream
					
					if (gzos != null)
						gzos.close(); // close gzip stream
	                
	                outputStream.close();  // close file stream
	                
	                // How much memory do we have left?
	                data.freeMemoryPct = Const.getPercentageFreeMemory();
	    			data.freeCounter = 0;
	    			
	    			if (data.writeSize <= 0 && log.isDetailed()) 
	    				logDetailed("Available memory : " + data.freeMemoryPct + "%");      			
				}catch(Exception e){
					throw new KettleException("Error processing temp-file!", e);
				}		
	            data.getBufferIndex=0;
			}				
			return true; 
		}
		return false;
	}
	
	private Object[] getBuffer() throws KettleValueException{
		// Open all files at once and read one row from each file...
		if (data.files.size() > 0 && ( data.dis.size() == 0 || data.fis.size() == 0 )){
			if(log.isBasic())
				logBasic("Opening " + data.files.size() + " tmp-files...");		
			try{
				for (int f=0;f<data.files.size() && !isStopped();f++){
					FileObject fileObject = (FileObject)data.files.get(f);

					if (log.isDetailed()) 
						logDetailed("Opening tmp-file: ["+fileObject.getName()+"]");
					
					InputStream fi = KettleVFS.getInputStream(fileObject);
					DataInputStream di;
					data.fis.add(fi);
					if (data.compressFiles){
						GZIPInputStream gzfi = new GZIPInputStream(new BufferedInputStream(fi));
						di = new DataInputStream(gzfi);
						data.gzis.add(gzfi);
					}else
						di = new DataInputStream(new BufferedInputStream(fi, 50000));
					
					data.dis.add(di);
					
					// How long is the buffer?
					int buffersize=data.bufferSizes.get(f);
					
					if (log.isDetailed()) 
						logDetailed("["+fileObject.getName()+"] expecting "+buffersize+" rows...");
					
					if (buffersize>0){
						Object[] row = (Object [])data.outputRowMeta.readData(di);
                        data.rowBuffer.add( row );    // new row from input stream
                        data.tempRows.add( new RowTempFile(row,f) );
					}
				}	
			}catch(Exception e){
				logError("Error reading back tmp-files : "+e.toString());
                logError(Const.getStackTracker(e));
			}
		}
		
		Object[] retval = null;
		
		if (data.files.size()==0){
			if (data.getBufferIndex<data.buffer.size()){
				retval=(Object[])data.buffer.get(data.getBufferIndex);
				data.getBufferIndex++;
			}
		}else{
			if (data.rowBuffer.size()>0){       
				if (log.isRowLevel()){
				    for (int i=0;i<data.rowBuffer.size() && !isStopped();i++){
					    logRowlevel("--BR#"+i+": "+data.outputRowMeta.getString((Object[])data.rowBuffer.get(i)));
				    }
				}				
				retval = data.tempRows.remove(0).row;												
			}
		}
		return retval;
	}
	
	public boolean isGeometry(Object obj){           
        return com.vividsolutions.jts.geom.Geometry.class.isAssignableFrom(obj.getClass())?true:false;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException{
		meta=(SpatialAnalysisMeta)smi;
		data=(SpatialAnalysisData)sdi;
		
        if (first){
            first = false;

            data.oneRowSet = findInputRowSet(meta.getReferenceStepName());
            
            if(data.oneRowSet.getRowMeta()!=null)
            	data.referenceIndex = data.oneRowSet.getRowMeta().indexOfValue(meta.getReferenceField()); 
            else
            	data.referenceIndex = getTransMeta().getStepFields(meta.getReferenceStepName()).indexOfValue(meta.getReferenceField()); 
       	
        	if (data.referenceIndex<0) 
            	throw new KettleException(Messages.getString("SpatialAnalysis.Exception.UnableToFindField", meta.getReferenceField())); 
        	       
        	data.one = getRowFrom(data.oneRowSet);  

            if (meta.isAlgoDual()){            	
				data.twoRowSet = findInputRowSet(meta.getCompareStepName());  
				
	            if(data.twoRowSet.getRowMeta()!=null)
	            	data.compareIndex = data.twoRowSet.getRowMeta().indexOfValue(meta.getCompareField()); 
	            else
	            	data.compareIndex = getTransMeta().getStepFields(meta.getCompareStepName()).indexOfValue(meta.getCompareField()); 
        
                if (data.compareIndex<0) 
                	throw new KettleException(Messages.getString("SpatialAnalysis.Exception.UnableToFindField", meta.getCompareField())); //$NON-NLS-1$

            	if (meta.getCompressFiles()){
            		data.two = getRowFrom(data.twoRowSet);          
	            	boolean err = addBuffer(data.twoRowSet.getRowMeta().clone(), data.two);
	            	while (err){
	            		data.two = getRowFrom(data.twoRowSet);
	            		err = addBuffer(data.outputRowMeta, data.two);
	            	}
            	}
    			   			
              	getCompareRow();
    	        
    	        if(!meta.getOneRow()){
    				Object[] tempRow = null;
    				Geometry compareSet = null;
            		
    				while (data.two!=null  && !isStopped()){          
            			compareSet = compareSet==null?checkGeometry(data.two[data.compareIndex]):compareSet.union(checkGeometry(data.two[data.compareIndex]));							           			
            			tempRow = data.two;
            			getCompareRow();
            		}  
    				
            		if (compareSet == null){
            			setOutputDone();
                        return false; 
            		} 
         		
            		tempRow[data.compareIndex] = compareSet; 
            		data.two = tempRow;
    			}  
            } 
 		  
    		//setting meta output
    		if (data.outputRowMeta == null){            
    			data.outputRowMeta = new RowMeta(); 
                meta.getFields(data.outputRowMeta, getStepname(), (meta.isAlgoDual() && meta.getOneRow())?new RowMetaInterface[] {data.oneRowSet.getRowMeta(), data.twoRowSet.getRowMeta()}:new RowMetaInterface[] {data.oneRowSet.getRowMeta()}, null, this);    
            }    		
        }
	
        if (data.one == null){
        	setOutputDone();
            return false;       
        }
        
        if (log.isRowLevel()) 
        	logRowlevel(Messages.getString("SpatialAnalysis.Log.DataInfo",data.one+"")+data.two);
 
        int outputIndex;
 
    	Geometry result = null;
    	Object[] outputRow;
    	       
    	if(meta.isAlgoDual() && meta.getOneRow()){
    		int outputIndexOne = data.oneRowSet.getRowMeta().size();
    		int outputIndexTwo = data.twoRowSet.getRowMeta().size();
        	outputIndex = outputIndexOne + outputIndexTwo;         		
        	outputRow = new Object[outputIndex];
        	for (int i = 0; i < outputIndex;i++){
        		outputRow[i] = (i < outputIndexOne)?data.one[i]:data.two[i-outputIndexOne];           		
    		}     		
    	}else{          		
    		outputRow = data.one;
    		outputIndex = data.oneRowSet.getRowMeta().size();
        }

    	Geometry geom = checkGeometry(data.one[data.referenceIndex]);

    	if(!meta.isAlgoDual() || data.two!=null){
	        switch (meta.getSpatialAnalysisByDesc()){
	            case 0:        	                   		     	                        
	        		result = geom.union(checkGeometry(data.two[data.compareIndex]));	        		
	            	break;
	            case 1:
	        		result = geom.intersection(checkGeometry(data.two[data.compareIndex]));        	
	            	break;
	            case 2:
	            	double dist = -1;
	            	try{
	            		dist = Double.parseDouble(meta.getDistField());	
	            	}catch(Exception e){
	            		logError(Messages.getString("SpatialAnalysis.Exception.WrongParameterType1") + Messages.getString("SpatialAnalysisDialog.DistField.Label") + Messages.getString("SpatialAnalysis.Exception.WrongParameterType2"));
	            	}
	            	result = geom.buffer(dist);
	            	break;
	            case 3:
	        		result = geom.symDifference(checkGeometry(data.two[data.compareIndex]));
	            	break;
	            case 4:
	            	result = geom.getInteriorPoint();
	            	break;
	            case 5:
	            	result = geom.getEnvelope();
	            	break;
	            case 6:
	            	result = geom.getCentroid();
	            	break;
	            case 7:
	            	result = geom.getBoundary();
	            	break;
	            case 8:
	        		result = geom.difference(checkGeometry(data.two[data.compareIndex]));
	            	break;
	            case 9:
	            	result = geom.convexHull();
	            	break;   
	            case 10:
	            	result = geom.reverse();
	            	break;         
	            default: 
	            	setOutputDone();
	                return false;  
	        }   
    	}
       
    	try {
			if(meta.isAlgoDual() && meta.getOneRow()){					
				int outputIndexOne = data.oneRowSet.getRowMeta().size();	                    		    		
		    	for (int i = outputIndexOne; i < outputIndex;i++){	                    		
		    		outputRow[i] = data.two[i-outputIndexOne];  
				} 					
			}
			putRow(data.outputRowMeta, RowDataUtil.addValueData(outputRow, outputIndex, result));							
		} catch (KettleStepException e) {
			throw new KettleException(e);
		}
		
        data.one = getRowFrom(data.oneRowSet);

        if (meta.isAlgoDual() && meta.getOneRow())
        	getCompareRow();          

        if (checkFeedback(getLinesRead()) && log.isBasic()) 
        	logBasic(Messages.getString("SpatialAnalysis.LineNumber")+getLinesRead());

		return true;
	}
	
	public void getCompareRow() throws KettleStepException, KettleValueException{
		data.two = meta.getCompressFiles()?getBuffer():getRowFrom(data.twoRowSet);
	}

	private Geometry checkGeometry(Object geom) throws KettleException{	 	                        		                    	                              
		if(!(geom instanceof Geometry))
			throw new KettleException("Unexpected class for Geometry field: "+ geom.getClass().toString());
		GeometryFactory gf = new GeometryFactory();
		if (geom instanceof Polygon)
			return gf.createMultiPolygon(new Polygon[] {(Polygon) geom});            			  								        			  							
		if (geom instanceof LineString)
			return gf.createMultiLineString(new LineString[] {(LineString) geom});            			  													
		return (Geometry) geom;
	}
	
	/**
     * @see StepInterface#init( org.pentaho.di.trans.step.StepMetaInterface , org.pentaho.di.trans.step.StepDataInterface)
     */
    public boolean init(StepMetaInterface smi, StepDataInterface sdi){
		meta = (SpatialAnalysisMeta)smi;
		data = (SpatialAnalysisData)sdi;

        if (super.init(smi, sdi)){         	           
	        if(meta.getCompressFiles()){
	        	data.writeSize = 5000;
				data.freeMemoryPctLimit = 25;
				data.buffer = new ArrayList<Object[]>(5000);
	            data.compressFiles =  meta.getCompressFiles();                   
	            data.rowBuffer=new ArrayList<Object[]>(data.writeSize);     
	            data.tempRows  = new ArrayList<RowTempFile>();            
	            data.minWriteSize = 1000; 
	        }
          
        	if (meta.getReferenceStepName() != null){
        		if (!Const.isEmpty(meta.getReferenceField())){
        			if (!Const.isEmpty(meta.getResultFieldName())){
    	    			if (meta.getSpatialAnalysisByDesc() == 2){//check if distance is a double for buffer
    	    				try{
    	    					Double.parseDouble(meta.getDistField());
    	    				}catch(Exception e){
    	    					logError(Messages.getString("SpatialAnalysisMeta.CheckResult.DistFieldMustBeDouble"));
    	    				}
    	    			}
        	    		//check compare input if transformation requires 2 streams
        	    		if (meta.isAlgoDual()){
        	    			if (meta.getCompareStepName() != null){
        	    				if (!Const.isEmpty(meta.getCompareField())){
        	    					if(meta.getCompareStepName().equals(meta.getReferenceStepName()))
        	    						logError(Messages.getString("SpatialAnalysisMeta.CheckResult.CompareSameAsReference"));   	    				
        	    					else
        	    						return true;       	    				
        	    				}else
        	    					logError(Messages.getString("SpatialAnalysisMeta.CheckResult.NoCompareFieldSpecified")); //$NON-NLS-1$        	    				
        	    			}else
        	    				logError(Messages.getString("SpatialAnalysisMeta.CheckResult.CompareStepMissing"));									        	    			
        	    		}else
        	    			return true;
        			}else
            			logError(Messages.getString("SpatialAnalysisMeta.CheckResult.NoResultFieldSpecified"));
        		}else
        			logError(Messages.getString("SpatialAnalysisMeta.CheckResult.NoReferenceFieldSpecified"));
        	}else
    			logError(Messages.getString("SpatialAnalysisMeta.CheckResult.ReferenceStepMissing"));                          	    
        }       
        return false;
    }

	public void run(){
    	BaseStep.runStepThread(this, meta, data);
	}
}