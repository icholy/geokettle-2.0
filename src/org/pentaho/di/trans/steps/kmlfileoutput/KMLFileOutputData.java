package org.pentaho.di.trans.steps.kmlfileoutput;

import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.geospatial.KMLWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the KMLFileOutput step.
 * 
 * @author tpelletierdoisy, eleprince, tbadard, jmathieu
 * @since 29-sep-2008
 */
public class KMLFileOutputData extends BaseStepData implements StepDataInterface
{
	public ArrayList <KMLWriter> kmlwriter;
	public RowMetaInterface fields;
	public ArrayList <FileObject> file_kml;
    public RowMetaInterface outputRowMeta;

	public KMLFileOutputData()
	{
		super();
		kmlwriter=null;
		file_kml=null;
		fields=null;
	}
}
