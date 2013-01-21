package org.pentaho.di.trans.steps.ogrfileoutput;

import java.util.List;

import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.OGRWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the OGRFileOutput step.
 * 
 * @author tbadard, jmathieu
 * @since 11-jun-2010
 */
public class OGRFileOutputData extends BaseStepData implements StepDataInterface{
	public OGRWriter ogrWriter;

    public String file_format;
    public String file_options;
    public int write_mode;
    public int file_geomtype;
    public String fid_field;
    //public String encoding;
    public boolean preserve_fid_field; 
    public int fileNr;
    public FileInputList files; 
    public List<String> layernames;
    public List<String> cnxStrings;
    public List<Boolean> newWriterFlags;

	public int index;
    public RowMetaInterface outputRowMeta;
	
	public OGRFileOutputData(){
		super();
		ogrWriter=null;
		index = 0;
		layernames = null;
	}
}
