package org.pentaho.di.trans.steps.gisfileinput;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.GeotoolsReader;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the GISFileInput step.
 * 
 * @author etdub
 * @since 29-sep-2008
 */
public class GISFileInputData extends BaseStepData implements StepDataInterface
{
	public GeotoolsReader gtreader;
	public RowMetaInterface fields;
    public int fileNr;
    public FileObject file_gis;
    public FileInputList files;
    public RowMetaInterface outputRowMeta;

	public GISFileInputData()
	{
		super();
		gtreader=null;
		fields=null;
	}

}
