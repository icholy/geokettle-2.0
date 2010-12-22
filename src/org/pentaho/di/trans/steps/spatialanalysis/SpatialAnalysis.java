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
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
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

/**
 * @author JM
 * @since nov-2010
 */
public class SpatialAnalysis extends BaseStep implements StepInterface{
    
	private SpatialAnalysisMeta meta;
	private SpatialAnalysisData data;
	
	private boolean hasTwoInputStreams = false;	
	private boolean oneRow = false;	
	private boolean keepAttributes = false;
	
	public SpatialAnalysis(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans){
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private boolean addBuffer(RowMetaInterface rowMeta, Object[] r) throws KettleException{
		if (r != null)						
			data.buffer.add(r);
		else
			return false;

		data.freeCounter++;
		
		boolean doWrite = data.buffer.size() == data.writeSize; // Buffer is full: dump to disk
		doWrite |= data.files.size() > 0 && r == null && data.buffer.size() > 0; // No more records: join from disk 
		doWrite |= data.freeMemoryPctLimit > 0 && data.freeMemoryPct < data.freeMemoryPctLimit && data.buffer.size() >= data.minWriteSize;
		
		if (doWrite){				
			// write rows to disk...
			DataOutputStream dos;
			GZIPOutputStream gzos;
			int p;
			
			try{
				FileObject fileObject=KettleVFS.createTempFile("spatialAnalysis", ".tmp", environmentSubstitute(Messages.getString("System.Button.Browse")));
				
				data.files.add(fileObject); // Remember the files!
				OutputStream outputStream = KettleVFS.getOutputStream(fileObject,false);
				if (data.compressFiles){
					gzos = new GZIPOutputStream(new BufferedOutputStream(outputStream));
					dos = new DataOutputStream(gzos);
				}else{
					dos = new DataOutputStream(new BufferedOutputStream(outputStream, 500000));
					gzos = null;
				}                              							
                
				// How many records do we have left?
				data.bufferSizes.add( data.buffer.size() );
                
                for (p = 0; p < data.buffer.size(); p++){
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
                //
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
	
	private Object[] getBuffer() throws KettleValueException{
		Object[] retval;
		
		// Open all files at once and read one row from each file...
		if (data.files.size() > 0 && ( data.dis.size() == 0 || data.fis.size() == 0 )){
			if(log.isBasic())
				logBasic("Opening " + data.files.size() + " tmp-files...");		
			try{
				for (int f=0;f<data.files.size() && !isStopped();f++){
					FileObject fileObject = (FileObject)data.files.get(f);
                    String filename = KettleVFS.getFilename(fileObject);
					if (log.isDetailed()) logDetailed("Opening tmp-file: ["+filename+"]");
					InputStream fi=KettleVFS.getInputStream(fileObject);
					DataInputStream di;
					data.fis.add(fi);
					if (data.compressFiles){
						GZIPInputStream gzfi = new GZIPInputStream(new BufferedInputStream(fi));
						di =new DataInputStream(gzfi);
						data.gzis.add(gzfi);
					}else
						di=new DataInputStream(new BufferedInputStream(fi, 50000));
					
					data.dis.add(di);
					
					// How long is the buffer?
					int buffersize=data.bufferSizes.get(f);
					
					if (log.isDetailed()) logDetailed("["+filename+"] expecting "+buffersize+" rows...");
					
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
		
		if (data.files.size()==0){
			if (data.getBufferIndex<data.buffer.size()){
				retval=(Object[])data.buffer.get(data.getBufferIndex);
				data.getBufferIndex++;
			}else
				retval=null;
		}else{
			if (data.rowBuffer.size()==0){
                retval=null;
            }else{
				// We now have "filenr" rows waiting: which one is the smallest?
				//
				if (log.isRowLevel()){
				    for (int i=0;i<data.rowBuffer.size() && !isStopped();i++){
					    Object[] b = (Object[])data.rowBuffer.get(i);
					    logRowlevel("--BR#"+i+": "+data.outputRowMeta.getString(b));
				    }
				}				
				RowTempFile rowTempFile = data.tempRows.remove(0);
				retval = rowTempFile.row;												
			}
		}
		return retval;
	}
	
	public boolean isGeometry(Object[] r, int index){
		Class<?> c = r[index].getClass();	            
            if(!com.vividsolutions.jts.geom.Geometry.class.isAssignableFrom(c))
            	return false; 
        return true;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException{
		meta=(SpatialAnalysisMeta)smi;
		data=(SpatialAnalysisData)sdi;

		boolean err = true;
		
        if (first){
            first = false;
                    
            try{
            	data.oneRowSet = findInputRowSet(meta.getReferenceStepName()); 
            	data.one = getRowFrom(data.oneRowSet);  
            }catch(Exception e){
            	throw new KettleException(Messages.getString("SpatialAnalysisMeta.CheckResult.ReferenceStepMissing", meta.getReferenceStepName())); //$NON-NLS-1$
            }               		  		                                            
    		
    		data.referenceIndex =  data.oneRowSet.getRowMeta().indexOfValue(meta.getReferenceField());               
            
    		if (data.referenceIndex<0) 
            	throw new KettleException(Messages.getString("SpatialAnalysis.Exception.UnableToFindField", meta.getReferenceField())); //$NON-NLS-1$
            
            if (hasTwoInputStreams){            	
    			try{
    				data.twoRowSet = findInputRowSet(meta.getCompareStepName());                 	
                }catch(Exception e){
                	throw new KettleException(Messages.getString("SpatialAnalysisMeta.CheckResult.CompareStepMissing", meta.getCompareStepName())); //$NON-NLS-1$
                }

            	data.two = getRowFrom(data.twoRowSet);
            	
            	if (data.two != null){
                    data.compareIndex =  data.twoRowSet.getRowMeta().indexOfValue(meta.getCompareField());          
                    if (data.compareIndex<0) 
                    	throw new KettleException(Messages.getString("SpatialAnalysis.Exception.UnableToFindField", meta.getCompareField())); //$NON-NLS-1$
                }
            	
            	if (!oneRow){
	            	//put every rows in the buffer            
	            	err = addBuffer(data.twoRowSet.getRowMeta().clone(), data.two);
	            	while (err){
	            		data.two = getRowFrom(data.twoRowSet);
	            		err = addBuffer(data.outputRowMeta, data.two);
	            	}
            	}
            } 
 		  
    		//setting meta output
    		if (data.outputRowMeta == null){            
            	data.outputRowMeta = data.oneRowSet.getRowMeta().clone();  
            	if(keepAttributes && hasTwoInputStreams)
                	data.outputRowMeta.mergeRowMeta(data.twoRowSet.getRowMeta().clone());           	        	
                meta.getFields(data.outputRowMeta, getStepname(), new RowMetaInterface[] { data.oneRowSet.getRowMeta() }, null, this);           
            }
        }
	
        if (log.isRowLevel()) 
        	logRowlevel(Messages.getString("SpatialAnalysis.Log.DataInfo",data.one+"")+data.two); //$NON-NLS-1$ //$NON-NLS-2$

        Object[] outputRow;
        int outputIndex;
        Geometry result = null;
           
        if (data.one == null){
        	setOutputDone();
            return false;       
        }else{        	
        	Class<?> c = data.one[data.referenceIndex].getClass();           
            if(!com.vividsolutions.jts.geom.Geometry.class.isAssignableFrom(c))
            	throw new KettleException(Messages.getString("SpatialAnalysis.Exception.ReferenceMustBeGeometry", meta.getReferenceField()));
            
        	if(!keepAttributes || !hasTwoInputStreams){
        		outputRow = data.one;
        		outputIndex = data.oneRowSet.getRowMeta().size();
        	}else{          		
        		int outputIndexOne = data.oneRowSet.getRowMeta().size();
        		int outputIndexTwo = data.twoRowSet.getRowMeta().size();
            	outputIndex = outputIndexOne + outputIndexTwo;         		
            	outputRow = new Object[outputIndex];
            	for (int i = 0; i < outputIndex;i++){
            		if(i < outputIndexOne)
            			outputRow[i] = data.one[i];
            		else 
            			if(hasTwoInputStreams && oneRow && data.two!=null)
            				outputRow[i] = data.two[i-outputIndexOne];  
        		} 
            }
        	
        	if(hasTwoInputStreams && oneRow){
   				if (data.two != null){
   	            	c = data.two[data.compareIndex].getClass();	            
       	        	if(!com.vividsolutions.jts.geom.Geometry.class.isAssignableFrom(c))
       	        		throw new KettleException(Messages.getString("SpatialAnalysis.Exception.CompareMustBeGeometry", meta.getCompareField()));    	                        		                    	                                    					
   				}else{
   					result = null;
                       putRow(data.outputRowMeta, RowDataUtil.addValueData(outputRow, outputIndex, result));
                       data.one=getRowFrom(data.oneRowSet);
                       data.two=getRowFrom(data.twoRowSet);
                       if (checkFeedback(getLinesRead())){
                       		if(log.isBasic()) logBasic(Messages.getString("SpatialAnalysis.LineNumber")+getLinesRead()); //$NON-NLS-1$
                       }
               		return true;
   				}
   			}
   
            switch (meta.getSpatialAnalysisByDesc()){
	            case 0: 
	            	if (oneRow)
	            		result = ((Geometry) data.one[data.referenceIndex]).union((Geometry) data.two[data.compareIndex]);
	            	else{
	            		Object[] bufferedRow = getBuffer();
	            		while (bufferedRow!=null  && !isStopped()){
	            			c = bufferedRow[data.compareIndex].getClass();	            
	            	        if(!com.vividsolutions.jts.geom.Geometry.class.isAssignableFrom(c))
	            	        	throw new KettleException(Messages.getString("SpatialAnalysis.Exception.CompareMustBeGeometry", meta.getCompareField()));    	                        		     	                        
	            			result = ((Geometry) data.one[data.referenceIndex]).union((Geometry) bufferedRow[data.compareIndex]);

            				int outputIndexOne = data.oneRowSet.getRowMeta().size();	                    		    		
                        	for (int i = outputIndexOne; i < outputIndex;i++){	                    		
                        		outputRow[i] = bufferedRow[i-outputIndexOne];  
                    		}         				
	            			putRow(data.outputRowMeta, RowDataUtil.addValueData(outputRow, outputIndex, result));
	            			bufferedRow = getBuffer();
	            		}
	            		data.getBufferIndex = 0;
	            	}
	            	break;
	            case 1:
	            	if (oneRow)
	            		result = ((Geometry) data.one[data.referenceIndex]).intersection((Geometry) data.two[data.compareIndex]);
	            	else{
	            		Object[] bufferedRow = getBuffer();
	            		while (bufferedRow !=null   && !isStopped()){
	            			c = bufferedRow[data.compareIndex].getClass();	            
	            	        if(!com.vividsolutions.jts.geom.Geometry.class.isAssignableFrom(c))
	            	        	throw new KettleException(Messages.getString("SpatialAnalysis.Exception.CompareMustBeGeometry", meta.getCompareField()));    	                        		     	                        
	            			result = ((Geometry) data.one[data.referenceIndex]).intersection((Geometry) bufferedRow[data.compareIndex]);

            				int outputIndexOne = data.oneRowSet.getRowMeta().size();	                    		    		
                        	for (int i = outputIndexOne; i < outputIndex;i++){	                    		
                        		outputRow[i] = bufferedRow[i-outputIndexOne];  
                    		} 
           				
	            			putRow(data.outputRowMeta, RowDataUtil.addValueData(outputRow, outputIndex, result));
	            			bufferedRow = getBuffer();
	            		}
	            		data.getBufferIndex = 0;
	            	}
	            	break;
	            case 2:
	            	result = ((Geometry) data.one[data.referenceIndex]).buffer(Double.parseDouble(meta.getDistField()));
	            	break;
	            case 3:
	            	if (oneRow)
	            		result = ((Geometry) data.one[data.referenceIndex]).symDifference((Geometry) data.two[data.compareIndex]);
	            	else{
	            		Object[] bufferedRow = getBuffer();
	            		while (bufferedRow != null  && !isStopped()){
	            			c = bufferedRow[data.compareIndex].getClass();	            
	            	        if(!com.vividsolutions.jts.geom.Geometry.class.isAssignableFrom(c))
	            	        	throw new KettleException(Messages.getString("SpatialAnalysis.Exception.CompareMustBeGeometry", meta.getCompareField()));    	                        		     	                        
	            			result = ((Geometry) data.one[data.referenceIndex]).symDifference((Geometry) bufferedRow[data.compareIndex]);

            				int outputIndexOne = data.oneRowSet.getRowMeta().size();	                    		    		
                        	for (int i = outputIndexOne; i < outputIndex;i++){	                    		
                        		outputRow[i] = bufferedRow[i-outputIndexOne];  
                    		} 
           				
	            			putRow(data.outputRowMeta, RowDataUtil.addValueData(outputRow, outputIndex, result));
	            			bufferedRow = getBuffer();
	            		}
	            		data.getBufferIndex = 0;
	            	}
	            	break;
	            case 4:
	            	result = ((Geometry) data.one[data.referenceIndex]).getInteriorPoint();
	            	break;
	            case 5:
	            	result = ((Geometry) data.one[data.referenceIndex]).getEnvelope();
	            	break;
	            case 6:
	            	result = ((Geometry) data.one[data.referenceIndex]).getCentroid();
	            	break;
	            case 7:
	            	result = ((Geometry) data.one[data.referenceIndex]).getBoundary();
	            	break;
	            case 8:
	            	if (oneRow)
	            		result = ((Geometry) data.one[data.referenceIndex]).difference((Geometry) data.two[data.compareIndex]);
	            	else{
	            		Object[] bufferedRow = getBuffer();
	            		while (bufferedRow != null  && !isStopped()){
	            			c = bufferedRow[data.compareIndex].getClass();	            
	            	        if(!com.vividsolutions.jts.geom.Geometry.class.isAssignableFrom(c))
	            	        	throw new KettleException(Messages.getString("SpatialAnalysis.Exception.CompareMustBeGeometry", meta.getCompareField()));    	                        		     	                        
	            			result = ((Geometry) data.one[data.referenceIndex]).difference((Geometry) bufferedRow[data.compareIndex]);

            				int outputIndexOne = data.oneRowSet.getRowMeta().size();	                    		    		
                        	for (int i = outputIndexOne; i < outputIndex;i++){	                    		
                        		outputRow[i] = bufferedRow[i-outputIndexOne];  
                    		} 
           				
	            			putRow(data.outputRowMeta, RowDataUtil.addValueData(outputRow, outputIndex, result));
	            			bufferedRow = getBuffer();
	            		}
	            		data.getBufferIndex = 0;
	            	}
	            	break;
	            case 9:
	            	result = ((Geometry) data.one[data.referenceIndex]).convexHull();
	            	break;   
	            case 10:
	            	result = ((Geometry) data.one[data.referenceIndex]).reverse();
	            	break;         
	            default: 
	            	setOutputDone();
	                return false;  
            }                                  
           
            data.one = getRowFrom(data.oneRowSet);
            
            if (hasTwoInputStreams && oneRow)
            	data.two = getRowFrom(data.twoRowSet);                                    
        }
        
        // send the row to the next steps...
        if (oneRow || !hasTwoInputStreams)
        	putRow(data.outputRowMeta, RowDataUtil.addValueData(outputRow, outputIndex, result));

        if (checkFeedback(getLinesRead()))
        	if(log.isBasic()) logBasic(Messages.getString("SpatialAnalysis.LineNumber")+getLinesRead()); //$NON-NLS-1$

		return true;
	}

	/**
     * @see StepInterface#init( org.pentaho.di.trans.step.StepMetaInterface , org.pentaho.di.trans.step.StepDataInterface)
     */
    public boolean init(StepMetaInterface smi, StepDataInterface sdi){
		meta = (SpatialAnalysisMeta)smi;
		data = (SpatialAnalysisData)sdi;

        if (super.init(smi, sdi)){    
       	
	        hasTwoInputStreams = meta.isAlgoDual();            
	        oneRow = meta.getOneRow();
	        keepAttributes = meta.getAttributes();
	        
	        if(!oneRow){
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
        				int analysisType = meta.getSpatialAnalysisByDesc();
    	    			if (analysisType == 2){//check if distance is a double for buffer
    	    				try{
    	    					 Double.parseDouble(meta.getDistField());
    	    				}catch(Exception e){
    	    					logError(Messages.getString("SpatialAnalysisMeta.CheckResult.DistFieldMustBeDouble")); //$NON-NLS-1$
    	    				}
    	    			}
        	    		//check compare input if transformation requires 2 streams
        	    		if (analysisType == 0 || analysisType == 1 || analysisType == 3 || analysisType == 8){
        	    			if (meta.getCompareStepName() != null){			
        	    				if (!Const.isEmpty(meta.getCompareField()))
        	    					return true;
        	    				else
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

	// Run is were the action happens!
	public void run(){
    	BaseStep.runStepThread(this, meta, data);
	}
}