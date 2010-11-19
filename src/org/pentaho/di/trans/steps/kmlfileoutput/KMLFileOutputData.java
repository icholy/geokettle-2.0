package org.pentaho.di.trans.steps.kmlfileoutput;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.KMLWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the KMLFileOutput step.
 * 
 * @author tpelletierdoisy, eleprince, tbadard
 * @since 29-sep-2008
 */
public class KMLFileOutputData extends BaseStepData implements StepDataInterface
{
	public KMLWriter gtwriter;
	public RowMetaInterface fields;
    // public int fileNr;
    public FileObject file_kml;
    // public FileInputList files;
    public RowMetaInterface outputRowMeta;

	public KMLFileOutputData()
	{
		super();
		gtwriter=null;
		fields=null;
	}
}
