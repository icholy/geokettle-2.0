package org.pentaho.di.trans.steps.sosinput;
/*
 * Created on 18-february-2010
 * 
 * by: jmathieu
 *
 */
import org.pentaho.di.core.geospatial.SOSReader;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class SOSInputData extends BaseStepData implements StepDataInterface{
	public RowMetaInterface 	outputRowMeta;
	public SOSReader 			sosReader;
	public RowMetaInterface 	fields;

	public SOSInputData(){
		super();
		sosReader=null;
		fields=null;
	}		
}