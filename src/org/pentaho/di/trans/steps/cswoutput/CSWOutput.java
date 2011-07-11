/**
 * 
 */
package org.pentaho.di.trans.steps.cswoutput;




import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import org.pentaho.di.trans.steps.cswoutput.Messages;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.vividsolutions.jts.geom.Point;

/**
 * @author O.Mamadou
 *
 */
public class CSWOutput extends BaseStep implements StepInterface {
	private CSWOutputMeta meta;
	private CSWOutputData data;
	private boolean isReceivingInputFields=false;
	private String allQuery="";
	
	/**
	 * @param stepMeta
	 * @param stepDataInterface
	 * @param copyNr
	 * @param transMeta
	 * @param trans
	 */
	public CSWOutput(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		// 
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException{
		String[] fieldName=null;
		meta=(CSWOutputMeta)smi;
		data=(CSWOutputData)sdi;
		Object [] r = getRow();
		
		if (isReceivingInputFields && r==null){
			setOutputDone();
			
			String response;
			try {
				response = meta.getCSWwriter().cswINSERTTransaction(allQuery);
				logBasic(response);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return false;			
		}
		
		if (first){ 
        	// we just got started
            first = false;          
                       		                          
        }
		//
		if (r!=null){
			
		}
		
		
		int i=0;
		String query=null;
		
		if (meta.getCSWwriter()!=null){
			CSWWriter writer=meta.getCSWwriter();
			System.out.println(Messages.getString("CSWOutputDialog.Schema.CSWRECORD"));
			if (writer.getSchema().equalsIgnoreCase(Messages.getString("CSWOutputDialog.Schema.CSWRECORD"))){
				query=CSWWriter.CSWBRIEF_XML;
			}else{
				query=CSWWriter.MD_METADATA_XML;
			}
		}
		
		if (r==null){
			
			isReceivingInputFields=true;
			//return false;
			
		}else{
			data.outputRowMeta=getInputRowMeta().clone();
			fieldName=data.outputRowMeta.getFieldNames();
			while(i<r.length){		
				String valueToSet=null;			
				try {
					String fieldname=fieldName[i];
					if (fieldname.equalsIgnoreCase("boundingbox_lowercorner")||fieldname.equalsIgnoreCase("boundingbox_uppercorner")){
						Point point=(Point)r[i];						
						valueToSet=point.getX()+" "+point.getY();
					}else{
						valueToSet=(String)r[i];
					}
					query=meta.getCSWwriter().setElementTextUsingQueryString(query,fieldname,valueToSet);
					i++;
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
				
			}
			allQuery +=query;
		}
		
		
		

		/*if(!isReceivingInputFields){
        	setOutputDone();
			return false;	
        }   */        
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi){
		meta = (CSWOutputMeta) smi;
		data = (CSWOutputData) sdi;

		if (super.init(smi, sdi)){
			 
			return true;
		}
		return false;
	}
	//
	
	public void run(){
		try{
			logBasic(Messages.getString("Input.Log.StartingToRun"));		 //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data) );
		}
		catch(Exception e){
			logError(Messages.getString("CSWInput.Log.Error.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
