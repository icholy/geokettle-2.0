package org.pentaho.di.trans.steps.gmlfileoutput;

import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
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
	public ArrayList <GMLWriter> gmlwriter;
	public RowMetaInterface fields;
    public ArrayList <FileObject> file_gml;
    public RowMetaInterface outputRowMeta;

	public GMLFileOutputData()
	{
		super();
		file_gml = null;
		gmlwriter=null;
		fields=null;
	}
}
