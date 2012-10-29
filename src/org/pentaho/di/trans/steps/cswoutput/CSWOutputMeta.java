/**
 * 
 */
package org.pentaho.di.trans.steps.cswoutput;

//import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;

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
 * @author mouattara,jmathieu,tbadard
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

		CSWwriter.setCswUrl(XMLHandler.getTagValue(stepnode, "catalogurl")) ;
		CSWwriter.setLoginUrl(XMLHandler.getTagValue(stepnode, "loginurl"));
		CSWwriter.setUsername(XMLHandler.getTagValue(stepnode, "username"));
		CSWwriter.setPassword(XMLHandler.getTagValue(stepnode, "password"));
		CSWwriter.setRequest(XMLHandler.getTagValue(stepnode, "request"));
		CSWwriter.setSchema(XMLHandler.getTagValue(stepnode, "schema"));
		CSWwriter.setUseLoginService("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "activatelogin")));
		
		Node mappingColumnsNode = XMLHandler.getSubNode(stepnode, "mappingcolumns");
		int nrMapCol = XMLHandler.countNodes(mappingColumnsNode, "mapcolumn");

		//
		ArrayList<String[]> mappingColumnList= new ArrayList<String[]>();


		for (int i = 0; i < nrMapCol; i++) {
			String[] s=new String[3];
			Node onode = XMLHandler.getSubNodeByNr(mappingColumnsNode, "mapcolumn", i);
			s[0]=XMLHandler.getTagValue(onode, "schemacolumn");
			s[1]=XMLHandler.getTagValue(onode, "previousstepcolumn");
			s[2]=XMLHandler.getTagValue(onode, "defaultvalue");
			mappingColumnList.add(s);

		}

		CSWwriter.setMappingColumns(mappingColumnList);


		//previous columns list
		Node prevcollistNode = XMLHandler.getSubNode(stepnode, "previouscolumnlist");
		int nrPrevCol = XMLHandler.countNodes(prevcollistNode, "previouscolumn");

		//
		String[] prevColumnList= new String[nrPrevCol];
		for (int i = 0; i < nrPrevCol; i++) {				
			Node onode = XMLHandler.getSubNodeByNr(prevcollistNode, "previouscolumn", i);
			String s=XMLHandler.getTagValue(onode, "id");				
			prevColumnList[i]=s;				
		}
		CSWwriter.setPrevColumnList(prevColumnList);

	}

	public String getXML(){
		StringBuffer retval = new StringBuffer();
		if (CSWwriter.getCswUrl()!=null)
			retval.append("    " + XMLHandler.addTagValue("catalogurl",   CSWwriter.getCswUrl().toString()));
		else retval.append("    " + XMLHandler.addTagValue("catalogurl",   ""));
		if (CSWwriter.getLoginUrl()!=null)
			retval.append("    " + XMLHandler.addTagValue("loginurl",   CSWwriter.getLoginUrl().toString()));
		else retval.append("    " + XMLHandler.addTagValue("loginurl",   ""));
		retval.append("    " + XMLHandler.addTagValue("username",   CSWwriter.getUsername()));
		retval.append("    " + XMLHandler.addTagValue("password",   CSWwriter.getPassword()));
		retval.append("    " + XMLHandler.addTagValue("schema",   CSWwriter.getSchema()));
		retval.append("    " + XMLHandler.addTagValue("request",   CSWwriter.getRequest()));
		retval.append("    " + XMLHandler.addTagValue("activatelogin", CSWwriter.isUseLoginService()));

		retval.append("    <mappingcolumns>").append(Const.CR);
		if (CSWwriter.getMappingColumns()!=null){
			for (String[] s:CSWwriter.getMappingColumns()) {
				retval.append("      <mapcolumn>").append(Const.CR);
				int j=0;
				String tagName=null;
				for (String c:s){
					if (j==0){
						tagName="schemacolumn";
					}else
						if (j==1){
							tagName="previousstepcolumn";
						}else
							if (j==2){
								tagName="defaultvalue";
							}
					retval.append("        ").append(XMLHandler.addTagValue(tagName, c));
					j++;
				}

				retval.append("      </mapcolumn>").append(Const.CR);
			}
		}
		retval.append("    </mappingcolumns>").append(Const.CR);

		retval.append("    <previouscolumnlist>").append(Const.CR);

		if (CSWwriter.getPrevColumnList()!=null){
			for(String s:CSWwriter.getPrevColumnList()){
				retval.append("      <previouscolumn>").append(Const.CR);
				retval.append("        ").append(XMLHandler.addTagValue("id", s));
				retval.append("      </previouscolumn>").append(Const.CR);
			}
		}
		retval.append("    </previouscolumnlist>").append(Const.CR);

		return retval.toString();
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#readRep(org.pentaho.di.repository.Repository, long, java.util.List, java.util.Map)
	 */
	@Override
	public void readRep(Repository rep, long idStep,
			List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException {

		CSWwriter.setCswUrl(rep.getStepAttributeString(idStep, "catalogurl")) ;
		CSWwriter.setLoginUrl(rep.getStepAttributeString(idStep, "loginurl")) ;
		CSWwriter.setUsername(rep.getStepAttributeString(idStep, "username")) ;
		CSWwriter.setPassword(rep.getStepAttributeString(idStep, "password")) ;
		CSWwriter.setRequest(rep.getStepAttributeString(idStep, "request")) ;
		CSWwriter.setSchema(rep.getStepAttributeString(idStep, "schema")) ;
		
		CSWwriter.setUseLoginService("Y".equalsIgnoreCase(rep.getStepAttributeString(idStep, "activatelogin")));

		int nrMapCol = rep.countNrStepAttributes(idStep, "mapcolumn");			
		ArrayList<String[]> mapColList= new ArrayList<String[]>();
		for (int i = 0; i < nrMapCol; i++) {
			String ch=rep.getStepAttributeString(idStep,i, "mapcolumn");
			String[] s=ch.split("@");
			mapColList.add(s);				
		}			
		CSWwriter.setMappingColumns(mapColList);

		int nrPrevColList=rep.countNrStepAttributes(idStep, "previouscolumn_item");
		String[] prevcol_list=new String[nrPrevColList];
		for (int i = 0; i < nrPrevColList; i++) {
			prevcol_list[i]=rep.getStepAttributeString(idStep,i, "previouscolumn_item");			
		}
		CSWwriter.setPrevColumnList(prevcol_list);//end prevcol_list

	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#saveRep(org.pentaho.di.repository.Repository, long, long)
	 */
	@Override
	public void saveRep(Repository rep, long idTransformation, long idStep)
	throws KettleException {
		if (CSWwriter.getCswUrl()!=null)
			rep.saveStepAttribute(idTransformation, idStep,"catalogurl",   CSWwriter.getCswUrl().toString());
		else rep.saveStepAttribute(idTransformation, idStep,"catalogurl",   "");
		if (CSWwriter.getLoginUrl()!=null)
			rep.saveStepAttribute(idTransformation, idStep,"loginurl",   CSWwriter.getLoginUrl().toString());
		else rep.saveStepAttribute(idTransformation, idStep,"loginurl",   "");
		rep.saveStepAttribute(idTransformation, idStep,"username",   CSWwriter.getUsername());
		rep.saveStepAttribute(idTransformation, idStep,"password",   CSWwriter.getPassword());
		rep.saveStepAttribute(idTransformation, idStep,"request",   CSWwriter.getRequest());
		rep.saveStepAttribute(idTransformation, idStep,"schema",   CSWwriter.getSchema());
		rep.saveStepAttribute(idTransformation, idStep,"activatelogin", CSWwriter.isUseLoginService());

		if (CSWwriter.getMappingColumns()!=null){
			int cpt=0;
			for (String[] s:CSWwriter.getMappingColumns()) {
				String temps="";
				int i=0;
				for (String ss:s){
					temps +=ss;
					if (i<2){
						temps +="@";
					}
					i++;
				}
				rep.saveStepAttribute(idTransformation, idStep,cpt,"mapcolumn", temps);
				cpt++;
			}
		}//end

		//previous columns list
		if (CSWwriter.getPrevColumnList()!=null){
			int cpt=0;
			for(String s:CSWwriter.getPrevColumnList()){
				rep.saveStepAttribute(idTransformation, idStep,cpt,"previouscolumn_item", s);
				cpt++;
			}
		}//previous columns list
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepMetaInterface#setDefault()
	 */
	@Override
	public void setDefault() {
		// 

		CSWwriter.setCswUrl("http://localhost:8080/geonetwork/srv/en/csw");
		CSWwriter.setLoginUrl("http://localhost:8080/geonetwork/srv/en/xml.user.login");
		CSWwriter.setUseLoginService(false);
		CSWwriter.setUsername(null);
		CSWwriter.setPassword(null);
		CSWwriter.setRequest(Messages.getString("CSWOutputDialog.Request.Insert"));
		CSWwriter.setSchema(Messages.getString("CSWOutputDialog.Schema.CSWRECORD"));
		CSWwriter.setMappingColumns(null);
		CSWwriter.setPrevColumnList(null);

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
