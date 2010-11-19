package org.pentaho.di.trans.steps.kmlfileinput;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.KMLReader;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Provides data for the KMLFileInput step.
 * 
 * @author tpelletierdoisy, eleprince, tbadard
 * @since 14-sep-2009
 */
public class KMLFileInputData extends BaseStepData implements StepDataInterface{
	public KMLReader gtreader;
	public RowMetaInterface fields;
    public int fileNr;
    public FileObject file_kml;
    public FileInputList files;
    public RowMetaInterface outputRowMeta;

	public KMLFileInputData()
	{
		super();
		gtreader=null;
		fields=null;
	}
}
