package org.pentaho.di.trans.steps.wfsinput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class WFSInput extends BaseStep implements StepInterface {
	private WFSInputMeta meta;
	private WFSInputData data;

	private DataStore ds;

	public WFSInput(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	private void buildDataStore() throws IOException {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(WFSDataStoreFactory.URL.key, meta.getCapabilitiesUrl());
		parameters.put(WFSDataStoreFactory.PROTOCOL.key, meta.getMethod());
		parameters.put(WFSDataStoreFactory.TIMEOUT.key, GTUtils.TIMEOUT);
		String username = meta.getUsername();
		if (!Const.isEmpty(username))
			parameters.put(WFSDataStoreFactory.USERNAME.key, username);
		String password = meta.getPassword();
		if (!Const.isEmpty(password))
			parameters.put(WFSDataStoreFactory.PASSWORD.key, password);
		ds = DataStoreFinder.getDataStore(parameters);
	}

	private Filter buildFilter(SimpleFeatureType sft) throws Exception {
		Filter filter = null;
		if (!Const.isEmpty(meta.getFilters().toArray())) {
			List<org.pentaho.di.trans.steps.wfsinput.Filter> filters = meta
					.getFilters();
			org.pentaho.di.trans.steps.wfsinput.Filter f = filters.get(0);
			filter = GTUtils.buildFilter(GTUtils.FF.property(sft.getDescriptor(
					f.getAttribute()).getName()), f.getCondition(), f
					.getValue(), sft.getDescriptor(f.getAttribute()).getType());

			for (int i = 1; i < filters.size(); i++) {
				f = filters.get(i);
				String operator = f.getOperator();
				if (operator.equals(Messages
						.getString("WFSInputDialog.Operator.And.Label")))
					filter = GTUtils.FF.and(filter, GTUtils.buildFilter(
							GTUtils.FF.property(sft.getDescriptor(
									f.getAttribute()).getName()),
							f.getCondition(), f.getValue(),
							sft.getDescriptor(f.getAttribute()).getType()));
				else if (operator.equals(Messages
						.getString("WFSInputDialog.Operator.Or.Label")))
					filter = GTUtils.FF.or(filter, GTUtils.buildFilter(
							GTUtils.FF.property(sft.getDescriptor(
									f.getAttribute()).getName()),
							f.getCondition(), f.getValue(),
							sft.getDescriptor(f.getAttribute()).getType()));
			}
		}
		return filter;
	}

	private List<Object[]> getFeatures() throws Exception {
		String layername = meta.getLayername();

		Query q = new Query(layername);

		FeatureSource<SimpleFeatureType, SimpleFeature> fs = ds
				.getFeatureSource(layername);

		SimpleFeatureType sft = fs.getSchema();

		Filter f = buildFilter(sft);
		if (f != null)
			q.setFilter(f);

		String maxFeatures = meta.getMaxFeatures();
		if (!Const.isEmpty(maxFeatures))
			q.setMaxFeatures(Integer.parseInt(maxFeatures));

		String srs = meta.getSrs();
		if (!Const.isEmpty(srs))
			q.setCoordinateSystem(CRS.decode(srs));

		return getRow(fs.getFeatures(q));
	}

	private List<Object[]> getRow(
			FeatureCollection<SimpleFeatureType, SimpleFeature> fc) {
		FeatureIterator<SimpleFeature> it = fc.features();
		List<Object[]> rows = new ArrayList<Object[]>(fc.size());
		try {
			while (it.hasNext()) {
				List<Object> attributeValues = it.next().getAttributes();
				Object[] r = new Object[attributeValues.size()];
				int i = 0;
				for (Object val : attributeValues) {
					r[i] = GTUtils.getJavaBinding(val);
					i++;
				}
				rows.add(r);
			}
		} finally {
			it.close();
		}
		return rows;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
			throws KettleException {
		meta = (WFSInputMeta) smi;
		data = (WFSInputData) sdi;

		Object[] r = getRow();

		if (first) {
			first = false;
			data.outputRowMeta = new RowMeta();
			try {
				buildDataStore();
			} catch (IOException e) {
				throw new KettleException(
						Messages.getString("WFSInputDialog.ErrorBuildingDataStore.DialogMessage"));
			}
			try {
				meta.getFields(data.outputRowMeta, getStepname(), null, null,
						this);

				List<Object[]> rows = getFeatures();

				if (rows != null) {
					for (Object[] row : rows) {
						putRow(data.outputRowMeta, ArrayUtils.addAll(r, row));
					}
				}
			} catch (Exception e) {
				throw new KettleException(
						Messages.getString("WFSInput.ErrorGettingFeatures.DialogMessage"),
						e);
			}
		}

		if (r == null) {
			setOutputDone();
			return false;
		}

		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (WFSInputMeta) smi;
		data = (WFSInputData) sdi;
		return super.init(smi, sdi) ? true : false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (WFSInputMeta) smi;
		data = (WFSInputData) sdi;
		super.dispose(smi, sdi);
	}

	public void run() {
		BaseStep.runStepThread(this, meta, data);
	}
}
