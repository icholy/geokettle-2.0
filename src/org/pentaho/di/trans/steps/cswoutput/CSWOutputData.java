/**
 * 
 */
package org.pentaho.di.trans.steps.cswoutput;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author O.Mamadou
 *
 */
public class CSWOutputData extends BaseStepData implements StepDataInterface {

	/**
	 * 
	 */
	public RowMetaInterface fields;
    public RowMetaInterface outputRowMeta;
    public CSWWriter cswWriter;
	public CSWOutputData() {
		super();
		fields=null;
		cswWriter=null;
	}

}
