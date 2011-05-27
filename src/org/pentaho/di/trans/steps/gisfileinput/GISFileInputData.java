package org.pentaho.di.trans.steps.gisfileinput;

import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.GeotoolsReader;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the GISFileInput step.
 * 
 * @author etdub, jmathieu, tbadard
 * @since 29-sep-2008
 */
public class GISFileInputData extends BaseStepData implements StepDataInterface
{
	public ArrayList <GeotoolsReader>  gtreader;
	public RowMetaInterface fields;
	public int fileNr;
    public ArrayList <FileObject> file_gis;
    public ArrayList <String> charset;
    public RowMetaInterface outputRowMeta;
    public FileInputList files;

	public GISFileInputData(){
		super();
		gtreader=null;
		fields=null;
	}
}
