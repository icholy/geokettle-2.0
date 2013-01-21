package org.pentaho.di.trans.steps.ogrfileoutput;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.geospatial.OGRWriter;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
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
import org.pentaho.di.trans.steps.ogrfileinput.Messages;
import org.w3c.dom.Node;


public class OGRFileOutputMeta extends BaseStepMeta implements StepMetaInterface{
	private String 	source;
	private String layerName;
	private String ogrOutputFormat;
	private String ogrOptions;
	private int ogrWriteMode;
	private int ogrGeomType;
	private String ogrFIDField;
	private boolean preserveFIDField;
	//private String encoding;
	private boolean isSourceInField;
	private String  sourceField;
	private boolean isLayerNameInField;
	private String  layerNameField;
	private boolean isFileSource; //is the source a file?

	public OGRFileOutputMeta(){
		super(); // allocate BaseStepMeta
	}

	public void setIsFileSource(boolean isFileSource) {
		this.isFileSource = isFileSource;
	}

	public boolean isFileSource() {
		return isFileSource;
	}

	public String getSource(){
		return source;
	}

	public void setSource(String source){
		this.source = source;
	}

	public String getSourceField(){
        return sourceField;
    }
    
    public void setSourceField(String sourceField){
        this.sourceField = sourceField;
    }
    
    public boolean isSourceInField(){
        return isSourceInField;
    }
    
    public void setSourceInField(boolean isSourceInField){
        this.isSourceInField = isSourceInField;
    }
	
	public String getLayerName() {
		return layerName;
	}

	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

	public String getLayerNameField(){
        return layerNameField;
    }
    
    public void setLayerNameField(String layerNameField){
        this.layerNameField = layerNameField;
    }
    
    public boolean isLayerNameInField(){
        return isLayerNameInField;
    }
    
    public void setLayerNameInField(boolean isLayerNameInField){
        this.isLayerNameInField = isLayerNameInField;
    }
    
	/**
	 * @return Returns the ogrWriteMode.
	 */
	public int getOgrWriteMode() {
		return ogrWriteMode;
	}

	/**
	 * @param ogrWriteMode The ogrWriteMode to set.
	 */
	public void setOgrWriteMode(int ogrWriteMode) {
		this.ogrWriteMode = ogrWriteMode;
	}

	/**
	 * @return Returns the ogrFIDField.
	 */
	public String getOgrFIDField() {
		return ogrFIDField;
	}

	/**
	 * @param ogrFIDField The ogrFIDField to set.
	 */
	public void setOgrFIDField(String ogrFIDField) {
		this.ogrFIDField = ogrFIDField;
	}

	/**
	 * @return Returns the preserveFIDField.
	 */
	public boolean isPreserveFIDField() {
		return preserveFIDField;
	}

	/*public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}*/

	/**
	 * @param preserveFIDField The preserveFIDField to set.
	 */
	public void setPreserveFIDField(boolean preserveFIDField) {
		this.preserveFIDField = preserveFIDField;
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	public String[] getInfoSteps(){	
		return super.getInfoSteps();
	}
	
	public Object clone(){
		return (OGRFileOutputMeta)super.clone();
	}

	private void readData(Node stepnode)
	throws KettleXMLException{
		try{
			source        = XMLHandler.getTagValue(stepnode, "source"); //$NON-NLS-1$
			layerName          = XMLHandler.getTagValue(stepnode, "layer_name"); //$NON-NLS-1$
			ogrOutputFormat    = XMLHandler.getTagValue(stepnode, "file_format");
			ogrOptions         = XMLHandler.getTagValue(stepnode, "file_options");
			//encoding         = XMLHandler.getTagValue(stepnode, "encoding");
			if (XMLHandler.getTagValue(stepnode, "write_mode")!=null)
				ogrWriteMode = Integer.parseInt(XMLHandler.getTagValue(stepnode, "write_mode"));
			else ogrWriteMode = 0;
			if (XMLHandler.getTagValue(stepnode, "file_geomtype")!=null)
				ogrGeomType = Integer.parseInt(XMLHandler.getTagValue(stepnode, "file_geomtype"));
			else ogrGeomType = org.gdal.ogr.ogrConstants.wkbUnknown;
			ogrFIDField = XMLHandler.getTagValue(stepnode, "fid_field");
			//preserveFIDField = Boolean.parseBoolean(XMLHandler.getTagValue(stepnode, "preserve_fid_field"));
			if (XMLHandler.getTagValue(stepnode, "preserve_fid_field")!=null) {
				if (XMLHandler.getTagValue(stepnode, "preserve_fid_field").equals("Y"))
					preserveFIDField = true;
				else preserveFIDField = false;
			} else preserveFIDField = false;
			sourceField      = XMLHandler.getTagValue(stepnode, "sourcefield");
			isSourceInField  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "issourceinfield"));
			layerNameField      = XMLHandler.getTagValue(stepnode, "layernamefield");
			isLayerNameInField  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "islayernameinfield"));
			isFileSource  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "isfilesource"));
		}catch(Exception e){
			throw new KettleXMLException(Messages.getString("OGRFileOutputMeta.Exception.UnableToReadStepInformationFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault(){
		source      = null;
		layerName        = null;
		ogrOutputFormat  = "ESRI Shapefile";
		ogrWriteMode     = 0;
		ogrFIDField      = null;
		preserveFIDField = false;
		//encoding = null;
		sourceField = null;
		isSourceInField = false;
		layerNameField = null;
		isLayerNameInField = false;
	}
	
	public String getXML(){
		StringBuffer retval=new StringBuffer();

		retval.append("    " + XMLHandler.addTagValue("source",    source));
		retval.append("    " + XMLHandler.addTagValue("layer_name",    layerName));
		retval.append("    " + XMLHandler.addTagValue("file_format", ogrOutputFormat));
		retval.append("    " + XMLHandler.addTagValue("file_options", ogrOptions));
		retval.append("    " + XMLHandler.addTagValue("write_mode", ogrWriteMode));
		retval.append("    " + XMLHandler.addTagValue("file_geomtype", ogrGeomType));
		retval.append("    " + XMLHandler.addTagValue("fid_field", ogrFIDField));
		retval.append("    " + XMLHandler.addTagValue("preserve_fid_field", preserveFIDField));
		//retval.append("    " + XMLHandler.addTagValue("encoding", encoding));
		retval.append("    ").append(XMLHandler.addTagValue("issourceinfield", isSourceInField));
		retval.append("    ").append(XMLHandler.addTagValue("sourcefield", sourceField)); 
		retval.append("    ").append(XMLHandler.addTagValue("islayernameinfield", isLayerNameInField));
		retval.append("    ").append(XMLHandler.addTagValue("layernamefield", layerNameField)); 
		retval.append("    ").append(XMLHandler.addTagValue("isfilesource", isFileSource));
		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException{
		try{
			source = rep.getStepAttributeString (id_step, "source"); //$NON-NLS-1$
			layerName = rep.getStepAttributeString (id_step, "layer_name");
			ogrOutputFormat = rep.getStepAttributeString (id_step, "file_format");
			ogrOptions = rep.getStepAttributeString (id_step, "file_options");
			//encoding = rep.getStepAttributeString (id_step, "encoding");
			if (rep.getStepAttributeString(id_step, "write_mode")!=null)
				ogrWriteMode = Integer.parseInt(rep.getStepAttributeString(id_step, "write_mode"));
			else ogrWriteMode = 0;
			if (rep.getStepAttributeString(id_step, "file_geomtype")!=null)
				ogrGeomType = Integer.parseInt(rep.getStepAttributeString(id_step, "file_geomtype"));
			else ogrGeomType = org.gdal.ogr.ogrConstants.wkbUnknown;
			ogrFIDField = rep.getStepAttributeString (id_step, "fid_field");
			if (rep.getStepAttributeString(id_step, "preserve_fid_field")!=null) {
				//preserveFIDField = Boolean.parseBoolean(rep.getStepAttributeString(id_step, "preserve_fid_field"));
				if (rep.getStepAttributeString(id_step, "preserve_fid_field").equals("Y"))
					preserveFIDField = true;
				else preserveFIDField = false;
			}
			else preserveFIDField = false;
			isSourceInField  = rep.getStepAttributeBoolean(id_step, "issourceinfield");	
			sourceField      = rep.getStepAttributeString (id_step, "sourcefield");
			isLayerNameInField  = rep.getStepAttributeBoolean(id_step, "islayernameinfield");	
			layerNameField      = rep.getStepAttributeString (id_step, "layernamefield");
			isFileSource  = rep.getStepAttributeBoolean(id_step, "isfilesource");	
		}catch(Exception e){
			throw new KettleException(Messages.getString("OGRFileOutputMeta.Exception.UnexpectedErrorReadingMetaDataFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
	throws KettleException{
		try{
			rep.saveStepAttribute(id_transformation, id_step, "source", source); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "layer_name", layerName);
			rep.saveStepAttribute(id_transformation, id_step, "file_format", ogrOutputFormat);
			rep.saveStepAttribute(id_transformation, id_step, "file_options", ogrOptions);
			rep.saveStepAttribute(id_transformation, id_step, "write_mode", ogrWriteMode);
			rep.saveStepAttribute(id_transformation, id_step, "file_geomtype", ogrGeomType);
			rep.saveStepAttribute(id_transformation, id_step, "fid_field", ogrFIDField);
			rep.saveStepAttribute(id_transformation, id_step, "preserve_fid_field", preserveFIDField);
			//rep.saveStepAttribute(id_transformation, id_step, "encoding", encoding);
			rep.saveStepAttribute(id_transformation, id_step, "sourcefield", sourceField);
			rep.saveStepAttribute(id_transformation, id_step, "issourceinfield", isSourceInField);
			rep.saveStepAttribute(id_transformation, id_step, "layernamefield", layerNameField);
			rep.saveStepAttribute(id_transformation, id_step, "islayernameinfield", isLayerNameInField);
			rep.saveStepAttribute(id_transformation, id_step, "isfilesource", isFileSource);
		}catch(Exception e){
			throw new KettleException(Messages.getString("OGRFileOutputMeta.Exception.UnableToSaveMetaDataToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void getFields(RowMetaInterface  r, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space){
		if (r==null) 
			r=new RowMeta(); 
		// give back values
	}
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
			String[] Output, String[] output, RowMetaInterface info){
		CheckResult cr;

		if (!isSourceInField && source==null){
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("OGRFileOutputMeta.Remark.PleaseSelectFileToUse"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}else{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("OGRFileOutputMeta.Remark.FileToUseIsSpecified"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);

			OGRWriter ogrWriter = null;

			try{			
				if (source!=null && !(source.trim().equals(""))) {
					if(isFileSource){
						//ogrWriter = new OGRWriter(source,true,ogrOutputFormat,ogrOptions, ogrGeomType, layerName, ogrWriteMode, ogrFIDField, preserveFIDField, encoding);
						ogrWriter = new OGRWriter(source, true, ogrOutputFormat,ogrOptions, ogrGeomType, layerName, ogrWriteMode, ogrFIDField, preserveFIDField);
					}else{
						//ogrWriter = new OGRWriter(source,false,ogrOutputFormat,ogrOptions, ogrGeomType, layerName, ogrWriteMode, ogrFIDField, preserveFIDField, encoding);
						ogrWriter = new OGRWriter(source, false, ogrOutputFormat,ogrOptions, ogrGeomType, layerName, ogrWriteMode, ogrFIDField, preserveFIDField);					
					}
					ogrWriter.open();
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("OGRFileOutputMeta.Remark.FileExistsAndCanBeOpened"), stepMeta); //$NON-NLS-1$
					remarks.add(cr);
				}		
			}catch(KettleException ke){
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("OGRFileOutputMeta.Remark.NoFieldsCouldBeFoundInFileBecauseOfError")+Const.CR+ke.getMessage(), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}finally{
				if (ogrWriter != null) ogrWriter.close();
			}
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans){
		return new OGRFileOutput(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData(){
		return new OGRFileOutputData();
	}    

	public FileInputList getFileList(VariableSpace space){
        return FileInputList.createFileList(space, new String[] { null }, new String[] { null }, new String[] { "N" });
    }

	public String getOgrOutputFormat() {
		return ogrOutputFormat;
	}

	public void setOgrOutputFormat(String ogrOutputFormat) {
		this.ogrOutputFormat = ogrOutputFormat;
	}

	public String getOgrOptions() {
		return ogrOptions;
	}

	public void setOgrOptions(String ogrOptions) {
		this.ogrOptions = ogrOptions;
	}

	public int getOgrGeomType() {
		return ogrGeomType;
	}

	public void setOgrGeomType(int ogrGeomType) {
		this.ogrGeomType = ogrGeomType;
	}

}
