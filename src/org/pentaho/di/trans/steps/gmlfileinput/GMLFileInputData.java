package org.pentaho.di.trans.steps.gmlfileinput;

import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.GMLReader;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the GMLFileInput step.
 * 
 * @author jmathieu, tbadard
 * @since 10-may-2010
 */
public class GMLFileInputData extends BaseStepData implements StepDataInterface
{	
	public ArrayList <GMLReader> gmlreader;
	public RowMetaInterface fields;
    public ArrayList <FileObject> file_gml;
    public RowMetaInterface outputRowMeta;   
	public int fileNr;
    public FileInputList files;
    
	public GMLFileInputData(){
		super();
		gmlreader=null;
		fields=null;
	}
}