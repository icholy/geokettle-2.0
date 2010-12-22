package org.pentaho.di.trans.steps.spatialanalysis;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.sort.RowTempFile;

/**
 * @author JM
 * @since nov-2010
 *
 */
public class SpatialAnalysisData extends BaseStepData implements StepDataInterface{
    public RowMetaInterface outputRowMeta;
    public ValueMetaInterface oneInputValueMeta;
    public ValueMetaInterface twoInputValueMeta;      
    
    public Object[] one, two;
    
    public int referenceIndex;
    public int compareIndex;    

	public RowSet oneRowSet;
	public RowSet twoRowSet;
	
	public List<Object[]>   buffer;
	public List<FileObject> files;
	public List<Integer> bufferSizes;
	public List<InputStream> fis;
    public List<GZIPInputStream> gzis;
    public List<DataInputStream> dis;
	public List<Object[]> rowBuffer;
    public List<RowTempFile> tempRows;
	
	public int getBufferIndex;
	public int freeCounter;
	public int freeMemoryPct;
	public int minWriteSize;
	public int freeMemoryPctLimit;
	public int memoryReporting;
	public int writeSize;
	
	public boolean compressFiles;
	
	public SpatialAnalysisData(){
		super();		
		files= new ArrayList<FileObject>();
		fis  = new ArrayList<InputStream>();
        gzis  = new ArrayList<GZIPInputStream>();
        dis = new ArrayList<DataInputStream>();
        bufferSizes = new ArrayList<Integer>();
	}
}