/**
 * 
 */
package org.pentaho.di.trans.steps.cswoutput;

import java.net.MalformedURLException;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResultInterface;

import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/**
 * @author LiberT
 *
 */
public class CSWOutputMeta extends BaseStepMeta implements StepMetaInterface {
	private CSWWriter CSWwriter;

	/**
	 * 
	 */
	public CSWOutputMeta() {
		
		super();
		CSWwriter= new CSWWriter();
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#check(java.util.List, org.pentaho.di.trans.TransMeta, org.pentaho.di.trans.step.StepMeta, org.pentaho.di.core.row.RowMetaInterface, java.lang.String[], java.lang.String[], org.pentaho.di.core.row.RowMetaInterface)
	 */
	@Override
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
			StepMeta stepMeta, RowMetaInterface prev, String[] input,
			String[] output, RowMetaInterface info) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#getStep(org.pentaho.di.trans.step.StepMeta, org.pentaho.di.trans.step.StepDataInterface, int, org.pentaho.di.trans.TransMeta, org.pentaho.di.trans.Trans)
	 */
	@Override
	public StepInterface getStep(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans) {
		// TODO Auto-generated method stub
		return new CSWOutput(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#getStepData()
	 */
	@Override
	public StepDataInterface getStepData() {
		// TODO Auto-generated method stub
		return new CSWOutputData();
	}
	
	public Object clone(){
		CSWOutputMeta retval = (CSWOutputMeta)super.clone();

		return retval;
	}	

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#loadXML(org.w3c.dom.Node, java.util.List, java.util.Map)
	 */
	@Override
	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);

	}
	
	private void readData(Node stepnode)throws KettleXMLException{
		
		try {
			CSWwriter.setCswUrl(XMLHandler.getTagValue(stepnode, "catalogurl")) ;
			CSWwriter.setLoginUrl(XMLHandler.getTagValue(stepnode, "loginurl"));
			CSWwriter.setUsername(XMLHandler.getTagValue(stepnode, "username"));
			CSWwriter.setPassword(XMLHandler.getTagValue(stepnode, "password"));
			CSWwriter.setSchema(XMLHandler.getTagValue(stepnode, "schema"));
		} catch (MalformedURLException e) {
			// 
			throw new KettleXMLException(e);
		} //$NON-NLS-1$ 
		
	}
	
	public String getXML(){
		StringBuffer retval = new StringBuffer();
		retval.append("    " + XMLHandler.addTagValue("catalogurl",   CSWwriter.getCswUrl().toString()));
		retval.append("    " + XMLHandler.addTagValue("loginurl",   CSWwriter.getLoginUrl().toString()));
		retval.append("    " + XMLHandler.addTagValue("username",   CSWwriter.getUsername()));
		retval.append("    " + XMLHandler.addTagValue("password",   CSWwriter.getPassword()));
		retval.append("    " + XMLHandler.addTagValue("schema",   CSWwriter.getSchema()));
				
		return retval.toString();
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#readRep(org.pentaho.di.repository.Repository, long, java.util.List, java.util.Map)
	 */
	@Override
	public void readRep(Repository rep, long idStep,
			List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#saveRep(org.pentaho.di.repository.Repository, long, long)
	 */
	@Override
	public void saveRep(Repository rep, long idTransformation, long idStep)
			throws KettleException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#setDefault()
	 */
	@Override
	public void setDefault() {
		// 
		try {
			CSWwriter.setCswUrl("http://sample-catalog-server.local");
			CSWwriter.setLoginUrl("http://login-server.loc");
			CSWwriter.setUsername(null);
			CSWwriter.setPassword(null);
			CSWwriter.setSchema(Messages.getString("CSWOutputDialog.Schema.CSWRECORD"));
		} catch (MalformedURLException e) {
			
		}

	}

	/**
	 * @return the cSWwriter
	 */
	public CSWWriter getCSWwriter() {
		return CSWwriter;
	}

	/**
	 * @param cSWwriter the cSWwriter to set
	 */
	public void setCSWwriter(CSWWriter cSWwriter) {
		CSWwriter = cSWwriter;
	}

}
