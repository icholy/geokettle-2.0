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
import org.pentaho.di.core.geospatial.OGRReader;
import org.pentaho.di.core.geospatial.OGRWriter;
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
import org.pentaho.di.trans.steps.ogrfileoutput.Messages;
import org.w3c.dom.Node;


public class OGRFileOutputMeta extends BaseStepMeta implements StepMetaInterface
{
	private String gisFileName;
	private String connectionString;
	private String layerName;
	private String ogrOutputFormat;
	private String ogrOptions;
	private int ogrWriteMode;
	private int ogrGeomType;
	private String ogrFIDField;
	private boolean preserveFIDField;

	public OGRFileOutputMeta()
	{
		super(); // allocate BaseStepMeta
	}

	/**
	 * @return Returns the connectionString.
	 */
	public String getConnectionString() {
		return connectionString;
	}

	/**
	 * @param connectionString The connectionString to set.
	 */
	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}

	/**
	 * @return Returns the layerName.
	 */
	public String getLayerName() {
		return layerName;
	}

	/**
	 * @param layerName The layerName to set.
	 */
	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

	/**
	 * @return Returns the gisFileName.
	 */
	public String getGisFileName()
	{
		return gisFileName;
	}

	/**
	 * @param gisFileName The gisFileName to set.
	 */
	public void setGisFileName(String gisFileName)
	{
		this.gisFileName = gisFileName;
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

	/**
	 * @param preserveFIDField The preserveFIDField to set.
	 */
	public void setPreserveFIDField(boolean preserveFIDField) {
		this.preserveFIDField = preserveFIDField;
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	public Object clone()
	{
		OGRFileOutputMeta retval = (OGRFileOutputMeta)super.clone();
		return retval;
	}

	private void readData(Node stepnode)
	throws KettleXMLException
	{
		try
		{
			gisFileName        = XMLHandler.getTagValue(stepnode, "file_gis"); //$NON-NLS-1$
			connectionString   = XMLHandler.getTagValue(stepnode, "connection_string"); //$NON-NLS-1$
			layerName          = XMLHandler.getTagValue(stepnode, "layer_name"); //$NON-NLS-1$
			ogrOutputFormat    = XMLHandler.getTagValue(stepnode, "file_format");
			ogrOptions         = XMLHandler.getTagValue(stepnode, "file_options");
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

		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("OGRFileOutputMeta.Exception.UnableToReadStepInformationFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		gisFileName      = null;
		connectionString = null;
		layerName        = null;
		ogrOutputFormat  = "ESRI Shapefile";
		ogrWriteMode     = 0;
		ogrFIDField      = null;
		preserveFIDField = false;
	}

	public String getXML()
	{
		StringBuffer retval=new StringBuffer();

		retval.append("    " + XMLHandler.addTagValue("file_gis",    gisFileName));
		retval.append("    " + XMLHandler.addTagValue("connection_string",    connectionString));
		retval.append("    " + XMLHandler.addTagValue("layer_name",    layerName));
		retval.append("    " + XMLHandler.addTagValue("file_format", ogrOutputFormat));
		retval.append("    " + XMLHandler.addTagValue("file_options", ogrOptions));
		retval.append("    " + XMLHandler.addTagValue("write_mode", ogrWriteMode));
		retval.append("    " + XMLHandler.addTagValue("file_geomtype", ogrGeomType));
		retval.append("    " + XMLHandler.addTagValue("fid_field", ogrFIDField));
		retval.append("    " + XMLHandler.addTagValue("preserve_fid_field", preserveFIDField));

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
		try
		{
			gisFileName = rep.getStepAttributeString (id_step, "file_gis"); //$NON-NLS-1$
			connectionString = rep.getStepAttributeString (id_step, "connection_string"); //$NON-NLS-1$
			layerName = rep.getStepAttributeString (id_step, "layer_name");
			ogrOutputFormat = rep.getStepAttributeString (id_step, "file_format");
			ogrOptions = rep.getStepAttributeString (id_step, "file_options");
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

		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("OGRFileOutputMeta.Exception.UnexpectedErrorReadingMetaDataFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
	throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "file_gis", gisFileName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "connection_string", connectionString);
			rep.saveStepAttribute(id_transformation, id_step, "layer_name", layerName);
			rep.saveStepAttribute(id_transformation, id_step, "file_format", ogrOutputFormat);
			rep.saveStepAttribute(id_transformation, id_step, "file_options", ogrOptions);
			rep.saveStepAttribute(id_transformation, id_step, "write_mode", ogrWriteMode);
			rep.saveStepAttribute(id_transformation, id_step, "file_geomtype", ogrGeomType);
			rep.saveStepAttribute(id_transformation, id_step, "fid_field", ogrFIDField);
			rep.saveStepAttribute(id_transformation, id_step, "preserve_fid_field", preserveFIDField);

		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("OGRFileOutputMeta.Exception.UnableToSaveMetaDataToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
			String[] Output, String[] output, RowMetaInterface info)
	{
		CheckResult cr;

		if (gisFileName==null && (connectionString==null || connectionString.trim().equals("")))
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("OGRFileOutputMeta.Remark.PleaseSelectFileToUse"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);

		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("OGRFileOutputMeta.Remark.FileToUseIsSpecified"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);

			OGRWriter ogrWriter = null;
			try
			{
				if (gisFileName!=null) {
					ogrWriter = new OGRWriter(gisFileName,true,ogrOutputFormat,ogrOptions, ogrGeomType, layerName, ogrWriteMode, ogrFIDField, preserveFIDField);
				}

				if (connectionString!=null && !(connectionString.trim().equals(""))) {
					ogrWriter = new OGRWriter(connectionString,false,ogrOutputFormat,ogrOptions, ogrGeomType, layerName, ogrWriteMode, ogrFIDField, preserveFIDField);
				}

				ogrWriter.open();
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("OGRFileOutputMeta.Remark.FileExistsAndCanBeOpened"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);

			}
			catch(KettleException ke)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("OGRFileOutputMeta.Remark.NoFieldsCouldBeFoundInFileBecauseOfError")+Const.CR+ke.getMessage(), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
			finally
			{
				if (ogrWriter != null) ogrWriter.close();
			}
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new OGRFileOutput(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new OGRFileOutputData();
	}    

	public FileInputList getTextFileList(VariableSpace space)
	{
		return FileInputList.createFileList(space, new String[] { gisFileName }, new String[] { null }, new String[] { "Y" });
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
