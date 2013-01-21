package org.pentaho.di.trans.steps.ogrfileinput;

import java.util.List;

import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.OGRReader;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the GISFileInput step.
 * 
 * @author tbadard, jmathieu
 * @since 10-jun-2010
 */
public class OGRFileInputData extends BaseStepData implements StepDataInterface{
	public OGRReader ogrReader;
	public RowMetaInterface fields;
    public int fileNr;
    public FileInputList files; 
    public List<String> layernames;
    public List<String> cnxStrings;
	public int index;
	public int readerRowSize;
	public int rowRead;  
    public RowMetaInterface outputRowMeta;

    public RowMetaInterface prevStepFields;
	public OGRFileInputData(){
		super();
		ogrReader=null;
		fields=null;
		rowRead = 0;
		index = 0;
		layernames = null;
	}
}
