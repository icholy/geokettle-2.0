package org.pentaho.di.trans.steps.gisfileoutput;

import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.geospatial.GeotoolsWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the GISFileOutput step.
 * 
 * @author jmathieu, etdub
 * @since 29-sep-2008
 */
public class GISFileOutputData extends BaseStepData implements StepDataInterface{
	public ArrayList <GeotoolsWriter>  gtwriter;
	public RowMetaInterface fields;
    public ArrayList <FileObject> file_gis;
    public RowMetaInterface outputRowMeta;

	public GISFileOutputData(){
		super();
		gtwriter=null;
		fields=null;
		file_gis=null;
	}
}
