/**
 * 
 */
package org.pentaho.di.trans.steps.cswinput;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.jdom.Element;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.sosinput.Messages;


import org.w3c.dom.Node;

/**
 * @author O.Mamadou
 *
 */
public class CSWInputMeta extends BaseStepMeta implements StepMetaInterface {
	
	
	private CSWReader cswParam;
	private RowMetaInterface fieds;
	//private String keyword;
	
	/**
	 * @return the keyword
	 
	public String getKeyword() {
		return keyword;
	}*/

	/**
	 * @param keyword the keyword to set
	 
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}*/

	/**
	 * 
	 */
	public CSWInputMeta() {
		// TODO Auto-generated constructor stub
		super();
		cswParam= new CSWReader();
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
		return new CSWInput(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		//return null;
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#getStepData()
	 */
	@Override
	public StepDataInterface getStepData() {
		// TODO Auto-generated method stub
		 return new CSWInputData();
	}
	
	public Object clone(){
		CSWInputMeta retval = (CSWInputMeta)super.clone();

		return retval;
	}	
	
	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#loadXML(org.w3c.dom.Node, java.util.List, java.util.Map)
	 */
	@Override
	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			Map<String, Counter> counters) throws KettleXMLException {
		// TODO Auto-generated method stub
		readData(stepnode);

	}
	
	private void readData(Node stepnode)throws KettleXMLException{
		try{
			cswParam.setCatalogUrl(XMLHandler.getTagValue(stepnode, "url")) ; //$NON-NLS-1$ 
			cswParam.setMethod(XMLHandler.getTagValue(stepnode, "method"));
			cswParam.setVersion(XMLHandler.getTagValue(stepnode, "cswVersion")) ;
			cswParam.setKeyword(XMLHandler.getTagValue(stepnode, "keyword"));
			cswParam.setElementSet(XMLHandler.getTagValue(stepnode, "elementset"));
			cswParam.setStartDate(XMLHandler.getTagValue(stepnode, "startdate"));
			cswParam.setEndDate(XMLHandler.getTagValue(stepnode, "enddate"));
			cswParam.setSimpleSearch("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "simplesearch")));
			
			cswParam.setUsername(XMLHandler.getTagValue(stepnode, "username"));
			cswParam.setPassword(XMLHandler.getTagValue(stepnode, "password"));
			cswParam.setLoginServiceUrl(XMLHandler.getTagValue(stepnode, "loginurl"));
			cswParam.setOutputSchema(XMLHandler.getTagValue(stepnode, "outputschema"));
			cswParam.setTitle(XMLHandler.getTagValue(stepnode, "title"));
			
			cswParam.setConstraintLanguage(XMLHandler.getTagValue(stepnode, "constraintlanguage"));
			
			
			try{
				cswParam.setStartPosition(Integer.parseInt(XMLHandler.getTagValue(stepnode, "startposition")));
			}catch(NumberFormatException e){
				cswParam.setStartPosition(0);
				//e.printStackTrace();
			}
			try{
				cswParam.setMaxRecords(Integer.parseInt(XMLHandler.getTagValue(stepnode, "maxrecords")));
			}catch(NumberFormatException e){
				cswParam.setMaxRecords(10);
				//e.printStackTrace();
			}
			
			
			Iterator<String> it= cswParam.getBBOX().keySet().iterator();
			HashMap<String, Double> bbox=new HashMap<String, Double>();
			while (it.hasNext()){
				String courant=it.next();
				String chaine=XMLHandler.getTagValue(stepnode, "BBOX_"+courant);
				//Double.parseDouble(chaine);
				bbox.put(courant, Double.parseDouble(chaine));
				
			}
			cswParam.setBBOX(bbox);
		}
		catch(Exception e){
			throw new KettleXMLException(Messages.getString("CSWInputMeta.Exception.UnableToReadStepInformationFromXML"), e); //$NON-NLS-1$
		}
	}

	
	public String getXML(){
		StringBuffer retval = new StringBuffer();
		retval.append("    " + XMLHandler.addTagValue("url",   cswParam.getCatalogUrl().toString())); //$NON-NLS-1$ //$NON-NLS-2$		
		retval.append("    " + XMLHandler.addTagValue("method", cswParam.getMethod())); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("cswVersion", cswParam.getVersion())); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("keyword", cswParam.getKeyword())); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("elementset", cswParam.getElementSet())); //$NON-NLS-1$ //$NON-NLS-2$
		
		retval.append("    " + XMLHandler.addTagValue("startdate", cswParam.getStartDate())); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("enddate", cswParam.getEndDate())); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("simplesearch", cswParam.isSimpleSearch())); //$NON-NLS-1$ //$NON-NLS-2$
		
		retval.append("    " + XMLHandler.addTagValue("title", cswParam.getTitle()));
		retval.append("    " + XMLHandler.addTagValue("username", cswParam.getUsername())); 
		retval.append("    " + XMLHandler.addTagValue("password", cswParam.getPassword()));
		retval.append("    " + XMLHandler.addTagValue("loginurl", cswParam.getLoginServiceUrl()));
		retval.append("    " + XMLHandler.addTagValue("outputschema", cswParam.getOutputSchema()));
		retval.append("    " + XMLHandler.addTagValue("constraintlanguage", cswParam.getConstraintLanguage()));
		retval.append("    " + XMLHandler.addTagValue("startposition", cswParam.getStartPosition()));
		retval.append("    " + XMLHandler.addTagValue("maxrecords", cswParam.getMaxRecords()));
		
				
			Iterator<String> it=cswParam.getBBOX().keySet().iterator();
			while(it.hasNext()) {
				String bbName=it.next();				
				retval.append("    " +XMLHandler.addTagValue("BBOX_"+bbName, cswParam.getBBOX().get(bbName)));
				
			}
        
		//retval.append("    </BBOX>").append(Const.CR);		
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
		// TODO Auto-generated method stub
		cswParam.setKeyword(null);
		cswParam.setVersion(null);
		cswParam.setMethod(null);
		cswParam.setStartDate(null);
		cswParam.setEndDate(null);
		try {
			cswParam.setCatalogUrl("http://localhost/CSW");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HashMap<String,Double> bbox=new HashMap<String, Double>();
		bbox.put("NORTH", new Double(0));
		bbox.put("SOUTH", new Double(0));
		bbox.put("EAST", new Double(0));
		bbox.put("WEST", new Double(0));
		cswParam.setBBOX(bbox);

	}

	/**
	 * @param cswParam the cswParam to set
	 */
	public void setCswParam(CSWReader cswParam) {
		this.cswParam = cswParam;
	}

	/**
	 * @return the cswParam
	 */
	public CSWReader getCswParam() {
		return cswParam;
	}
	
	public void getFields(RowMetaInterface row, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space){
    		
			try {
				String pattern=null;
				if (cswParam.getElementSet().equalsIgnoreCase("brief")){
					pattern="csw:BriefRecord";
				}else
				if (cswParam.getElementSet().equalsIgnoreCase("summary")){
					pattern="csw:SummaryRecord";
				}else
				if(cswParam.getElementSet().equalsIgnoreCase("full")){
					pattern="csw:Record";
				}
				
				//String str=this.cswParam.GetRecords();
				//Element rootElement=cswParam.fromStringToJDOMDocument(str).getRootElement();
				Element el=this.cswParam.findSubElement(cswParam.fromStringToJDOMDocument(cswParam.GetRecords()).getRootElement(),pattern);
				Iterator<Element> it=cswParam.getColumns(el).iterator();
				while (it.hasNext()){
					Element c=it.next();					
					row.addValueMeta(new ValueMeta(c.getName(), ValueMetaInterface.TYPE_STRING));
				}				
				//
				fieds=row;
				
				
			} catch (KettleException e) {
				
				// TODO Auto-generated catch block
				//e.printStackTrace();
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
    	
	}

	/**
	 * @param fieds the fieds to set
	 */
	public void setMetaInterfaceFieds(RowMetaInterface fieds) {
		this.fieds = fieds;
	}

	/**
	 * @return the fieds
	 */
	public RowMetaInterface getMetaInterfaceFieds() {
		return fieds;
	}

}