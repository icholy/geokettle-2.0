package org.pentaho.di.trans.steps.wfsinput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStoreFinder;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
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
import org.w3c.dom.Node;

public class WFSInputMeta extends BaseStepMeta implements StepMetaInterface {
	private static final String YES = "Y";

	private String[] attributes;
	private String capabilitiesUrl;
	private String username;
	private String password;
	private String layername;
	private String maxFeatures;
	private String srs;

	private boolean method;

	private List<Filter> filters;

	public String[] getAttributes() {
		return attributes;
	}

	public void setAttributes(String[] attributes) {
		this.attributes = attributes;
	}

	public String getCapabilitiesUrl() {
		return capabilitiesUrl;
	}

	public void setCapabilitiesUrl(String capabilitiesUrl) {
		this.capabilitiesUrl = capabilitiesUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLayername() {
		return layername;
	}

	public void setLayername(String layername) {
		this.layername = layername;
	}

	public String getMaxFeatures() {
		return maxFeatures;
	}

	public void setMaxFeatures(String maxFeatures) {
		this.maxFeatures = maxFeatures;
	}

	public String getSrs() {
		return srs;
	}

	public void setSrs(String srs) {
		this.srs = srs;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	public void setMethod(boolean method) {
		this.method = method;
	}

	public boolean getMethod() {
		return method;
	}

	@Override
	public void setDefault() {
		capabilitiesUrl = null;
		username = null;
		password = null;
		method = false;
		layername = null;
		maxFeatures = null;
		srs = null;
		filters = null;
		attributes = null;
	}

	public Object clone() {
		return (WFSInputMeta) super.clone();
	}

	public void allocateFilters(int nrFilters) {
		filters = new ArrayList<Filter>(nrFilters);
	}

	public void allocateAttributes(int nrAttributes) {
		attributes = new String[nrAttributes];
	}

	@Override
	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	private void readData(Node stepnode) throws KettleXMLException {
		capabilitiesUrl = XMLHandler.getTagValue(stepnode, "capabilitiesUrl");
		username = XMLHandler.getTagValue(stepnode, "username");
		password = XMLHandler.getTagValue(stepnode, "password");
		method = YES.equalsIgnoreCase(XMLHandler
				.getTagValue(stepnode, "method"));
		layername = XMLHandler.getTagValue(stepnode, "layername");
		maxFeatures = XMLHandler.getTagValue(stepnode, "maxFeatures");
		srs = XMLHandler.getTagValue(stepnode, "srs");

		Node filtersNode = XMLHandler.getSubNode(stepnode, "filters");
		int nrFilters = XMLHandler.countNodes(filtersNode, "filter");

		allocateFilters(nrFilters);

		for (int i = 0; i < nrFilters; i++) {
			Node n = XMLHandler.getSubNodeByNr(filtersNode, "filter", i);
			filters.add(new Filter(XMLHandler.getTagValue(n, "attribute"),
					XMLHandler.getTagValue(n, "condition"), XMLHandler
							.getTagValue(n, "value"), XMLHandler.getTagValue(n,
							"operator")));
		}
		Node attributesNode = XMLHandler.getSubNode(stepnode, "attributes");
		int nrAttributes = XMLHandler.countNodes(attributesNode, "attribute");
		allocateAttributes(nrAttributes);
		for (int i = 0; i < nrAttributes; i++) {
			attributes[i] = XMLHandler.getTagValue(
					XMLHandler.getSubNodeByNr(attributesNode, "attribute", i),
					"name");
		}
	}

	public String getXML() {
		StringBuffer retval = new StringBuffer();
		retval.append("	"
				+ XMLHandler.addTagValue("capabilitiesUrl", capabilitiesUrl));
		retval.append("	" + XMLHandler.addTagValue("username", username));
		retval.append("	" + XMLHandler.addTagValue("password", password));
		retval.append("	" + XMLHandler.addTagValue("method", method));
		retval.append("	" + XMLHandler.addTagValue("layername", layername));
		retval.append("	" + XMLHandler.addTagValue("maxFeatures", maxFeatures));
		retval.append("	" + XMLHandler.addTagValue("srs", srs));
		retval.append("	<filters>").append(Const.CR);
		if (filters != null) {
			for (Filter f : filters) {
				retval.append("		<filter>").append(Const.CR);
				retval.append("			").append(
						XMLHandler.addTagValue("attribute", f.getAttribute()));
				retval.append("			").append(
						XMLHandler.addTagValue("condition", f.getCondition()));
				retval.append("			").append(
						XMLHandler.addTagValue("value", f.getValue()));
				retval.append("			").append(
						XMLHandler.addTagValue("operator", f.getOperator()));
				retval.append("		</filter>").append(Const.CR);
			}
		}
		retval.append("	</filters>").append(Const.CR);
		retval.append("	<attributes>").append(Const.CR);
		if (attributes != null) {
			for (int i = 0; i < attributes.length; i++) {
				retval.append("		<attribute>").append(Const.CR);
				retval.append("			").append(
						XMLHandler.addTagValue("name", attributes[i]));
				retval.append("		</attribute>").append(Const.CR);
			}
		}
		retval.append("	</attributes>").append(Const.CR);
		return retval.toString();
	}

	@Override
	public void saveRep(Repository rep, long id_transformation, long id_step)
			throws KettleException {
		rep.saveStepAttribute(id_transformation, id_step, "capabilitiesUrl",
				capabilitiesUrl);
		rep.saveStepAttribute(id_transformation, id_step, "username", username);
		rep.saveStepAttribute(id_transformation, id_step, "password", password);
		rep.saveStepAttribute(id_transformation, id_step, "method", method);
		rep.saveStepAttribute(id_transformation, id_step, "layername",
				layername);
		rep.saveStepAttribute(id_transformation, id_step, "maxFeatures",
				maxFeatures);
		rep.saveStepAttribute(id_transformation, id_step, "srs", srs);
		if (filters != null) {
			int i = 0;
			for (Filter f : filters) {
				rep.saveStepAttribute(id_transformation, id_step, i,
						"filter_attribute", f.getAttribute());
				rep.saveStepAttribute(id_transformation, id_step, i,
						"filter_condition", f.getCondition());
				rep.saveStepAttribute(id_transformation, id_step, i,
						"filter_value", f.getValue());
				rep.saveStepAttribute(id_transformation, id_step, i,
						"filter_operator", f.getOperator());
				i++;
			}
		}
		if (attributes != null) {
			for (int i = 0; i < attributes.length; i++) {
				rep.saveStepAttribute(id_transformation, id_step, i,
						"attribute", attributes[i]);
			}
		}
	}

	@Override
	public void readRep(Repository rep, long id_step,
			List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleException {
		capabilitiesUrl = rep
				.getStepAttributeString(id_step, "capabilitiesUrl");
		username = rep.getStepAttributeString(id_step, "username");
		password = rep.getStepAttributeString(id_step, "password");
		method = rep.getStepAttributeBoolean(id_step, "method");
		layername = rep.getStepAttributeString(id_step, "layername");
		maxFeatures = rep.getStepAttributeString(id_step, "maxFeatures");
		srs = rep.getStepAttributeString(id_step, "srs");

		int nrFilters = rep.countNrStepAttributes(id_step, "filter_attribute");
		allocateFilters(nrFilters);
		for (int i = 0; i < nrFilters; i++) {
			filters.add(new Filter(rep.getStepAttributeString(id_step, i,
					"filter_attribute"), rep.getStepAttributeString(id_step, i,
					"filter_condition"), rep.getStepAttributeString(id_step, i,
					"filter_value"), rep.getStepAttributeString(id_step, i,
					"filter_operator")));
		}

		int nrAttributes = rep.countNrStepAttributes(id_step, "attribute");

		allocateAttributes(nrAttributes);
		for (int i = 0; i < nrAttributes; i++) {
			attributes[i] = rep.getStepAttributeString(id_step, i, "attribute");
		}
	}

	@Override
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
			StepMeta stepMeta, RowMetaInterface prev, String[] input,
			String[] output, RowMetaInterface info) {
	}

	@Override
	public StepInterface getStep(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans) {
		return new WFSInput(stepMeta, stepDataInterface, copyNr, transMeta,
				trans);
	}

	@Override
	public StepDataInterface getStepData() {
		return new WFSInputData();
	}

	private SimpleFeatureType getSimpleFeatureType() throws IOException {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(WFSDataStoreFactory.URL.key, capabilitiesUrl);
		parameters.put(WFSDataStoreFactory.PROTOCOL.key, method);
		if (!Const.isEmpty(username))
			parameters.put(WFSDataStoreFactory.USERNAME.key, username);
		if (!Const.isEmpty(password))
			parameters.put(WFSDataStoreFactory.PASSWORD.key, password);
		return DataStoreFinder.getDataStore(parameters).getSchema(layername);
	}

	public void getFields(RowMetaInterface row, String name,
			RowMetaInterface info[], StepMeta nextStep, VariableSpace space) {
		row.clear();
		row.addRowMeta(getOutputFields());
	}

	public RowMetaInterface getOutputFields() {
		RowMetaInterface row = new RowMeta();
		try {
			SimpleFeatureType sft = getSimpleFeatureType();
			for (AttributeDescriptor ad : sft.getAttributeDescriptors()) {
				int type = GTUtils.getGKBinding(ad.getType());
				ValueMeta v = new ValueMeta(ad.getName().getLocalPart(), type);
				if (type == ValueMetaInterface.TYPE_GEOMETRY)
					v.setGeometrySRS(GTUtils.getSRS((GeometryDescriptor) ad));
				row.addValueMeta(v);
			}
		} catch (Exception e) {
		}
		return row;
	}
}
