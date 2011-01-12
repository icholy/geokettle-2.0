package org.pentaho.di.trans.steps.sosinput;
/*
 * Created on 18-february-2010
 * 
 * by: jmathieu
 *
 */
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.geospatial.SOSReader;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.sosinput.Messages;

public class SOSInput extends BaseStep implements StepInterface{
	private SOSInputMeta meta;
	private SOSInputData data;
	private boolean isReceivingInputFields=false;
	
	public SOSInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans){
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
    
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException{
		meta=(SOSInputMeta)smi;
		data=(SOSInputData)sdi;		
				
		Object [] r = getRow();
		
		if (isReceivingInputFields && r==null){
			setOutputDone();
			return false;			
		}
		
        if (first){ 
        	// we just got started
            first = false;

            data.outputRowMeta = new RowMeta();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);                       		                          
        }	
		       
        if (meta.getRequest().equals(Messages.getString("SOSInputDialog.Request.GetObservation"))){
        	
        	if (meta.isDifferentUrlObs() && Const.isEmpty(meta.getUrlObs()))
        		logError(Messages.getString("SOSInputMeta.CheckResult.NoUrlObs"));
        	
			try {
				data.sosReader.setURLObs(meta.isDifferentUrlObs() && Const.isEmpty(meta.getUrlObs()) ? new URL (meta.getUrlObs()):new URL (meta.getUrl()));
			} catch (MalformedURLException e) {
				logError(Messages.getString("SOSInput.Log.UnableCreateUrl") + e);      			
			}
			   	
        	//check if time bounds are specified in a previous field
        	String time1 = meta.isTime1InField()?(String) r[getInputRowMeta().indexOfValue(meta.getTime1Field())]:meta.getTime1();
        	String time2 = meta.isTime2InField()?(String) r[getInputRowMeta().indexOfValue(meta.getTime2Field())]:meta.getTime2();
        
        	ArrayList<Object[]> rows = data.sosReader.getObservations(meta.getOffering(), meta.getProcedures(), time1, time2, meta.getObservedProperties());
        	if (rows!=null){
        		for(Object [] row :rows){
        			putRow(data.outputRowMeta, row);
        		}  
        	}
        }

        if (checkFeedback(getLinesInput())) logBasic(Messages.getString("SOSInput.Log.LineNr")+getLinesInput()); //$NON-NLS-1$

        if(!isReceivingInputFields){
        	setOutputDone();
			return false;	
        }           
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi){
		meta = (SOSInputMeta) smi;
		data = (SOSInputData) sdi;

		if (super.init(smi, sdi)){
			try {
				if(meta.isTime1InField()||meta.isTime2InField())
					isReceivingInputFields=true;
				data.sosReader = new SOSReader(new URL(meta.getUrl()), meta.getMethod(), meta.getSOSVersion());
			} catch (Exception e) {
				new KettleException(Messages.getString("SOSInput.Log.UnableCreateUrl"), e);
			}   
			return true;
		}
		return false;
	}
		
	public void dispose(StepMetaInterface smi, StepDataInterface sdi){
	    meta = (SOSInputMeta)smi;
	    data = (SOSInputData)sdi;
	    
	    super.dispose(smi, sdi);
	}

	// Run is were the action happens!
	public void run(){
		try{
			logBasic(Messages.getString("SOSInput.Log.StartingToRun"));		 //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data) );
		}
		catch(Exception e){
			logError(Messages.getString("SOSInput.Log.Error.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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