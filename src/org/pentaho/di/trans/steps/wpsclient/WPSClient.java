package org.pentaho.di.trans.steps.wpsclient;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.wpsclient.parameter.Parameter;
import org.pentaho.di.trans.steps.wpsclient.requestbuilder.ExecuteRequestBuilder;

/*
Deegree (3.1.1):
	GetCapabilities: GET  -- OK
					 POST -- OK
	DescribeProcess: GET  -- OK
					 POST -- OK
	Execute: GET  -- REFERENCE -- RawDataOutput    -- OK
	                           -- ResponseDocument -- OK
					 INLINE    -- RawDataOutput    -- NOT SUPPORTED
	                           -- ResponseDocument -- NOT SUPPORTED
			 POST -- REFERENCE -- RawDataOutput    -- OK
	                           -- ResponseDocument -- OK
			 		 INLINE    -- RawDataOutput    -- OK
	                           -- ResponseDocument -- OK
	                                                     
Deegree (Demo 3.2-pre3):
	GetCapabilities: GET  -- OK
					 POST -- OK
	DescribeProcess: GET  -- OK
					 POST -- OK
	Execute: GET  -- REFERENCE -- RawDataOutput    -- Untested ... Connection refused on demo instance
	                           -- ResponseDocument -- Untested ... Connection refused on demo instance
					 INLINE    -- RawDataOutput    -- NOT SUPPORTED
	                           -- ResponseDocument -- NOT SUPPORTED
			 POST -- REFERENCE -- RawDataOutput    -- OK
	                           -- ResponseDocument -- OK
			 		 INLINE    -- RawDataOutput    -- OK
	                           -- ResponseDocument -- OK

Geoserver (2.1.3):
	GetCapabilities: GET  -- OK
					 POST -- OK
	DescribeProcess: GET  -- OK
					 POST -- OK
	Execute: GET  -- REFERENCE -- RawDataOutput    -- CONDITIONNAL ...Crashes when providing output attributes ...use default values.
	                           -- ResponseDocument -- CONDITIONNAL ...Crashes when providing output attributes ...use default values.
			 		 INLINE    -- RawDataOutput    -- CONDITIONNAL ...Crashes when providing output attributes ...use default values.
	                           -- ResponseDocument -- CONDITIONNAL ...Crashes when providing output attributes ...use default values.
			 POST -- REFERENCE -- RawDataOutput    -- OK
	                           -- ResponseDocument -- OK
			 		 INLINE    -- RawDataOutput    -- OK
	                           -- ResponseDocument -- OK
			 
PYWPS (3.2-soap):
	GetCapabilities: GET  -- OK
					 POST -- OK
	DescribeProcess: GET  -- OK
					 POST -- OK
	Execute: GET  -- REFERENCE -- RawDataOutput    -- CONDITIONNAL ...Crashes when providing output attributes ...use default values.
							   -- ResponseDocument -- CONDITIONNAL ...Crashes when providing output attributes ...use default values.
			      -- INLINE	   -- RawDataOutput    -- CONDITIONNAL ...Crashes when providing output attributes ...use default values.
							   -- ResponseDocument -- CONDITIONNAL ...Crashes when providing output attributes ...use default values.
			 POST -- REFERENCE -- RawDataOutput    -- OK
							   -- ResponseDocument -- OK
			      -- INLINE	   -- RawDataOutput    -- OK
							   -- ResponseDocument -- OK

Zoo (1.0):
	GetCapabilities: GET  -- OK
					 POST -- OK
	DescribeProcess: GET  -- OK
					 POST -- OK
	Execute: GET  -- REFERENCE -- RawDataOutput    -- OK
	                           -- ResponseDocument -- OK
					 INLINE    -- RawDataOutput    -- OK
	                           -- ResponseDocument -- OK
			 POST -- REFERENCE -- RawDataOutput    -- NOT SUPPORTED
	                           -- ResponseDocument -- OK
			 		 INLINE    -- RawDataOutput    -- NOT SUPPORTED
	                           -- ResponseDocument -- OK
	
52n (2.0 RC7):
	GetCapabilities: GET  -- OK
					 POST -- NOT SUPPORTED
	DescribeProcess: GET  -- OK
					 POST -- NOT SUPPORTED
	Execute: GET  -- REFERENCE -- RawDataOutput    -- OK
	                           -- ResponseDocument -- OK
					 INLINE    -- RawDataOutput    -- NOT SUPPORTED 
	                           -- ResponseDocument -- NOT SUPPORTED 
			 POST -- REFERENCE -- RawDataOutput    -- OK
	                           -- ResponseDocument -- OK
			 		 INLINE    -- RawDataOutput    -- OK
	                           -- ResponseDocument -- OK
			 		 		 		 
Notes: 
	  For all above implementations, mimetypes with ";" (subtype) are not 
	  supported in GET method...Use with default or non containing ";" mimetypes.
*/

public class WPSClient extends BaseStep implements StepInterface{
	private WPSClientMeta meta;
	private WPSClientData data;
	
	private boolean receiveInputRows = true;
	
	public WPSClient(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans){
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}	

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException{
		meta=(WPSClientMeta)smi;
		data=(WPSClientData)sdi;		

        Object[] r = getRow();
        
        if (first){ 
            first = false;
            if(r == null){
            	receiveInputRows = false;
            	data.outputRowMeta = new RowMeta();
            }else
            	data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this); 			
        }	

        if(r == null){
        	if(receiveInputRows){
        		setOutputDone();
        		return false;
        	}else
        		r = RowDataUtil.allocateRowData(data.outputRowMeta.size());
        }
        
        Map<String, Parameter> inputParameters = meta.getInputParameters();
		Map<String, Parameter> inputParametersCopy = new HashMap<String, Parameter>(inputParameters);
        for(String key: meta.getInputParameters().keySet()){        	
        	Parameter param = inputParameters.get(key);
        	Parameter paramCopy = (Parameter)param.clone();
        	String fieldName = inputParameters.get(key).getFieldName();
        	if(!Const.isEmpty(fieldName)){ 
        		int index = getInputRowMeta().indexOfValue(fieldName);
        		if(index > -1)
        			paramCopy.setValue(r[index].toString());  
        		else
        			throw new KettleException(Messages.getString("WPSClient.Log.FieldDoesNotExists1") + fieldName + Messages.getString("WPSClient.Log.FieldDoesNotExists2"));
        	}
        	inputParametersCopy.put(key, paramCopy);	     	
        }
        
        Object result = new ExecuteRequestBuilder(meta.getUrls(), meta.getMethod(), meta.getVersion(), meta.getProcessId(), inputParametersCopy, 
        		meta.getInputValues(), meta.getOutputParameters(), meta.getOutputValues(), meta.hasRawDataOutput()).sendRequest();
        
        if (result!=null){
        	int index = 0;
        	if(receiveInputRows){
				r = RowDataUtil.resizeArray(r, data.outputRowMeta.size());
				index = data.outputRowMeta.size() - 1;
        	}
        	r[index]=result;
			putRow(data.outputRowMeta, r); 			
        }
       
        if (checkFeedback(getLinesInput())) 
        	logBasic(Messages.getString("WPSClient.Log.LineNr")+getLinesInput());
         
        if(!receiveInputRows){
        	setOutputDone();
        	return false;
        }
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi){
		meta = (WPSClientMeta) smi;
		data = (WPSClientData) sdi;
		return super.init(smi, sdi)?true:false;
	}
		
	public void dispose(StepMetaInterface smi, StepDataInterface sdi){
	    meta = (WPSClientMeta)smi;
	    data = (WPSClientData)sdi;	    
	    super.dispose(smi, sdi);
	}

	public void run(){
		BaseStep.runStepThread(this, meta, data);
	}
}
