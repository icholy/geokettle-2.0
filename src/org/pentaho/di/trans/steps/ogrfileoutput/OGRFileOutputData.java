package org.pentaho.di.trans.steps.ogrfileoutput;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.geospatial.OGRWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the OGRFileOutput step.
 * 
 * @author tbadard
 * @since 11-jun-2010
 */
public class OGRFileOutputData extends BaseStepData implements StepDataInterface
{
	public OGRWriter ogrWriter;
	public RowMetaInterface fields;
    public FileObject file_gis;
    public String file_format;
    public String file_options;
    public int file_geomtype;
    public RowMetaInterface outputRowMeta;

	public OGRFileOutputData()
	{
		super();
		ogrWriter=null;
		fields=null;
	}
}
