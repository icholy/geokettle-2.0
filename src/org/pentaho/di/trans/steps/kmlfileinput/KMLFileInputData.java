package org.pentaho.di.trans.steps.kmlfileinput;

import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.KMLReader;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Provides data for the KMLFileInput step.
 * 
 * @author tpelletierdoisy, eleprince, tbadard, jmathieu
 * @since 14-sep-2009
 */
public class KMLFileInputData extends BaseStepData implements StepDataInterface{
	
	public ArrayList <KMLReader> kmlreader;
	public RowMetaInterface fields;
    public ArrayList <FileObject> file_kml;
    public FileInputList files;
    public RowMetaInterface outputRowMeta;   
	public int fileNr;

	public KMLFileInputData(){
		super();
		kmlreader=null;
		fields=null;
	}
}