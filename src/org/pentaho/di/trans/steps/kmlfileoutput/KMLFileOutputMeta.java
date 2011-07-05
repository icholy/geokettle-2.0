package org.pentaho.di.trans.steps.kmlfileoutput;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
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
import org.pentaho.di.trans.steps.kmlfileinput.Messages;
import org.w3c.dom.Node;

public class KMLFileOutputMeta extends BaseStepMeta implements
		StepMetaInterface {
	private String fileName;
	private boolean isFileNameInField;
	private String fileNameField;
	private String featureNameField;
	private String featureDescField;
	private boolean exportingFeatureName;
	private boolean exportingFeatureDesc;
	private String acceptingStepName;
	private StepMeta acceptingStep;

	public KMLFileOutputMeta() {
		super(); // allocate BaseStepMeta
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileNameField() {
		return fileNameField;
	}

	public void setFileNameField(String fileNameField) {
		this.fileNameField = fileNameField;
	}

	public String getFeatureNameField() {
		return featureNameField;
	}

	public void setFeatureNameField(String featureNameField) {
		this.featureNameField = featureNameField;
	}

	public String getFeatureDescField() {
		return featureDescField;
	}

	public void setFeatureDescField(String featureDescField) {
		this.featureDescField = featureDescField;
	}

	public boolean isFileNameInField() {
		return isFileNameInField;
	}

	public void setFileNameInField(boolean isfileNameInField) {
		this.isFileNameInField = isfileNameInField;
	}

	public boolean isExportingFeatureName() {
		return exportingFeatureName;
	}

	public void setExportingFeatureName(boolean exportingFeatureName) {
		this.exportingFeatureName = exportingFeatureName;
	}

	public boolean isExportingFeatureDesc() {
		return exportingFeatureDesc;
	}

	public void setExportingFeatureDesc(boolean exportingFeatureDesc) {
		this.exportingFeatureDesc = exportingFeatureDesc;
	}

	public String getLookupStepname() {
		if (isFileNameInField && acceptingStep != null
				&& !Const.isEmpty(acceptingStep.getName()))
			return acceptingStep.getName();
		return null;
	}

	public Object clone() {
		KMLFileOutputMeta retval = (KMLFileOutputMeta) super.clone();
		return retval;
	}

	public void searchInfoAndTargetSteps(List<StepMeta> steps) {
		acceptingStep = StepMeta.findStep(steps, acceptingStepName);
	}

	public String[] getInfoSteps() {
		if (isFileNameInField && acceptingStep != null)
			return new String[] { acceptingStep.getName() };
		return super.getInfoSteps();
	}

	private void readData(Node stepnode) throws KettleXMLException {
		try {
			fileNameField = XMLHandler.getTagValue(stepnode, "filenamefield");
			isFileNameInField = "Y".equalsIgnoreCase(XMLHandler.getTagValue(
					stepnode, "isfilenameinfield"));
			fileName = XMLHandler.getTagValue(stepnode, "filename");
			exportingFeatureName = "Y".equalsIgnoreCase(XMLHandler.getTagValue(
					stepnode, "exportingfeaturename"));
			featureNameField = XMLHandler.getTagValue(stepnode,
					"featurenamefield");
			exportingFeatureDesc = "Y".equalsIgnoreCase(XMLHandler.getTagValue(
					stepnode, "exportingfeaturedesc"));
			featureDescField = XMLHandler.getTagValue(stepnode,
					"featuredescfield");
			acceptingStepName = XMLHandler.getTagValue(stepnode,
					"accept_stepname");
		} catch (Exception e) {
			throw new KettleXMLException(
					Messages.getString("KMLFileOutputMeta.Exception.UnableToReadStepInformationFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault() {
		fileName = null;
		fileNameField = null;
		featureNameField = null;
		featureDescField = null;
		isFileNameInField = false;
		exportingFeatureName = false;
		exportingFeatureDesc = false;
	}

	public String getXML() {
		StringBuffer retval = new StringBuffer();

		retval.append("    ").append(
				XMLHandler.addTagValue("filename", fileName));
		retval.append("    ").append(
				XMLHandler.addTagValue("isfilenameinfield", isFileNameInField));
		retval.append("    ").append(
				XMLHandler.addTagValue("featurenamefield", featureNameField));
		retval.append("    ").append(
				XMLHandler.addTagValue("featuredescfield", featureDescField));
		retval.append("    ").append(
				XMLHandler.addTagValue("filenamefield", fileNameField));
		retval.append("    ").append(
				XMLHandler.addTagValue("exportingfeaturename",
						exportingFeatureName));
		retval.append("    ").append(
				XMLHandler.addTagValue("exportingfeaturedesc",
						exportingFeatureDesc));
		retval.append("    ")
				.append(XMLHandler.addTagValue("accept_stepname",
						(acceptingStep != null ? acceptingStep.getName() : "")));
		return retval.toString();
	}

	public void readRep(Repository rep, long id_step,
			List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleException {
		try {
			fileName = rep.getStepAttributeString(id_step, "filename");
			isFileNameInField = rep.getStepAttributeBoolean(id_step,
					"isfilenameinfield");
			fileNameField = rep
					.getStepAttributeString(id_step, "filenamefield");
			featureNameField = rep.getStepAttributeString(id_step,
					"featurenamefield");
			featureDescField = rep.getStepAttributeString(id_step,
					"featuredescfield");
			exportingFeatureName = rep.getStepAttributeBoolean(id_step,
					"exportingfeaturename");
			exportingFeatureDesc = rep.getStepAttributeBoolean(id_step,
					"exportingfeaturedesc");
			acceptingStepName = rep.getStepAttributeString(id_step,
					"accept_stepname");
		} catch (Exception e) {
			throw new KettleException(
					Messages.getString("KMLFileOutputMeta.Exception.UnexpectedErrorReadingMetaDataFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
			throws KettleException {
		try {
			rep.saveStepAttribute(id_transformation, id_step, "filenamefield",
					fileNameField);
			rep.saveStepAttribute(id_transformation, id_step,
					"featurenamefield", featureNameField);
			rep.saveStepAttribute(id_transformation, id_step,
					"featuredescfield", featureDescField);
			rep.saveStepAttribute(id_transformation, id_step, "filename",
					fileName);
			rep.saveStepAttribute(id_transformation, id_step,
					"isfilenameinfield", isFileNameInField);
			rep.saveStepAttribute(id_transformation, id_step,
					"exportingfeaturename", exportingFeatureName);
			rep.saveStepAttribute(id_transformation, id_step,
					"exportingfeaturedesc", exportingFeatureDesc);
			rep.saveStepAttribute(id_transformation, id_step,
					"accept_stepname",
					(acceptingStep != null ? acceptingStep.getName() : ""));
		} catch (Exception e) {
			throw new KettleException(
					Messages.getString("KMLFileOutputMeta.Exception.UnableToSaveMetaDataToRepository") + id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
			StepMeta stepMeta, RowMetaInterface prev, String[] input,
			String[] output, RowMetaInterface info) {
		CheckResult cr;

		if (!isFileNameInField) {
			if (fileName == null) {
				cr = new CheckResult(
						CheckResult.TYPE_RESULT_ERROR,
						Messages.getString("KMLFileOutputMeta.Remark.PleaseSelectFileToUse"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
		} else if (fileNameField == null) {
			cr = new CheckResult(
					CheckResult.TYPE_RESULT_ERROR,
					Messages.getString("KMLFileOutputMeta.Remark.PleaseSelectFileField"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		} else {
			if (featureNameField == null) {
				cr = new CheckResult(
						CheckResult.TYPE_RESULT_ERROR,
						Messages.getString("KMLFileOutputMeta.Remark.PleaseSelectFeatureNameField"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
			if (featureDescField == null) {
				cr = new CheckResult(
						CheckResult.TYPE_RESULT_ERROR,
						Messages.getString("KMLFileOutputMeta.Remark.PleaseSelectFeatureDescField"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
			cr = new CheckResult(
					CheckResult.TYPE_RESULT_OK,
					Messages.getString("KMLFileOutputMeta.Remark.FileToUseIsSpecified"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
			if (input.length > 0) {
				cr = new CheckResult(
						CheckResult.TYPE_RESULT_OK,
						Messages.getString("KMLFileOutputMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			} else {
				cr = new CheckResult(
						CheckResult.TYPE_RESULT_ERROR,
						Messages.getString("KMLFileOutputMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
		}
	}

	public StepInterface getStep(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int cnr, TransMeta tr,
			Trans trans) {
		return new KMLFileOutput(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData() {
		return new KMLFileOutputData();
	}

	public void setAcceptingStep(StepMeta acceptingStep) {
		this.acceptingStep = acceptingStep;
	}

	public StepMeta getAcceptingStep() {
		return acceptingStep;
	}

	public void setAcceptingStepName(String acceptingStepName) {
		this.acceptingStepName = acceptingStepName;
	}

	public String getAcceptingStepName() {
		return acceptingStepName;
	}
}
