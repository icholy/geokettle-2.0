package org.pentaho.di.trans.steps.gmlfileinput;

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
	
	public GMLReader gmlreader;
	public RowMetaInterface fields;
    public int fileNr;
    // public java.net.URL file_gml;
    public FileObject file_gml;
    public FileInputList files;
    public RowMetaInterface outputRowMeta;

	public GMLFileInputData()
	{
		super();
		// xbi=null;
		gmlreader=null;
		fields=null;
	}

}
