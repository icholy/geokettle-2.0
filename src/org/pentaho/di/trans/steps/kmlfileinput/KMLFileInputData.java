package org.pentaho.di.trans.steps.kmlfileinput;

import java.util.HashMap;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the KMLFileInput step.
 * 
 * @author tpelletierdoisy, eleprince, tbadard, jmathieu
 * @since 14-sep-2009
 */
public class KMLFileInputData extends BaseStepData implements StepDataInterface{
	
	public RowMetaInterface fields;
    public RowMetaInterface outputRowMeta;
    public FileInputList files;       
	public HashMap<FileObject, Object[]> passThruFields;	
	public Object[] currentPassThruFieldsRow;	
	public int nrPassThruFields;	
	public RowSet rowSet;
	public int fileIndex;
	public int readerRowSize;
	public int rowRead;

	public KMLFileInputData(){
		super();
		fields = null;
		fileIndex = 0;
		rowRead = 0;
	}
}