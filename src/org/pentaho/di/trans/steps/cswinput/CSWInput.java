package org.pentaho.di.trans.steps.cswinput;

import java.io.IOException;
import java.util.ArrayList;


import javax.servlet.ServletException;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;

import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;





public class CSWInput extends BaseStep implements StepInterface {
	
	private CSWInputMeta meta;
	private CSWInputData data;
	private boolean isReceivingInputFields=false;

	public CSWInput(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException{
		meta=(CSWInputMeta)smi;
		data=(CSWInputData)sdi;
		Object [] r = getRow();
		if (isReceivingInputFields && r==null){
			setOutputDone();
			return false;			
		}
		CSWReader cswParam = new CSWReader();
		try {
			cswParam = meta.getCswParam().getParametersValues(this);
		} catch (CloneNotSupportedException e1) {
			throw new KettleException(e1);
		}
		if (first){ 
        	// we just got started
            first = false;
            data.outputRowMeta=cswParam.getColumnField();
                     
            if (data.outputRowMeta==null){
            	data.outputRowMeta = new RowMeta();
            	 meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            }
                                  		                          
        }
		
		try {
			ArrayList<ArrayList<Object>> rows = cswParam.getCatalogRecords();
			if (rows!=null){
	    		
	    		for(ArrayList<Object> objectList:rows){
	    			Object[] row=objectList.toArray(new Object[data.outputRowMeta.size()]);
	    			//
	    			putRow(data.outputRowMeta, row);	    			
	    		}  
	    	}
		} catch (ServletException e) {
			throw new KettleException(e);
			//
		} catch (IOException e) {
			throw new KettleException(e);
		}
		if(!isReceivingInputFields){
        	setOutputDone();
			return false;	
        }           
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi){
		meta = (CSWInputMeta) smi;
		data = (CSWInputData) sdi;

		if (super.init(smi, sdi)){
			 
			return true;
		}
		return false;
	}
	//
	
	public void run(){
		try{
			logBasic(Messages.getString("CSWInput.Log.StartingToRun"));
			while (!isStopped() && processRow(meta, data) );
		}
		catch(Exception e){
			logError(Messages.getString("CSWInput.Log.Error.UnexpectedError")+" : "+e.toString());
            logError(Const.getStackTracker(e));
            setErrors(1);
			stopAll();
		}
		finally{
		    dispose(meta, data);
			markStop();
		    logSummary();
		}
	}
}
