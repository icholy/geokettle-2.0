package org.pentaho.di.trans.steps.wfsinput;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class WFSInputData extends BaseStepData implements StepDataInterface{
	public RowMetaInterface outputRowMeta;
	
	public WFSInputData(){
		super();
	}		
}
