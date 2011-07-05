package org.pentaho.di.trans.steps.kmlfileoutput;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.RowSet;
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
public class KMLFileOutputData extends BaseStepData implements StepDataInterface{
    public RowMetaInterface outputRowMeta;   
	public KMLWriter  kmlWriter;
    public String charset;
    public FileObject file;  
    public RowSet rowSet;
	
	public KMLFileOutputData(){
		super();
		kmlWriter=null;
	    file=null;
	}
}
