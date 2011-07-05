package org.pentaho.di.trans.steps.gmlfileoutput;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.RowSet;
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
public class GMLFileOutputData extends BaseStepData implements StepDataInterface{
    public RowMetaInterface outputRowMeta;   
	public GMLWriter  gmlWriter;
    public String charset;
    public FileObject file;  
    public RowSet rowSet;
	
	public GMLFileOutputData(){
		super();
		gmlWriter=null;
	    file=null;
	}
}
