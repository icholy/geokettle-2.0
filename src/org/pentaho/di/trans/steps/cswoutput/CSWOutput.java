/**
 * 
 */
package org.pentaho.di.trans.steps.cswoutput;




import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import javax.servlet.ServletException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
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
 * @author mouattara,jmathieu,tbadard
 *
 */
public class CSWOutput extends BaseStep implements StepInterface {
	public static final String CSWEXCEPTIONREPORT="Exception";
	private static final String REQ_INSERT = "INSERT";
	private static final String REQ_DELETE = "DELETE";
	private static final String REQ_UPDATE = "UPDATE";
	private static final String CSW_NAMESPACE = "http://www.opengis.net/cat/csw/2.0.2";
	private static final String TOTAL_INSERTED_TAG = "totalInserted";

	
	private CSWOutputMeta meta;
	private CSWOutputData data;
	private boolean isReceivingInputFields=false;
	private String allQuery="";
	private ArrayList<String[]> mappingColumns;

	public CSWOutput(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
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
				response = meta.getCSWwriter().execute(allQuery);
				boolean trouve=parseCSWServerResponse(response, CSWEXCEPTIONREPORT) ;
				if (trouve==true){
					throw new KettleException(response);
				}
				logBasic(response);
			} catch (UnsupportedEncodingException e) {
				logError("Error closing file.", e);
			}
			
			return false;			
		}
		
		if (first){ 
            first = false;                     		                          
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
		}else{
			data.outputRowMeta=getInputRowMeta().clone();
			
			fieldName=data.outputRowMeta.getFieldNames();
			while (i<mappingColumns.size()){
			try {
					String valueToSet=findValueToSet(fieldName,r, mappingColumns.get(i));
					query=meta.getCSWwriter().setElementTextUsingQueryString(query,mappingColumns.get(i)[0],valueToSet);
					i++;					
				} catch (ServletException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}		
				
			}
			allQuery +=query;			
			incrementLinesOutput();
		}     
		return true;
	}
	
	/**
	 * This method parse CSW server response to check out if transactions have been successfully completed
	 * */
	public boolean parseCSWServerResponse(String resp, String SearchElementName) throws KettleException{
		Document doc= meta.getCSWwriter().fromStringToJDOMDocument(resp);
		Element rootElement=doc.getRootElement();
		boolean trouve=false;
		String request=meta.getCSWwriter().getRequest();

		List<?> list=rootElement.getChildren();
		Iterator<?> it=list.iterator();
		while (it.hasNext()){
			Element courant=(Element)it.next();
			String cName=courant.getName();

			if (request.equalsIgnoreCase(REQ_INSERT)) {
				if (cName.equalsIgnoreCase(SearchElementName)){
					trouve=true;
					super.setLinesWritten(0);
				}else{
					Element summary=courant.getChild(TOTAL_INSERTED_TAG,Namespace.getNamespace(CSW_NAMESPACE));
					if (summary !=null)
						super.setLinesWritten(Long.parseLong(summary.getText()));	
				}
			}else
			if (request.equalsIgnoreCase(REQ_DELETE)){
				if (cName.equalsIgnoreCase(SearchElementName)){
				}else{
					super.incrementLinesRejected();
				}
				
			}
			else
			if (request.equalsIgnoreCase(REQ_UPDATE)){
				if (cName.equalsIgnoreCase(SearchElementName)){
				}else	
				super.incrementLinesUpdated();
			}
		}	
		
		return trouve;
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
					obj= (o[i]!=null) ?o[i].toString():null;
				//obj=(String)o[i];
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
			logBasic(Messages.getString("CSWOutput.Log.StartingToRun"));
			while (!isStopped() && processRow(meta, data) );
		}
		catch(Exception e){
			logError(Messages.getString("CSWOutput.Log.Error.UnexpectedError")+" : "+e.toString());
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
