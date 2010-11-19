package org.pentaho.di.trans.steps.gmlfileoutput;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.GMLWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the GMLFileOutput step.
 * 
 * @author jmathieu, tbadard
 * @since 10-may-2010
 */
public class GMLFileOutputData extends BaseStepData implements StepDataInterface
{
	// public XBase xbi;
	public GMLWriter gmlwriter;
	public RowMetaInterface fields;
    public int fileNr;
    // public java.net.URL file_gis;
    public FileObject file_gml;
    public FileInputList files;
    public RowMetaInterface outputRowMeta;

	public GMLFileOutputData()
	{
		super();
		// xbi=null;
		gmlwriter=null;
		fields=null;
	}
}
