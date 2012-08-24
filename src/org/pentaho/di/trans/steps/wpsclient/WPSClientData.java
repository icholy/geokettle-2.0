package org.pentaho.di.trans.steps.wpsclient;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class WPSClientData extends BaseStepData implements StepDataInterface{
	public RowMetaInterface 	outputRowMeta;
	public RowMetaInterface 	fields;

	public WPSClientData(){
		super();
		fields=null;
	}		
}
