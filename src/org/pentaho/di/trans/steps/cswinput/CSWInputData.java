package org.pentaho.di.trans.steps.cswinput;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class CSWInputData extends BaseStepData implements StepDataInterface {
	public RowMetaInterface fields;
    public RowMetaInterface outputRowMeta;
    public CSWReader cswReader;
    
	public CSWInputData() {
		// TODO Auto-generated constructor stub
		super();
		fields=null;
		cswReader=null;
	}

}
