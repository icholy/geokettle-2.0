package org.pentaho.di.trans.steps.ogrfileinput;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.OGRReader;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the GISFileInput step.
 * 
 * @author tbadard
 * @since 10-jun-2010
 */
public class OGRFileInputData extends BaseStepData implements StepDataInterface
{
	public OGRReader ogrReader;
	public RowMetaInterface fields;
    public int fileNr;
    public FileObject file_gis;
    public FileInputList files;
    
    public String connectionString;
    public String layerName;
    
    public RowMetaInterface outputRowMeta;

	public OGRFileInputData()
	{
		super();
		ogrReader=null;
		fields=null;
	}

}
