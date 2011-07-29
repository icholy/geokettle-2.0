/**
 * 
 */
package org.pentaho.di.trans.steps.cswoutput;




import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


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
	private ArrayList<String[]> mappingColumns;
	
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
		
		mappingColumns=meta.getCSWwriter().getMappingColumns();
		
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
			while (i<mappingColumns.size()){
			//while(i<r.length){		
							
				try {
					
		
					String valueToSet=findValueToSet(fieldName,r, mappingColumns.get(i));
					query=meta.getCSWwriter().setElementTextUsingQueryString(query,mappingColumns.get(i)[0],valueToSet);
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
	
	private String findValueToSet(String[] columList,Object[] o, String[] mapcols){
		boolean trouve=false;
		String obj=mapcols[2];
		int i=0;
		while (i<columList.length && !trouve){
			String col=columList[i];
			if (col.equalsIgnoreCase(mapcols[1])){
				trouve=true;
				if (col.equalsIgnoreCase("boundingbox_lowercorner")||col.equalsIgnoreCase("boundingbox_uppercorner")){
					if (o[i]!=null){
						Point point=(Point)o[i];						
						obj=point.getX()+" "+point.getY();
					}
					
				}else
				obj=(String)o[i];
			}else
			i++;
		}
		return obj;
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
			logBasic(Messages.getString("CSWOutput.Log.StartingToRun"));		 //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data) );
		}
		catch(Exception e){
			logError(Messages.getString("CSWOutput.Log.Error.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
